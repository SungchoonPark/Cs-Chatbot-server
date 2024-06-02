package com.capstone.cschatbot.cs.service.query;

import com.capstone.cschatbot.common.enums.CustomResponseStatus;
import com.capstone.cschatbot.common.exception.CustomException;
import com.capstone.cschatbot.cs.dto.response.CSChatDto;
import com.capstone.cschatbot.cs.dto.response.CSChatHistory;
import com.capstone.cschatbot.cs.dto.response.CSChatHistoryList;
import com.capstone.cschatbot.cs.domain.CSChat;
import com.capstone.cschatbot.cs.repository.CSChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CSQueryServiceImpl implements CSQueryService{
    private final CSChatRepository csChatRepository;

    @Override
    @Cacheable(value = "CSChats", key = "'csAllTopic'", unless = "#result == null", cacheManager = "oidcCacheManager")
    public CSChatHistoryList findAllCSChat(String memberId) {
        List<CSChat> csChats = csChatRepository.findAllByMemberIdAndTerminateStatusTrue(memberId);
        List<CSChatDto> csChatDtos = getCsChatDtos(csChats);

        return CSChatHistoryList.builder()
                .csChats(csChatDtos)
                .build();
    }

    @Override
    @Cacheable(value = "CSChats", key = "'csAll'", unless = "#result == null", cacheManager = "oidcCacheManager")
    public CSChatHistoryList findAllCSChatByTopic(String memberId, String topic) {
        List<CSChat> csChats = csChatRepository.findAllByMemberIdAndTopicEqualsAndTerminateStatusTrue(memberId, topic);
        List<CSChatDto> csChatDtos = getCsChatDtos(csChats);

        return CSChatHistoryList.builder()
                .csChats(csChatDtos)
                .build();
    }

    @Override
    @Cacheable(value = "CSChat", key = "#chatRoomId", unless = "#result == null", cacheManager = "oidcCacheManager")
    public CSChatHistory findCSChat(String chatRoomId) {
        CSChat csChat = csChatRepository.findById(chatRoomId)
                .orElseThrow(() -> new CustomException(CustomResponseStatus.SELF_INTRO_CHAT_NOT_FOUND));

        return CSChatHistory.builder()
                .chatEvaluations(csChat.getChatHistory())
                .build();
    }

    private static List<CSChatDto> getCsChatDtos(List<CSChat> csChats) {
        return csChats.stream()
                .map(csChat -> CSChatDto.builder()
                        .chatRoomId(csChat.getId())
                        .createdAt(csChat.getCreatedAt().toLocalDate())
                        .topic(csChat.getTopic())
                        .chatHistory(csChat.getChatHistory())
                        .build())
                .toList();
    }
}
