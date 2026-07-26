package com.eyevoicecoach.feature.recording

import android.os.SystemClock
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eyevoicecoach.domain.model.Recording
import com.eyevoicecoach.domain.model.SelfAssessment
import com.eyevoicecoach.domain.model.TensionLevel
import com.eyevoicecoach.domain.repository.AssessmentRepository
import com.eyevoicecoach.domain.repository.ProgramProgressRepository
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
    private val assessments: AssessmentRepository,
    private val programs: ProgramProgressRepository,
) : ViewModel() {
    private val tipId: Int = savedStateHandle.get<Int>("tipId") ?: 0
    private val programId: String? = savedStateHandle.get<String>("programId")
    private val programDay: Int? = savedStateHandle.get<Int>("dayNumber")
    private var startedAt = 0L

    /** Microphone levels for the recording waveform. */
    val amplitudes = recorder.amplitudes

    /** Persistent private recordings. */
    val recordings: StateFlow<List<Recording>> = recordingsRepository.observeRecordings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Current ExoPlayer state. */
    val playback = player.state

    private val _pendingAssessment = kotlinx.coroutines.flow.MutableStateFlow<PendingAssessment?>(null)

    /** A newly saved recording awaiting its private self-review. */
    val pendingAssessment: StateFlow<PendingAssessment?> = _pendingAssessment

    /** Whether the screen was opened from a valid exercise and can create a recording. */
    val canRecord: Boolean get() = tipId > 0

    /** Starts a recording after the UI has verified microphone access. */
    fun startRecording(): Result<Unit> = runCatching {
        recorder.start()
        startedAt = SystemClock.elapsedRealtime()
    }

    /** Finalizes a recording, then opens its self-review before a program day is completed. */
    fun stopRecording() {
        val uri = recorder.stop() ?: return
        val duration = ((SystemClock.elapsedRealtime() - startedAt) / 1000L).toInt()
        viewModelScope.launch {
            val recordingId = recordingsRepository.addRecording(uri, tipId, duration)
            _pendingAssessment.value = PendingAssessment(recordingId, programId, programDay)
        }
    }

    /** Saves the user's private recording review and completes the attached program day when applicable. */
    fun saveAssessment(clarity: Int, pace: Int, confidence: Int, pauses: Int, tension: TensionLevel, note: String) = viewModelScope.launch {
        val pending = _pendingAssessment.value ?: return@launch
        assessments.save(SelfAssessment(pending.recordingId, clarity, pace, confidence, pauses, tension, note, System.currentTimeMillis()))
        if (pending.programId != null && pending.dayNumber != null) {
            programs.completeDay(pending.programId, pending.dayNumber, pending.recordingId)
        }
        _pendingAssessment.value = null
    }

    /** Hides a review card without altering the safely saved recording. */
    fun dismissAssessment() { _pendingAssessment.value = null }

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

/** Identifies a freshly saved recording and its optional ready-made program session. */
data class PendingAssessment(val recordingId: Long, val programId: String?, val dayNumber: Int?)
