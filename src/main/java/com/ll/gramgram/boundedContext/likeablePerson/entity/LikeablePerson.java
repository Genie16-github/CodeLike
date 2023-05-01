package com.ll.gramgram.boundedContext.likeablePerson.entity;

import com.ll.gramgram.base.appConfig.AppConfig;
import com.ll.gramgram.base.baseEntity.BaseEntity;
import com.ll.gramgram.base.rsData.RsData;
import com.ll.gramgram.boundedContext.instaMember.entity.InstaMember;
import com.ll.gramgram.standard.util.Ut;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static jakarta.persistence.GenerationType.IDENTITY;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@ToString
@Entity
@Getter
public class LikeablePerson extends BaseEntity {
    private LocalDateTime modifyUnlockDate;

    @ManyToOne
    @ToString.Exclude // 연관관계 속성은 출력X
    private InstaMember fromInstaMember; // 호감을 표시한 사람(인스타 멤버)
    private String fromInstaMemberUsername; // 혹시 몰라서 기록

    @ManyToOne
    @ToString.Exclude
    private InstaMember toInstaMember; // 호감을 받은 사람(인스타 멤버)
    private String toInstaMemberUsername; // 혹시 몰라서 기록
    private int attractiveTypeCode; // 매력포인트(1=외모, 2=성격, 3=능력)

    public boolean isModifyUnlocked() {
        return modifyUnlockDate.isBefore(LocalDateTime.now());
    }

    // 초 단위에서 올림 해주세요.
    public String getModifyUnlockTimeRemainStrHuman() {
        // 남은 수정 가능 시간(쿨타임) = 수정 가능 시간 - 현재 시간
        // SECONDS : 초단위로 계산 -> 변환 필요
        long modifyUnlockTimeRemain = ChronoUnit.SECONDS.between(LocalDateTime.now(), modifyUnlockDate);
        return transTimeFormat(modifyUnlockTimeRemain);
    }

    public String transTimeFormat(long time) {
        // 초단위의 시간을 시, 분으로 변환
        long hour = time / 3600; // 시
        long minute = time % 3600 / 60; // 분
        long second = time % 3600 % 60; // 초
        if (hour == 0 && minute == 0) return second + "초";

        return hour + "시간 " + minute + "분";
    }

    public RsData updateAttractionTypeCode(int attractiveTypeCode) {
        if (this.attractiveTypeCode == attractiveTypeCode) {
            return RsData.of("F-1", "이미 설정되었습니다.");
        }
        this.attractiveTypeCode = attractiveTypeCode;
        // 호감 사유 갱신하면서 수정 가능 시간도 갱신
        this.modifyUnlockDate = AppConfig.genLikeablePersonModifyUnlockDate();

        return RsData.of("S-1", "성공");
    }

    public String getAttractiveTypeDisplayName() {
        return switch (attractiveTypeCode) {
            case 1 -> "외모";
            case 2 -> "성격";
            default -> "능력";
        };
    }

    public String getAttractiveTypeDisplayNameWithIcon() {
        return switch (attractiveTypeCode) {
            case 1 -> "<i class=\"fa-regular fa-face-smile\"></i>";
            case 2 -> "<i class=\"fa-regular fa-heart\"></i>";
            default -> "<i class=\"fa-solid fa-sack-dollar\"></i>";
        } + "&nbsp;" + getAttractiveTypeDisplayName();
    }

    public String getJdenticon() {
        return Ut.hash.sha256(fromInstaMember.getId() + "_likes_" + toInstaMember.getId());
    }
}
