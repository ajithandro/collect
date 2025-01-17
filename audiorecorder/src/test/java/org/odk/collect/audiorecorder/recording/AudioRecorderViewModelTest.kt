package org.odk.collect.audiorecorder.recording

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.After
import org.junit.Test
import org.odk.collect.audiorecorder.recorder.Output
import org.odk.collect.testshared.LiveDataTester
import java.io.File

abstract class AudioRecorderViewModelTest {

    private val liveDataTester = LiveDataTester()

    abstract val viewModel: AudioRecorderViewModel
    abstract fun runBackground()
    abstract fun getLastRecordedFile(): File?

    @After
    fun teardown() {
        liveDataTester.teardown()
    }

    @Test
    fun isRecording_whenNoSession_isFalse() {
        runBackground()
        assertThat(viewModel.isRecording(), equalTo(false))
    }

    @Test
    fun isRecording_whenRecording_isTrue() {
        viewModel.start("session1", Output.AAC)

        runBackground()
        assertThat(viewModel.isRecording(), equalTo(true))
    }

    @Test
    fun isRecording_afterStop_isFalse() {
        viewModel.start("session1", Output.AAC)
        viewModel.stop()

        runBackground()
        assertThat(viewModel.isRecording(), equalTo(false))
    }

    @Test
    fun isRecording_afterCleanUp_isFalse() {
        viewModel.start("session1", Output.AAC)
        viewModel.cleanUp()

        runBackground()
        assertThat(viewModel.isRecording(), equalTo(false))
    }

    @Test
    fun getCurrentSession_beforeRecording_isNull() {
        val recording = liveDataTester.activate(viewModel.getCurrentSession())

        runBackground()
        assertThat(recording.value, equalTo(null))
    }

    @Test
    fun getCurrentSession_whenRecording_returnsSessionWithId() {
        val recording = liveDataTester.activate(viewModel.getCurrentSession())
        viewModel.start("session1", Output.AAC)

        runBackground()
        assertThat(recording.value, equalTo(RecordingSession("session1", null, 0, 0)))
    }

    @Test
    fun getCurrentSession_afterStop_isRecordedFile() {
        val recording = liveDataTester.activate(viewModel.getCurrentSession())
        viewModel.start("session1", Output.AAC)
        viewModel.stop()

        runBackground()
        assertThat(recording.value, equalTo(RecordingSession("session1", getLastRecordedFile(), 0, 0)))
    }

    @Test
    fun getCurrentSession_afterCleanUp_isNull() {
        val recording = liveDataTester.activate(viewModel.getCurrentSession())
        viewModel.start("session1", Output.AAC)
        viewModel.cleanUp()

        runBackground()
        assertThat(recording.value, equalTo(null))
    }
}
