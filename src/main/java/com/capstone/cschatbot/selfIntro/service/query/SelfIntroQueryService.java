package com.capstone.cschatbot.selfIntro.service.query;

import com.capstone.cschatbot.selfIntro.dto.response.SelfIntroDetail;
import com.capstone.cschatbot.selfIntro.dto.response.SelfIntroList;

public interface SelfIntroQueryService {
    SelfIntroList findAllSelfIntro(String memberId);
    SelfIntroDetail findSelfIntro(String chatRoomId);
}
