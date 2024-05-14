package com.capstone.cschatbot;

import com.capstone.cschatbot.chat.repository.ChatRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@EnableFeignClients
@EnableJpaAuditing
@EnableMongoRepositories(basePackageClasses = ChatRepository.class)
@SpringBootApplication
public class CsChatbotServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(CsChatbotServerApplication.class, args);
	}

}
