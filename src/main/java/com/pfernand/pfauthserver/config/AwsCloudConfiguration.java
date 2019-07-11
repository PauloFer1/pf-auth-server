package com.pfernand.pfauthserver.config;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchAsync;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchAsyncClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class AwsCloudConfiguration {

    @Bean
    public ClientConfiguration clientConfiguration() {
        return new ClientConfiguration()
                .withClientExecutionTimeout(10000);
    }

    @Bean
    @Primary
    public AmazonCloudWatchAsync amazonCloudWatchAsync(@Value("${cloud.aws.region.static}") final String awsRegion,
                                                       final ClientConfiguration clientConfiguration) {
        return AmazonCloudWatchAsyncClientBuilder.standard()
                .withRegion(awsRegion)
                .withClientConfiguration(clientConfiguration)
                .build();
    }
}
