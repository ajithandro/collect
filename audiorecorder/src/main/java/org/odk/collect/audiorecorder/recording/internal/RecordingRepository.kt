package org.odk.collect.audiorecorder.recording.internal

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.odk.collect.audiorecorder.recording.RecordingSession
import java.io.File

class RecordingRepository {

    private val _currentSession = MutableLiveData<RecordingSession?>(null)
    val currentSession: LiveData<RecordingSession?> = _currentSession

    fun start(sessionId: String) {
        _currentSession.value = RecordingSession(sessionId, null, 0, 0)
    }

    fun setDuration(duration: Long) {
        _currentSession.value?.let {
            _currentSession.value = it.copy(duration = duration)
        }
    }

    fun setAmplitude(amplitude: Int) {
        _currentSession.value?.let {
            _currentSession.value = it.copy(amplitude = amplitude)
        }
    }

    fun recordingReady(recording: File) {
        _currentSession.value?.let {
            _currentSession.value = it.copy(file = recording)
        }
    }

    fun clear() {
        _currentSession.value?.let {
            it.file?.delete()
        }

        _currentSession.value = null
    }
}
