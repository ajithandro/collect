package org.odk.collect.audiorecorder.recorder

import android.media.MediaRecorder
import java.io.File

internal class MediaRecorderRecorder(private val cacheDir: File, private val mediaRecorderFactory: () -> MediaRecorderWrapper) : Recorder {

    private var mediaRecorder: MediaRecorderWrapper? = null
    private var file: File? = null

    override fun start(output: Output) {
        mediaRecorder = mediaRecorderFactory().also {
            it.setAudioSource(MediaRecorder.AudioSource.MIC)

            when (output) {
                Output.AMR -> {
                    val tempFile = File.createTempFile("recording", ".amr", cacheDir)
                    file = tempFile

                    it.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB)
                    it.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                    it.setAudioEncodingSampleRate(8000)
                    it.setAudioEncodingBitRate(12200)
                    it.setOutputFile(tempFile.absolutePath)
                }

                Output.AAC -> {
                    val tempFile = File.createTempFile("recording", ".m4a", cacheDir)
                    file = tempFile

                    it.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    it.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    it.setAudioEncodingSampleRate(32000)
                    it.setAudioEncodingBitRate(64000)
                    it.setOutputFile(tempFile.absolutePath)
                }
            }

            it.prepare()
            it.start()
        }
    }

    override fun stop(): File {
        stopAndReleaseMediaRecorder()
        return file!!
    }

    override fun cancel() {
        stopAndReleaseMediaRecorder()
        file?.delete()
    }

    override val amplitude: Int
        get() = mediaRecorder?.getMaxAmplitude() ?: 0

    override fun isRecording(): Boolean {
        return mediaRecorder != null
    }

    private fun stopAndReleaseMediaRecorder() {
        mediaRecorder?.apply {
            stop()
            release()
        }

        mediaRecorder = null
    }
}
