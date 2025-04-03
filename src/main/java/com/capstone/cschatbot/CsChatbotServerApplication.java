package com.capstone.cschatbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@EnableFeignClients
@EnableMongoAuditing
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class CsChatbotServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(CsChatbotServerApplication.class, args);
	}

}
