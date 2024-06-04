package com.capstone.cschatbot.cs.service;

import com.capstone.cschatbot.cs.dto.request.ClientAnswer;
import com.capstone.cschatbot.chat.dto.response.QuestionAndChatId;
import com.capstone.cschatbot.cs.dto.request.CSChatInfo;
import com.capstone.cschatbot.cs.dto.response.CSChatHistory;
import com.capstone.cschatbot.cs.dto.response.NewQuestion;

public interface CSService {
    QuestionAndChatId initiateCSChat(String memberId, String topic);
    NewQuestion processCSChat(String memberId, ClientAnswer clientAnswer);
    CSChatHistory terminateCSChat(String memberId, CSChatInfo csChatInfo);
    void deleteCSChat(String memberId, CSChatInfo csChatInfo);
}
