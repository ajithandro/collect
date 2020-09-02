package org.odk.collect.android.formmanagement.matchexactly;

import org.odk.collect.android.formmanagement.FormDeleter;
import org.odk.collect.android.formmanagement.FormDownloadException;
import org.odk.collect.android.formmanagement.FormDownloader;
import org.odk.collect.android.formmanagement.ServerFormDetails;
import org.odk.collect.android.formmanagement.ServerFormsDetailsFetcher;
import org.odk.collect.forms.Form;
import org.odk.collect.forms.FormsRepository;
import org.odk.collect.android.instances.InstancesRepository;
import org.odk.collect.openrosa.FormApiException;

import java.util.List;

public class ServerFormsSynchronizer {

    private final FormsRepository formsRepository;
    private final InstancesRepository instancesRepository;
    private final FormDownloader formDownloader;
    private final ServerFormsDetailsFetcher serverFormsDetailsFetcher;

    public ServerFormsSynchronizer(ServerFormsDetailsFetcher serverFormsDetailsFetcher, FormsRepository formsRepository, InstancesRepository instancesRepository, FormDownloader formDownloader) {
        this.serverFormsDetailsFetcher = serverFormsDetailsFetcher;
        this.formsRepository = formsRepository;
        this.instancesRepository = instancesRepository;
        this.formDownloader = formDownloader;
    }

    public void synchronize() throws FormApiException {
        List<ServerFormDetails> formList = serverFormsDetailsFetcher.fetchFormDetails();
        List<Form> formsOnDevice = formsRepository.getAll();
        FormDeleter formDeleter = new FormDeleter(formsRepository, instancesRepository);

        formsOnDevice.stream().forEach(form -> {
            if (formList.stream().noneMatch(f -> form.getJrFormId().equals(f.getFormId()))) {
                formDeleter.delete(form.getId());
            }
        });

        boolean downloadException = false;

        for (ServerFormDetails form : formList) {
            if (form.isNotOnDevice() || form.isUpdated()) {
                try {
                    formDownloader.downloadForm(form, null, null);
                } catch (FormDownloadException e) {
                    downloadException = true;
                }
            }
        }

        if (downloadException) {
            throw new FormApiException(FormApiException.Type.FETCH_ERROR);
        }
    }
}
