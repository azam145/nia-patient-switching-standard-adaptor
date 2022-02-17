package uk.nhs.adaptors.pss.gpc.service;

import static uk.nhs.adaptors.connector.model.MigrationStatus.REQUEST_RECEIVED;
import static uk.nhs.adaptors.pss.gpc.controller.header.HttpHeaders.FROM_ASID;
import static uk.nhs.adaptors.pss.gpc.controller.header.HttpHeaders.FROM_ODS;
import static uk.nhs.adaptors.pss.gpc.controller.header.HttpHeaders.TO_ASID;
import static uk.nhs.adaptors.pss.gpc.controller.header.HttpHeaders.TO_ODS;

import java.util.Map;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Parameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import uk.nhs.adaptors.common.model.TransferRequestMessage;
import uk.nhs.adaptors.common.service.MDCService;
import uk.nhs.adaptors.common.util.DateUtils;
import uk.nhs.adaptors.common.util.fhir.FhirParser;
import uk.nhs.adaptors.connector.dao.MigrationStatusLogDao;
import uk.nhs.adaptors.connector.dao.PatientMigrationRequestDao;
import uk.nhs.adaptors.connector.model.MigrationStatusLog;
import uk.nhs.adaptors.connector.model.PatientMigrationRequest;
import uk.nhs.adaptors.pss.gpc.amqp.PssQueuePublisher;
import uk.nhs.adaptors.pss.gpc.util.fhir.ParametersUtils;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PatientTransferService {
    private final FhirParser fhirParser;
    private final PatientMigrationRequestDao patientMigrationRequestDao;
    private final MigrationStatusLogDao migrationStatusLogDao;
    private final PssQueuePublisher pssQueuePublisher;
    private final DateUtils dateUtils;
    private final MDCService mdcService;

    public MigrationStatusLog handlePatientMigrationRequest(Parameters parameters, Map<String, String> headers) {
        var patientNhsNumber = ParametersUtils.getNhsNumberFromParameters(parameters).get().getValue();
        PatientMigrationRequest patientMigrationRequest = patientMigrationRequestDao.getMigrationRequest(patientNhsNumber);

        if (patientMigrationRequest == null) {
            patientMigrationRequestDao.addNewRequest(patientNhsNumber);
            int addedId = patientMigrationRequestDao.getMigrationRequestId(patientNhsNumber);
            migrationStatusLogDao.addMigrationStatusLog(REQUEST_RECEIVED, dateUtils.getCurrentOffsetDateTime(), addedId);

            var pssMessage = createTransferRequestMessage(patientNhsNumber, headers);
            pssQueuePublisher.sendToPssQueue(pssMessage);
        } else {
            return migrationStatusLogDao.getLatestMigrationStatusLog(patientMigrationRequest.getId());
        }
        return null;
    }

    public String getEmptyBundle() {
        return fhirParser.encodeToJson(new Bundle());
    }

    private TransferRequestMessage createTransferRequestMessage(String patientNhsNumber, Map<String, String> headers) {
        return TransferRequestMessage.builder()
            .conversationId(mdcService.getConversationId())
            .patientNhsNumber(patientNhsNumber)
            .toAsid(headers.get(TO_ASID))
            .fromAsid(headers.get(FROM_ASID))
            .toOds(headers.get(TO_ODS))
            .fromOds(headers.get(FROM_ODS))
            .build();
    }
}
