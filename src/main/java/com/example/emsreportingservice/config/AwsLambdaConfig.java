package com.example.emsreportingservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;

@Configuration
public class AwsLambdaConfig {

    @Bean
    public LambdaClient lambdaClient() {
        return LambdaClient.builder()
                .region(Region.AP_SOUTH_1) // set your region
                .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                .build();
    }
}
