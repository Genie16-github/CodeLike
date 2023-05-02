package com.ll.gramgram.boundedContext.notification.entity;

import com.ll.gramgram.base.baseEntity.BaseEntity;
import com.ll.gramgram.boundedContext.instaMember.entity.InstaMember;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.ManyToOne;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@ToString
@Entity
@Getter
public class Notification extends BaseEntity {
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime readDate;
    @ManyToOne
    @ToString.Exclude
    private InstaMember toInstaMember; // 메세지 받는 사람(호감 받는 사람)
    @ManyToOne
    @ToString.Exclude
    private InstaMember fromInstaMember; // 메세지를 발생시킨 행위를 한 사람(호감표시한 사람)
    private String typeCode; // 호감표시=Like, 호감사유변경=ModifyAttractiveType
    private String oldGender; // 해당사항 없으면 null
    private int oldAttractiveTypeCode; // 해당사항 없으면 0
    private String newGender; // 해당사항 없으면 null
    private int newAttractiveTypeCode; // 해당사항 없으면 0

    public String getAttractiveTypeDisplayName(int attractiveTypeCode) {
        return switch (attractiveTypeCode) {
            case 1 -> "외모";
            case 2 -> "성격";
            default -> "능력";
        };
    }

    public String getAfterAddNotification() {
        long diff = ChronoUnit.SECONDS.between(getCreateDate(), LocalDateTime.now());
        if (diff < 60) return diff + "초";
        else if (diff < 3600) {
            return (diff / 60) + "분";
        }
        else if (diff < 86400) {
            return (diff / 60 / 60) + "시간";
        }
        else return (diff / 60/ 60/ 24) + "일";
    }

    public void setAfterReadNotification(LocalDateTime localDateTime) {
        this.readDate = localDateTime;
    }
}
