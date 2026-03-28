package io.darbata.zerotrust.config;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.okhttp.client.OkHttpRestfulClientFactory;
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FhirConfig {

    @Value("${aws.healthlake.endpoint}")
    private String healthLakeEndpoint;

    @Value("${aws.region}")
    private String region;

    @Bean
    public FhirContext fhirContext(OkHttpClient httpClient) {
        FhirContext ctx = FhirContext.forR4();
        OkHttpRestfulClientFactory clientFactory = new OkHttpRestfulClientFactory(ctx);
        clientFactory.setHttpClient(httpClient);
        clientFactory.setServerValidationMode(
                ServerValidationModeEnum.NEVER
        );
        ctx.setRestfulClientFactory(clientFactory);
        return ctx;
    }

    @Bean
    public IGenericClient fhirClient(FhirContext ctx) {
        return ctx.newRestfulGenericClient(healthLakeEndpoint);
    }
}