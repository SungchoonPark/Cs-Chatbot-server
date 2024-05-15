package com.capstone.cschatbot.selfIntro.repository;

import com.capstone.cschatbot.selfIntro.entity.SelfIntro;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SelfIntroRepository extends MongoRepository<SelfIntro, String> {

    List<SelfIntro> findAllByMemberIdAndTerminateStatusTrue(String memberId);
}
