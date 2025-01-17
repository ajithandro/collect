package org.odk.collect.android.widgets.utilities;

import android.app.Activity;
import android.util.Pair;

import androidx.lifecycle.LifecycleOwner;

import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.listeners.PermissionListener;
import org.odk.collect.android.utilities.FormEntryPromptUtils;
import org.odk.collect.android.utilities.PermissionUtils;
import org.odk.collect.android.utilities.QuestionMediaManager;
import org.odk.collect.audiorecorder.recorder.Output;
import org.odk.collect.audiorecorder.recording.AudioRecorderViewModel;

import java.util.function.Consumer;

public class InternalRecordingRequester implements RecordingRequester {

    private final Activity activity;
    private final AudioRecorderViewModel viewModel;
    private final PermissionUtils permissionUtils;
    private final LifecycleOwner lifecycleOwner;
    private final QuestionMediaManager questionMediaManager;

    public InternalRecordingRequester(Activity activity, AudioRecorderViewModel viewModel, PermissionUtils permissionUtils, LifecycleOwner lifecycleOwner, QuestionMediaManager questionMediaManager) {
        this.activity = activity;
        this.viewModel = viewModel;
        this.permissionUtils = permissionUtils;
        this.lifecycleOwner = lifecycleOwner;
        this.questionMediaManager = questionMediaManager;
    }

    @Override
    public void requestRecording(FormEntryPrompt prompt) {
        permissionUtils.requestRecordAudioPermission(activity, new PermissionListener() {
            @Override
            public void granted() {
                String quality = FormEntryPromptUtils.getAttributeValue(prompt, "quality");
                if (quality != null && quality.equals("voice-only")) {
                    viewModel.start(prompt.getIndex().toString(), Output.AMR);
                } else {
                    viewModel.start(prompt.getIndex().toString(), Output.AAC);
                }
            }

            @Override
            public void denied() {

            }
        });
    }

    @Override
    public void onIsRecordingChanged(Consumer<Boolean> isRecordingListener) {
        viewModel.getCurrentSession().observe(lifecycleOwner, session -> {
            isRecordingListener.accept(session != null && session.getFile() == null);
        });
    }

    @Override
    public void onRecordingAvailable(FormEntryPrompt prompt, Consumer<String> recordingAvailableListener) {
        viewModel.getCurrentSession().observe(lifecycleOwner, session -> {
            if (session != null && session.getId().equals(prompt.getIndex().toString()) && session.getFile() != null) {
                questionMediaManager.createAnswerFile(session.getFile()).observe(lifecycleOwner, fileName -> {
                    if (fileName != null) {
                        viewModel.cleanUp();
                        recordingAvailableListener.accept(fileName);
                    }
                });
            }
        });
    }

    @Override
    public void onRecordingInProgress(FormEntryPrompt prompt, Consumer<Pair<Long, Integer>> durationListener) {
        viewModel.getCurrentSession().observe(lifecycleOwner, session -> {
            if (session != null && session.getId().equals(prompt.getIndex().toString())) {
                durationListener.accept(new Pair<>(session.getDuration(), session.getAmplitude()));
            }
        });
    }
}
