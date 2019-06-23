package com.pfernand.pfauthserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

import java.io.FileOutputStream;
import java.io.IOException;

@SpringBootApplication
@EnableAutoConfiguration
@Configuration
@ComponentScan(value = {"com.pfernand.pfauthserver", "com.pfernand.security"})
public class PfAuthServerApplication {

	private static final String JKS_FILE_NAME = "client.truststore.jks";

	public static void main(String[] args) throws IOException {
		copyJksToTemp();
		SpringApplication.run(PfAuthServerApplication.class, args);
	}

	private static void copyJksToTemp() throws IOException {
		FileCopyUtils.copy(new ClassPathResource(JKS_FILE_NAME).getInputStream(),
				new FileOutputStream("/tmp/" + JKS_FILE_NAME));
	}
}
