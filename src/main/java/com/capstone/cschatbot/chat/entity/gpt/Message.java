package com.capstone.cschatbot.chat.entity.gpt;

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
