package com.capstone.cschatbot.chat.entity;

import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "self_intro")
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SelfIntro {
    @Id
    private String id;

    private String memberId;

    private LocalDateTime createdAt;

    private Boolean terminateStatus;

    private List<SelfIntroChat> selfIntroChats = new ArrayList<>();

    public SelfIntro(String memberId) {
        this.memberId = memberId;
        this.createdAt = LocalDateTime.now();
        terminateStatus = Boolean.FALSE;
    }

    public static SelfIntro of(String memberId) {
        return new SelfIntro(memberId);
    }

    public void addSelfIntroChat(SelfIntroChat selfIntroChat) {
        selfIntroChats.add(selfIntroChat);
    }

    public void terminateSelfIntroChat() {
        this.terminateStatus = Boolean.TRUE;
    }
}
