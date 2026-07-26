package com.eyevoicecoach.feature.recording

import android.content.Context
import android.media.MediaRecorder
import androidx.annotation.RequiresPermission
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/** Immutable playback data delivered to the recording interface. */
data class PlaybackState(
    val activeUri: String? = null,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0,
    val durationMs: Long = 0,
)

/** Records AAC audio in private app storage and samples amplitude every 100 milliseconds. */
@Singleton
class AudioRecorder @Inject constructor(@ApplicationContext private val context: Context) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var recorder: MediaRecorder? = null
    private var amplitudeJob: Job? = null
    private var outputFile: File? = null
    private val _amplitudes = MutableStateFlow<List<Float>>(emptyList())

    /** A bounded sequence of normalized microphone amplitudes for live waveform drawing. */
    val amplitudes: StateFlow<List<Float>> = _amplitudes.asStateFlow()

    /** Starts a high-quality AAC/M4A recording. The caller must have microphone permission. */
    @RequiresPermission(android.Manifest.permission.RECORD_AUDIO)
    fun start() {
        check(recorder == null) { "يوجد تسجيل جارٍ بالفعل" }
        val directory = File(context.filesDir, "recordings").apply { mkdirs() }
        outputFile = File(directory, "coach_${System.currentTimeMillis()}.m4a")
        val activeRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioSamplingRate(44_100)
            setAudioEncodingBitRate(128_000)
            setOutputFile(requireNotNull(outputFile).absolutePath)
            prepare()
            start()
        }
        recorder = activeRecorder
        _amplitudes.value = List(36) { 0.03f }
        amplitudeJob = scope.launch {
            while (isActive) {
                val amplitude = runCatching { activeRecorder.maxAmplitude / 32_767f }.getOrDefault(0f)
                _amplitudes.value = (_amplitudes.value + amplitude.coerceIn(0.02f, 1f)).takeLast(72)
                delay(100)
            }
        }
    }

    /** Stops the recorder and returns a private file URI, or null if recording could not complete. */
    fun stop(): String? {
        val activeRecorder = recorder ?: return null
        amplitudeJob?.cancel()
        amplitudeJob = null
        val result = runCatching {
            activeRecorder.stop()
            requireNotNull(outputFile).toURI().toString()
        }.getOrNull()
        activeRecorder.reset()
        activeRecorder.release()
        recorder = null
        if (result == null) outputFile?.delete()
        outputFile = null
        _amplitudes.value = emptyList()
        return result
    }

    /** Cancels a current recording and removes any incomplete audio file. */
    fun cancel() {
        runCatching { recorder?.stop() }
        recorder?.reset()
        recorder?.release()
        recorder = null
        amplitudeJob?.cancel()
        amplitudeJob = null
        outputFile?.delete()
        outputFile = null
        _amplitudes.value = emptyList()
    }
}

/** ExoPlayer controller with reactive progress and seek controls for private recordings. */
@Singleton
class AudioPlayer @Inject constructor(@ApplicationContext context: Context) {
    private val player = ExoPlayer.Builder(context).build()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var ticker: Job? = null
    private val _state = MutableStateFlow(PlaybackState())

    /** Current player state and progress. */
    val state: StateFlow<PlaybackState> = _state.asStateFlow()

    init {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) = publish()
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) player.seekTo(0)
                publish()
            }
        })
        ticker = scope.launch {
            while (isActive) {
                publish()
                delay(250)
            }
        }
    }

    /** Starts or pauses [uri], restarting player content when another file is selected. */
    fun toggle(uri: String) {
        if (_state.value.activeUri != uri) {
            player.setMediaItem(MediaItem.fromUri(uri))
            player.prepare()
            player.play()
        } else if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
        }
        publish()
    }

    /** Moves playhead by [milliseconds] while keeping it within file bounds. */
    fun seekBy(milliseconds: Long) {
        player.seekTo((player.currentPosition + milliseconds).coerceIn(0, player.duration.coerceAtLeast(0)))
        publish()
    }

    /** Seeks to an absolute [positionMs]. */
    fun seekTo(positionMs: Long) {
        player.seekTo(positionMs.coerceAtLeast(0))
        publish()
    }

    /** Stops playback if it currently owns [uri]. */
    fun stopIfPlaying(uri: String) {
        if (_state.value.activeUri == uri) {
            player.stop()
            _state.value = PlaybackState()
        }
    }

    private fun publish() {
        _state.value = PlaybackState(
            activeUri = _state.value.activeUri ?: player.currentMediaItem?.localConfiguration?.uri?.toString(),
            isPlaying = player.isPlaying,
            positionMs = player.currentPosition.coerceAtLeast(0),
            durationMs = player.duration.coerceAtLeast(0),
        )
    }
}
