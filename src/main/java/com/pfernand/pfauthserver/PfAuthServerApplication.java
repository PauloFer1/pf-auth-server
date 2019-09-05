package com.pfernand.pfauthserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;


@SpringBootApplication
@EnableAutoConfiguration
@Configuration
@ComponentScan(value = {"com.pfernand.pfauthserver", "com.pfernand.security"})
public class PfAuthServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(PfAuthServerApplication.class, args);
	}
}
