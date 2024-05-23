package com.capstone.cschatbot.common.domain;

import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Persistable;

import java.time.LocalDateTime;

@Getter
public abstract class BaseEntity implements Persistable<String> {
    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime modifiedAt;

    @Override
    public boolean isNew() {
        return createdAt == null;
    }
}
