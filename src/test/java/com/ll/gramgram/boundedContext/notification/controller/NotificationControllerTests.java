package com.ll.gramgram.boundedContext.notification.controller;

import com.ll.gramgram.TestUt;
import com.ll.gramgram.boundedContext.likeablePerson.entity.LikeablePerson;
import com.ll.gramgram.boundedContext.likeablePerson.service.LikeablePersonService;
import com.ll.gramgram.boundedContext.member.entity.Member;
import com.ll.gramgram.boundedContext.member.service.MemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.MethodName.class)
public class NotificationControllerTests {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private MemberService memberService;
    @Autowired
    private LikeablePersonService likeablePersonService;

    @Test
    @DisplayName("알림 페이지")
    @WithUserDetails("user2")
    void t001() throws Exception {
        // WHEN
        ResultActions resultActions = mvc
                .perform(get("/usr/notification/list"))
                .andDo(print());
        // THEN
        resultActions
                .andExpect(handler().handlerType(NotificationController.class))
                .andExpect(handler().methodName("showList"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().string(containsString("""
                        <i class="fa-regular fa-bell"></i>
                        """.stripIndent().trim())))
                .andExpect(content().string(containsString("""
                        <i class="fa-regular fa-face-laugh"></i> 최근에 받은 알림이 없습니다.
                        """.stripIndent().trim())));
    }

    @Test
    @DisplayName("호감 표현 시 좋아요 알림 추가")
    @WithUserDetails("user2")
    void t002() throws Exception {
        Member member = memberService.findByUsername("user3").orElseThrow();
        // "user3"이 "insta_user2"에게 호감 표현
        likeablePersonService.like(member, "insta_user2", 1);

        // WHEN
        ResultActions resultActions = mvc
                .perform(get("/usr/notification/list"))
                .andDo(print());
        // THEN
        resultActions
                .andExpect(handler().handlerType(NotificationController.class))
                .andExpect(handler().methodName("showList"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().string(containsString("""
                        <i class="fa-regular fa-clock"></i>
                        """.stripIndent().trim())))
                .andExpect(content().string(containsString("""
                        <i class="fa-regular fa-clock"></i>
                        """.stripIndent().trim())))
                .andExpect(content().string(containsString("""
                        <span class="badge badge-primary">
                        """.stripIndent().trim())))
                .andExpect(content().string(containsString("""
                        어떤 <span class="badge badge-primary">
                        """.stripIndent().trim())))
                .andExpect(content().string(containsString("""
                        </span> 때문에 좋아합니다.
                        """.stripIndent().trim())));
    }

    @Test
    @DisplayName("호감사유 수정 시 수정 알림 추가")
    @WithUserDetails("user2")
    void t003() throws Exception {
        Member member = memberService.findByUsername("user3").orElseThrow();
        // "user3"이 "insta_user2"에게 호감 표현
        LikeablePerson likeablePerson = likeablePersonService.like(member, "insta_user2", 1).getData();
        // 수정 가능하도록 쿨타임 강제 변경
        TestUt.setFieldValue(likeablePerson, "modifyUnlockDate", LocalDateTime.now().minusSeconds(1));
        // "user3"이 "insta_user2"를 향한 호감사유를 변경
        likeablePersonService.modifyAttractive(member, likeablePerson, 3);

        // WHEN
        ResultActions resultActions = mvc
                .perform(get("/usr/notification/list"))
                .andDo(print());
        // THEN
        resultActions
                .andExpect(handler().handlerType(NotificationController.class))
                .andExpect(handler().methodName("showList"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().string(containsString("""
                        어떤 <span class="badge badge-primary">
                        """.stripIndent().trim())))
                .andExpect(content().string(containsString("""
                        </span> 으로 변경했습니다.
                        """.stripIndent().trim())));
    }
}
