package com.jam.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class S3PresignerConfig {
	
	@Bean(destroyMethod = "close") 
    public S3Presigner s3Presigner() {
		 return S3Presigner.builder()
				 .region(Region.AP_NORTHEAST_2) // 서울
				 .credentialsProvider(DefaultCredentialsProvider.builder().build())
				 .build();
    }
}
