package io.darbata.zerotrust;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okio.Buffer;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.http.SdkHttpFullRequest;
import software.amazon.awssdk.http.SdkHttpMethod;
import software.amazon.awssdk.http.auth.aws.signer.AwsV4HttpSigner;
import software.amazon.awssdk.http.auth.spi.signer.SignedRequest;
import software.amazon.awssdk.identity.spi.AwsCredentialsIdentity;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;

public class AwsSigV4Interceptor implements Interceptor {

    private final AwsCredentialsProvider credentialsProvider;
    private final String service;
    private final String region;

    public AwsSigV4Interceptor(AwsCredentialsProvider credentialsProvider, String service, String region) {
        this.credentialsProvider = credentialsProvider;
        this.service = service;
        this.region = region;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();

        AwsCredentials credentials = credentialsProvider.resolveCredentials();
        AwsCredentialsIdentity identity = (credentials instanceof AwsSessionCredentials s)
                ? AwsSessionCredentials.create(s.accessKeyId(), s.secretAccessKey(), s.sessionToken())
                : AwsCredentialsIdentity.create(credentials.accessKeyId(), credentials.secretAccessKey());

        SdkHttpFullRequest sdkRequest = toSdkRequest(originalRequest);

        AwsV4HttpSigner signer = AwsV4HttpSigner.create();
        SignedRequest signedRequest = signer.sign(r -> r
                .identity(identity)
                .request(sdkRequest)
                .putProperty(AwsV4HttpSigner.SERVICE_SIGNING_NAME, service)
                .putProperty(AwsV4HttpSigner.REGION_NAME, region)
        );

        SdkHttpFullRequest signedSdkRequest = (SdkHttpFullRequest) signedRequest.request();
        Request okHttpSigned = applySignedHeaders(originalRequest, signedSdkRequest.headers());
        return chain.proceed(okHttpSigned);
    }

    private SdkHttpFullRequest toSdkRequest(Request request) throws IOException {
        SdkHttpFullRequest.Builder builder = SdkHttpFullRequest.builder()
                .uri(URI.create(request.url().toString()))
                .method(SdkHttpMethod.fromValue(request.method()));

        request.headers().toMultimap().forEach(builder::putHeader);

        if (request.body() != null) {
            Buffer buffer = new Buffer();
            request.body().writeTo(buffer);
            builder.contentStreamProvider(() -> buffer.inputStream());
        }

        return builder.build();
    }

    private Request applySignedHeaders(Request original, Map<String, List<String>> signedHeaders) {
        Request.Builder builder = original.newBuilder();
        signedHeaders.forEach((name, values) -> {
            builder.removeHeader(name);
            values.forEach(value -> builder.addHeader(name, value));
        });
        return builder.build();
    }
}