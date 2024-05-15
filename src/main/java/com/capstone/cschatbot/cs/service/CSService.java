package com.capstone.cschatbot.cs.service;

import com.capstone.cschatbot.chat.dto.request.ClientAnswer;
import com.capstone.cschatbot.chat.dto.response.QuestionAndChatId;
import com.capstone.cschatbot.cs.dto.response.CSChatHistory;
import com.capstone.cschatbot.cs.dto.response.CSChatHistoryList;
import com.capstone.cschatbot.cs.dto.response.NewQuestion;
import com.capstone.cschatbot.cs.entity.CSChat;

public interface CSService {
    QuestionAndChatId initiateCSChat(String memberId, String topic);
    NewQuestion processCSChat(String memberId, ClientAnswer clientAnswer);
    CSChatHistory terminateCSChat(String memberId, String chatRoomId);
    CSChatHistoryList findAllCSChat(String memberId);
    CSChatHistory findCSChat(String chatRoomId);
}