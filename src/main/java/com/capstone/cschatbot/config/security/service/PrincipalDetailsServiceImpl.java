package com.capstone.cschatbot.config.security.service;

import com.capstone.cschatbot.common.enums.CustomResponseStatus;
import com.capstone.cschatbot.common.exception.CustomException;
import com.capstone.cschatbot.member.domain.Member;
import com.capstone.cschatbot.member.infrastructure.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class PrincipalDetailsServiceImpl implements UserDetailsService {
    private final MemberRepository memberRepository;
    @Override
    public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new CustomException(CustomResponseStatus.MEMBER_NOT_FOUND));
        return new PrincipalDetails(member);
    }
}
