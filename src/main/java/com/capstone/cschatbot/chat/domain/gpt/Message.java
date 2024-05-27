package com.capstone.cschatbot.chat.domain.gpt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Message {
    private String role;
    private String content;
}