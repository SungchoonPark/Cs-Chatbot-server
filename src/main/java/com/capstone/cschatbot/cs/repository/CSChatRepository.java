package com.capstone.cschatbot.cs.repository;

import com.capstone.cschatbot.cs.entity.CSChat;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CSChatRepository extends MongoRepository<CSChat, String> {
}
