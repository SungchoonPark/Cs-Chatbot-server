package com.capstone.cschatbot.cs.repository;

import com.capstone.cschatbot.cs.domain.CSChat;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CSChatRepository extends MongoRepository<CSChat, String> {
    List<CSChat> findAllByMemberIdAndTerminateStatusTrue(String memberId);
    List<CSChat> findAllByMemberIdAndTopicEqualsAndTerminateStatusTrue(String memberId, String topic);
}
