package com.capstone.cschatbot.cs.application.query;

import com.capstone.cschatbot.cs.dto.response.CSChatHistory;
import com.capstone.cschatbot.cs.dto.response.CSChatHistoryList;

public interface CSQueryService {

    CSChatHistoryList findAllCSChat(String memberId);
    CSChatHistoryList findAllCSChatByTopic(String memberId, String topic);
    CSChatHistory findCSChat(String chatRoomId);
}
