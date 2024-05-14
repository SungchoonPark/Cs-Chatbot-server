package com.capstone.cschatbot.chat.service.chat;

import com.capstone.cschatbot.chat.dto.request.ClientAnswer;
import com.capstone.cschatbot.chat.dto.request.SelfIntroChatRequest;
import com.capstone.cschatbot.chat.dto.response.NewQuestion;
import com.capstone.cschatbot.chat.dto.response.NewQuestionAndGrade;
import com.capstone.cschatbot.chat.dto.response.QuestionAndChatId;

public interface ChatService {
    QuestionAndChatId initiateCSChat(String memberId, String topic);
    QuestionAndChatId initiateSelfIntroChat(String memberId, SelfIntroChatRequest chat);
    NewQuestion processCSChat(String memberId, ClientAnswer clientAnswer);
    NewQuestionAndGrade processSelfIntroChat(String memberId, ClientAnswer clientAnswer, String chatRoomId);
    void terminateCSChat(String memberId, String chatRoomId);
    void terminateSelfIntroChat(String memberId, String chatRoomId);

}
