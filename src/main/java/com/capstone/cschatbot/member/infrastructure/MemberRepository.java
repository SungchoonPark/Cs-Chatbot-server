package com.capstone.cschatbot.member.infrastructure;

import com.capstone.cschatbot.member.domain.Member;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface MemberRepository extends MongoRepository<Member, String> {
    Optional<Member> findByOauthInfoOid(String oid);
}
