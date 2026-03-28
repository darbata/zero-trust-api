package io.darbata.zerotrust;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PatientService {
    private final IGenericClient client;

    public PatientService(IGenericClient client) {
        this.client = client;
    }

    public List<Patient> getAllPatients() {
        Bundle bundle = client
                .search()
                .forResource(Patient.class)
                .returnBundle(Bundle.class)
                .execute();

        return bundle.getEntry().stream()
                .map(entry -> (Patient) entry.getResource())
                .toList();
    }

    public Patient getPatient(String resourceId) {
        return client.read()
                .resource(Patient.class)
                .withId(resourceId)
                .execute();
    }
}