package com.capstone.cschatbot.selfIntro.service;

import com.capstone.cschatbot.chat.dto.request.ClientAnswer;
import com.capstone.cschatbot.selfIntro.dto.request.SelfIntroChatRequest;
import com.capstone.cschatbot.selfIntro.dto.response.NewQuestionAndGrade;
import com.capstone.cschatbot.chat.dto.response.QuestionAndChatId;
import com.capstone.cschatbot.selfIntro.dto.response.SelfIntroDetail;
import com.capstone.cschatbot.selfIntro.dto.response.SelfIntroList;

public interface SelfIntroService {
    QuestionAndChatId initiateSelfIntroChat(String memberId, SelfIntroChatRequest chat);
    NewQuestionAndGrade processSelfIntroChat(String memberId, ClientAnswer clientAnswer, String chatRoomId);
    void terminateSelfIntroChat(String memberId, String chatRoomId);

}
