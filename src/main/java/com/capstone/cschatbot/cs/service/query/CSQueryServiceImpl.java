package com.capstone.cschatbot.cs.service.query;

import com.capstone.cschatbot.common.enums.CustomResponseStatus;
import com.capstone.cschatbot.common.exception.CustomException;
import com.capstone.cschatbot.cs.dto.response.CSChatHistory;
import com.capstone.cschatbot.cs.dto.response.CSChatHistoryList;
import com.capstone.cschatbot.cs.domain.CSChat;
import com.capstone.cschatbot.cs.repository.CSChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CSQueryServiceImpl implements CSQueryService{
    private final CSChatRepository csChatRepository;

    @Override
    public CSChatHistoryList findAllCSChat(String memberId) {
        List<CSChat> csChats = csChatRepository.findAllByMemberIdAndTerminateStatusTrue(memberId);
        return CSChatHistoryList.builder()
                .csChats(csChats)
                .build();
    }

    @Override
    public CSChatHistoryList findAllCSChatByTopic(String memberId, String topic) {
        List<CSChat> csChats = csChatRepository.findAllByMemberIdAndTopicEqualsAndTerminateStatusTrue(memberId, topic);
        return CSChatHistoryList.builder()
                .csChats(csChats)
                .build();
    }

    @Override
    public CSChatHistory findCSChat(String chatRoomId) {
        CSChat csChat = csChatRepository.findById(chatRoomId)
                .orElseThrow(() -> new CustomException(CustomResponseStatus.SELF_INTRO_CHAT_NOT_FOUND));
        return CSChatHistory.builder()
                .chatEvaluations(csChat.getChatHistory())
                .build();
    }
}
