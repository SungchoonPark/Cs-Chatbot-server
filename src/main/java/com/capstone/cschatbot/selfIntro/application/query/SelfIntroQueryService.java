package com.capstone.cschatbot.selfIntro.application.query;

import com.capstone.cschatbot.selfIntro.dto.response.SelfIntroDetail;
import com.capstone.cschatbot.selfIntro.dto.response.SelfIntroList;

public interface SelfIntroQueryService {
    SelfIntroList findAllSelfIntro(String memberId);
    SelfIntroDetail findSelfIntro(String chatRoomId);
}
