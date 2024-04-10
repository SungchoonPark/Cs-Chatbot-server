package com.capstone.cschatbot.member.repository;

import com.capstone.cschatbot.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
public interface MemberRepository extends JpaRepository<Member, Long> {
}
