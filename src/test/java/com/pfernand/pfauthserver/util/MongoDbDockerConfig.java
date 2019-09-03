package com.pfernand.pfauthserver.util;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.io.BufferedReader;
import java.io.InputStreamReader;

//@Configuration
public class MongoDbDockerConfig {

//    @Bean
//    @Order(Ordered.HIGHEST_PRECEDENCE)
    public String mongoDbDocker() throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("src/test/resources/start-mongo.sh");

        Process process = processBuilder.start();
        StringBuilder output = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line + "\n");
        }
        int exitVal = process.waitFor();
        if (exitVal == 0) {
            System.out.println("Success!");
            System.out.println(output);
            System.exit(0);
        } else {
            System.out.println(output);
            System.out.println("Something went wrong starting the mongoDB command!");
        }
        return "OK";
    }
}
