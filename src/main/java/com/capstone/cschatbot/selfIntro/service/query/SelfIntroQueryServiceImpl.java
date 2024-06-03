package com.capstone.cschatbot.selfIntro.service.query;

import com.capstone.cschatbot.common.enums.CustomResponseStatus;
import com.capstone.cschatbot.common.exception.CustomException;
import com.capstone.cschatbot.selfIntro.dto.response.SelfIntroDetail;
import com.capstone.cschatbot.selfIntro.dto.response.SelfIntroDto;
import com.capstone.cschatbot.selfIntro.dto.response.SelfIntroList;
import com.capstone.cschatbot.selfIntro.entity.SelfIntro;
import com.capstone.cschatbot.selfIntro.repository.SelfIntroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SelfIntroQueryServiceImpl implements SelfIntroQueryService {
    private final SelfIntroRepository selfIntroRepository;

    @Override
    @Cacheable(value = "SelfIntros", key = "'selfAll'", unless = "#result == null", cacheManager = "oidcCacheManager")
    public SelfIntroList findAllSelfIntro(String memberId) {
        List<SelfIntro> selfIntros = selfIntroRepository.findAllByMemberIdAndTerminateStatusTrue(memberId);
        List<SelfIntroDto> selfIntroDtos = selfIntros.stream()
                .map(selfIntro -> SelfIntroDto.builder()
                        .chatRoomId(selfIntro.getId())
                        .createdAt(selfIntro.getCreatedAt().toLocalDate())
                        .selfIntroChats(selfIntro.getSelfIntroChats())
                        .build())
                .toList();


        return SelfIntroList.builder()
                .selfIntros(selfIntroDtos)
                .build();
    }

    @Override
    @Cacheable(value = "SelfIntro", key = "#chatRoomId", unless = "#result == null", cacheManager = "oidcCacheManager")
    public SelfIntroDetail findSelfIntro(String chatRoomId) {
        SelfIntro selfIntro = selfIntroRepository.findById(chatRoomId)
                .orElseThrow(() -> new CustomException(CustomResponseStatus.SELF_INTRO_CHAT_NOT_FOUND));

        return SelfIntroDetail.builder()
                .selfIntroChats(selfIntro.getSelfIntroChats())
                .build();
    }
}
