package com.capstone.cschatbot.chat.repository;

import com.capstone.cschatbot.chat.entity.SelfIntro;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SelfIntroRepository extends MongoRepository<SelfIntro, String> {
}
