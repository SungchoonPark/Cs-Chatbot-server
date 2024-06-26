package com.capstone.cschatbot.selfIntro.domain;

import com.capstone.cschatbot.common.enums.CustomResponseStatus;
import com.capstone.cschatbot.common.exception.CustomException;
import com.capstone.cschatbot.common.model.BaseEntity;
import com.capstone.cschatbot.selfIntro.dto.SelfIntroChat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "self_intro")
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SelfIntro extends BaseEntity {
    @Id
    private String id;
    private String memberId;
    private Boolean terminateStatus;

    private List<SelfIntroChat> selfIntroChats = new ArrayList<>();

    public SelfIntro(String memberId) {
        this.memberId = memberId;
        terminateStatus = Boolean.FALSE;
    }

    public static SelfIntro of(String memberId) {
        return new SelfIntro(memberId);
    }

    public void addSelfIntroChat(SelfIntroChat selfIntroChat) {
        selfIntroChats.add(selfIntroChat);
    }

    public void checkEqualMember(String memberId) {
        if (this.memberId.equals(memberId)) {
            throw new CustomException(CustomResponseStatus.MEMBER_NOT_MATCH);
        }
    }

    public void terminateSelfIntroChat() {
        this.terminateStatus = Boolean.TRUE;
    }
}
