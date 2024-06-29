package com.capstone.cschatbot.selfIntro.application;

import com.capstone.cschatbot.selfIntro.dto.request.SelfIntroChatInfo;
import com.capstone.cschatbot.selfIntro.dto.request.SelfIntroChatRequest;
import com.capstone.cschatbot.selfIntro.dto.response.NewQuestionAndGrade;
import com.capstone.cschatbot.common.dto.QuestionAndChatId;

public interface SelfIntroService {
    QuestionAndChatId initiateSelfIntroChat(String memberId, SelfIntroChatRequest chat);
    NewQuestionAndGrade processSelfIntroChat(String memberId, SelfIntroChatInfo selfIntroChatInfo);
    void terminateSelfIntroChat(String memberId, String chatRoomId);
    void deleteSelfIntroChat(String memberId, String chatRoomId);

}
