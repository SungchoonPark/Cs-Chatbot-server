package com.capstone.cschatbot.chat.repository;

import com.capstone.cschatbot.chat.entity.CSChat;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ChatRepository extends MongoRepository<CSChat, String> {
}

