package com.ll.gramgram.boundedContext.notification.service;

import com.ll.gramgram.TestUt;
import com.ll.gramgram.boundedContext.likeablePerson.entity.LikeablePerson;
import com.ll.gramgram.boundedContext.likeablePerson.service.LikeablePersonService;
import com.ll.gramgram.boundedContext.member.entity.Member;
import com.ll.gramgram.boundedContext.member.service.MemberService;
import com.ll.gramgram.boundedContext.notification.entity.Notification;
import com.ll.gramgram.boundedContext.notification.repository.NotificationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.MethodName.class)
public class NotificationServiceTests {
    @Autowired
    private MemberService memberService;
    @Autowired
    private LikeablePersonService likeablePersonService;
    @Autowired
    private NotificationRepository notificationRepository;

    @Test
    @DisplayName("호감표현 시 좋아요 알림 추가")
    void t001() throws Exception {
        Member member1 = memberService.findByUsername("user3").orElseThrow();
        Member member2 = memberService.findByUsername("user2").orElseThrow();

        // 호감 표시 하기 이전의 "insta_user2"의 알림 리스트
        List<Notification> notificationList1 = notificationRepository.findByToInstaMember(member2.getInstaMember());

        // "user3"이 "insta_user2"에게 호감 표현
        likeablePersonService.like(member1, "insta_user2", 1);

        // 호감 표시 한 후의 "insta_user2"의 알림 리스트
        List<Notification> notificationList2 = notificationRepository.findByToInstaMember(member2.getInstaMember());

        // 호감 표시 이전의 리스트 사이즈에 +1한 값과 같아야한다.
        assertThat(notificationList2.size()).isEqualTo(notificationList1.size() + 1);
    }

    @Test
    @DisplayName("호감사유 수정 시 수정 알림 추가")
    void t002() throws Exception {
        Member member1 = memberService.findByUsername("user3").orElseThrow();
        Member member2 = memberService.findByUsername("user2").orElseThrow();

        // "user3"이 "insta_user2"에게 호감 표현
        LikeablePerson likeablePersonToUser2 = likeablePersonService.like(member1, "insta_user2", 3).getData();

        // 호감사유 수정 이전의 "insta_user2"의 알림 리스트
        List<Notification> notificationList1 = notificationRepository.findByToInstaMember(member2.getInstaMember());

        // 바로 수정 가능하도록 쿨타임을 지난 것으로 강제 설정
        TestUt.setFieldValue(likeablePersonToUser2, "modifyUnlockDate", LocalDateTime.now().minusSeconds(1));
        // "user3"이 "insta_user2"를 향한 호감 사유를 수정
        likeablePersonService.modifyAttractive(member1, likeablePersonToUser2, 1);

        // 호감사유 수정 후의 "insta_user2"의 알림 리스트
        List<Notification> notificationList2 = notificationRepository.findByToInstaMember(member2.getInstaMember());

        // 호감사유 수정 이전의 리스트 사이즈에 +1한 값과 같아야한다.
        assertThat(notificationList2.size()).isEqualTo(notificationList1.size() + 1);
    }
}
