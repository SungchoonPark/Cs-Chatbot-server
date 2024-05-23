package com.capstone.cschatbot.selfIntro.service.query;

import com.capstone.cschatbot.common.enums.CustomResponseStatus;
import com.capstone.cschatbot.common.exception.CustomException;
import com.capstone.cschatbot.selfIntro.dto.response.SelfIntroDetail;
import com.capstone.cschatbot.selfIntro.dto.response.SelfIntroList;
import com.capstone.cschatbot.selfIntro.entity.SelfIntro;
import com.capstone.cschatbot.selfIntro.repository.SelfIntroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SelfIntroQueryServiceImpl implements SelfIntroQueryService {
    private final SelfIntroRepository selfIntroRepository;

    @Override
    public SelfIntroList findAllSelfIntro(String memberId) {
        List<SelfIntro> selfIntros = selfIntroRepository.findAllByMemberIdAndTerminateStatusTrue(memberId);
        return SelfIntroList.builder()
                .selfIntros(selfIntros)
                .build();
    }

    @Override
    public SelfIntroDetail findSelfIntro(String chatRoomId) {
        SelfIntro selfIntro = selfIntroRepository.findById(chatRoomId)
                .orElseThrow(() -> new CustomException(CustomResponseStatus.SELF_INTRO_CHAT_NOT_FOUND));

        return SelfIntroDetail.builder()
                .selfIntroChats(selfIntro.getSelfIntroChats())
                .build();
    }
}
