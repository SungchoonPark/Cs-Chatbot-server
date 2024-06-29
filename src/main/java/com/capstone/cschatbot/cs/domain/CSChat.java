package com.capstone.cschatbot.cs.domain;

import com.capstone.cschatbot.common.model.BaseEntity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "cs_chat")
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CSChat extends BaseEntity {
    @Id
    private String id;
    private String memberId;
    private String topic;
    private Boolean terminateStatus;
    private List<ChatEvaluation> chatHistory = new ArrayList<>();

    private CSChat(String memberId, String topic) {
        this.memberId = memberId;
        this.topic = topic;
        this.terminateStatus = Boolean.FALSE;
    }

    public static CSChat of(String memberId, String topic) {
        return new CSChat(memberId, topic);
    }

    public void terminateProcess(List<ChatEvaluation> evaluations) {
        updateChatHistory(evaluations);
        terminateCsChat();
    }

    private void updateChatHistory(List<ChatEvaluation> evaluations) {
        this.chatHistory = evaluations;
    }

    private void terminateCsChat() {
        this.terminateStatus = Boolean.TRUE;
    }
}
