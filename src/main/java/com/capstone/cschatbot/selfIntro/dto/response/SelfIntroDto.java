package com.capstone.cschatbot.selfIntro.dto.response;

import com.capstone.cschatbot.selfIntro.entity.SelfIntroChat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record SelfIntroDto(
        String chatRoomId,
        @JsonSerialize(using = LocalDateSerializer.class)
        @JsonDeserialize(using = LocalDateDeserializer.class)
        LocalDate createdAt,
        List<SelfIntroChat> selfIntroChats
) {
}
