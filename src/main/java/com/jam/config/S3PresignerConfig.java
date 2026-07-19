package com.jam.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class S3PresignerConfig {
	
	@Bean
    public AwsCredentialsProvider awsCredentialsProvider() {
        return DefaultCredentialsProvider.builder().build();
    }
	
	@Bean(destroyMethod = "close")
	public S3Client s3Client(AwsCredentialsProvider awsCredentialsProvider) {
		return S3Client.builder()
			.region(Region.AP_NORTHEAST_2)
			.credentialsProvider(awsCredentialsProvider)
			.build();
	}
	
	@Bean(destroyMethod = "close") 
    public S3Presigner s3Presigner(AwsCredentialsProvider awsCredentialsProvider) {
		 return S3Presigner.builder()
				 .region(Region.AP_NORTHEAST_2) // 서울
				 .credentialsProvider(awsCredentialsProvider)
				 .build();
    }
	
	@Bean
    public ApplicationRunner warmUpAwsCredentials(AwsCredentialsProvider awsCredentialsProvider) {
        return args -> {
            awsCredentialsProvider.resolveCredentials();
        };
    }
}
