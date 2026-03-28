package io.darbata.zerotrust.config;

import io.darbata.zerotrust.AwsSigV4Interceptor;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;

@Configuration
public class HttpClientConfig {

    @Value("${aws.region}")
    private String awsRegion;

    @Bean
    public OkHttpClient okHttpClient(AwsCredentialsProvider credentialsProvider) {
        return new OkHttpClient.Builder()
                .addInterceptor(new AwsSigV4Interceptor(credentialsProvider, "healthlake", awsRegion))
                .build();
    }

}