package org.odk.collect.audiorecorder.recording.internal

import android.app.Service
import android.content.Intent
import android.os.IBinder
import org.odk.collect.async.Cancellable
import org.odk.collect.async.Scheduler
import org.odk.collect.audiorecorder.getComponent
import org.odk.collect.audiorecorder.recorder.Output
import org.odk.collect.audiorecorder.recorder.Recorder
import javax.inject.Inject

class AudioRecorderService : Service() {

    @Inject
    internal lateinit var recorder: Recorder

    @Inject
    internal lateinit var recordingRepository: RecordingRepository

    @Inject
    internal lateinit var scheduler: Scheduler

    private lateinit var notification: RecordingForegroundServiceNotification
    private var duration = 0L
    private var durationUpdates: Cancellable? = null
    private var amplitudeUpdates: Cancellable? = null

    override fun onCreate() {
        super.onCreate()
        getComponent().inject(this)
        notification = RecordingForegroundServiceNotification(this, recordingRepository)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val sessionId = intent.getStringExtra(EXTRA_SESSION_ID)
                val output = intent.getSerializableExtra(EXTRA_OUTPUT) as Output

                if (!recorder.isRecording() && sessionId != null) {
                    notification.show()

                    recordingRepository.start(sessionId)
                    recorder.start(output)
                    durationUpdates = scheduler.repeat(
                        {
                            recordingRepository.setDuration(duration)
                            duration += 1000
                        },
                        1000L
                    )

                    amplitudeUpdates = scheduler.repeat({ recordingRepository.setAmplitude(recorder.amplitude) }, 100L)
                }
            }

            ACTION_STOP -> {
                stopRecording()
            }

            ACTION_CLEAN_UP -> {
                cleanUp()
            }
        }

        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        cleanUp()
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    private fun stopRecording() {
        amplitudeUpdates?.cancel()
        durationUpdates?.cancel()
        notification.dismiss()

        val file = recorder.stop()
        recordingRepository.recordingReady(file)
    }

    private fun cleanUp() {
        amplitudeUpdates?.cancel()
        durationUpdates?.cancel()
        notification.dismiss()

        recorder.cancel()
        recordingRepository.clear()
    }

    companion object {
        const val ACTION_START = "START"
        const val ACTION_STOP = "STOP"
        const val ACTION_CLEAN_UP = "CLEAN_UP"

        const val EXTRA_SESSION_ID = "EXTRA_SESSION_ID"
        const val EXTRA_OUTPUT = "EXTRA_OUTPUT"
    }
}
