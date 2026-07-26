package com.eyevoicecoach.feature.recording

import android.os.SystemClock
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eyevoicecoach.domain.model.Recording
import com.eyevoicecoach.domain.repository.RecordingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Manages a permission-gated recording session, waveform and persistent private recordings. */
@HiltViewModel
class RecordingViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val recorder: AudioRecorder,
    private val player: AudioPlayer,
    private val recordingsRepository: RecordingRepository,
) : ViewModel() {
    private val tipId: Int = savedStateHandle.get<Int>("tipId") ?: 0
    private var startedAt = 0L

    /** Microphone levels for the recording waveform. */
    val amplitudes = recorder.amplitudes

    /** Persistent private recordings. */
    val recordings: StateFlow<List<Recording>> = recordingsRepository.observeRecordings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Current ExoPlayer state. */
    val playback = player.state

    /** Whether the screen was opened from a valid exercise and can create a recording. */
    val canRecord: Boolean get() = tipId > 0

    /** Starts a recording after the UI has verified microphone access. */
    fun startRecording(): Result<Unit> = runCatching {
        recorder.start()
        startedAt = SystemClock.elapsedRealtime()
    }

    /** Finalizes a recording and writes its metadata only if a valid private file exists. */
    fun stopRecording() {
        val uri = recorder.stop() ?: return
        val duration = ((SystemClock.elapsedRealtime() - startedAt) / 1000L).toInt()
        viewModelScope.launch { recordingsRepository.addRecording(uri, tipId, duration) }
    }

    /** Discards the active unfinished recording. */
    fun cancelRecording() = recorder.cancel()

    /** Toggles playback for [recording]. */
    fun togglePlayback(recording: Recording) = player.toggle(recording.uri)

    /** Seeks the current recording by [milliseconds]. */
    fun seekBy(milliseconds: Long) = player.seekBy(milliseconds)

    /** Seeks the active recording to [positionMs]. */
    fun seekTo(positionMs: Long) = player.seekTo(positionMs)

    /** Deletes a recording and stops playback when necessary. */
    fun delete(recording: Recording) = viewModelScope.launch {
        player.stopIfPlaying(recording.uri)
        recordingsRepository.deleteRecording(recording)
    }

    override fun onCleared() {
        super.onCleared()
        recorder.cancel()
    }
}
