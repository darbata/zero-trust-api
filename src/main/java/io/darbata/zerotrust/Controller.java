package io.darbata.zerotrust;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class Controller {

    private final PatientService patientService;

    public Controller(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping("/")
    public ResponseEntity<?> hello(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(jwt.getClaims());
    }

    @GetMapping("/patients")
    public ResponseEntity<?> fetchPatients(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(patientService.getAllPatients());
    }

    @GetMapping("/patients/{resourceId}")
    public ResponseEntity<?> fetchPatients(@AuthenticationPrincipal Jwt jwt, @PathVariable String resourceId) {
        return ResponseEntity.ok(patientService.getPatient(resourceId));
    }

}