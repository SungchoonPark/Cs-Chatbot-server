package com.capstone.cschatbot.cs.dto.response;

import com.capstone.cschatbot.cs.domain.ChatEvaluation;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record CSChatDto(
        String chatRoomId,
        @JsonSerialize(using = LocalDateSerializer.class)
        @JsonDeserialize(using = LocalDateDeserializer.class)
        LocalDate createdAt,
        String topic,
        List<ChatEvaluation> chatHistory
) {
}
