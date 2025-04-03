package com.capstone.cschatbot.selfIntro.infrastructure;

import com.capstone.cschatbot.selfIntro.domain.SelfIntro;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SelfIntroRepository extends MongoRepository<SelfIntro, String> {

    List<SelfIntro> findAllByMemberIdAndTerminateStatusTrue(String memberId);
}
