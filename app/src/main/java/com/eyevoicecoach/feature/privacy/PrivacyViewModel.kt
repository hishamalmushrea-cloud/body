package com.eyevoicecoach.feature.privacy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eyevoicecoach.domain.repository.AssessmentRepository
import com.eyevoicecoach.domain.repository.ProgramProgressRepository
import com.eyevoicecoach.domain.repository.RecordingRepository
import com.eyevoicecoach.domain.repository.SettingsRepository
import com.eyevoicecoach.domain.repository.TipRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Executes explicit user-requested deletion of private on-device data. */
@HiltViewModel
class PrivacyViewModel @Inject constructor(
    private val recordings: RecordingRepository,
    private val tips: TipRepository,
    private val assessments: AssessmentRepository,
    private val programs: ProgramProgressRepository,
    private val settings: SettingsRepository,
) : ViewModel() {
    private val _message = MutableStateFlow<String?>(null)

    /** Latest successful privacy action message. */
    val message: StateFlow<String?> = _message.asStateFlow()

    /** Deletes only private audio files and their database records. */
    fun deleteRecordings() = runAction("تم حذف التسجيلات من جهازك.") { recordings.deleteAllRecordings() }

    /** Deletes viewed history and favorites while retaining bundled exercises. */
    fun deleteHistory() = runAction("تم حذف سجل التدريب والمفضلة.") { tips.clearUserActivity() }

    /** Deletes subjective reviews without affecting the audio files. */
    fun deleteAssessments() = runAction("تم حذف جميع التقييمات الذاتية.") { assessments.deleteAll() }

    /** Restores the experience to first-run state and deletes every piece of user-created data. */
    fun resetApplication() = runAction("تمت إعادة التطبيق كبداية جديدة.") {
        recordings.deleteAllRecordings()
        tips.clearUserActivity()
        assessments.deleteAll()
        programs.clearAll()
        settings.clearAll()
    }

    /** Clears an already displayed feedback message. */
    fun clearMessage() { _message.value = null }

    private fun runAction(success: String, action: suspend () -> Unit) = viewModelScope.launch {
        try {
            action()
            _message.value = success
        } catch (_: Exception) {
            _message.value = "تعذر إكمال العملية. حاول مرة أخرى."
        }
    }
}
