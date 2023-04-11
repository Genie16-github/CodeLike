package com.ll.gramgram.boundedContext.likeablePerson.service;

import com.ll.gramgram.boundedContext.likeablePerson.entity.LikeablePerson;
import com.ll.gramgram.boundedContext.member.entity.Member;
import com.ll.gramgram.boundedContext.member.service.MemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class LikeablePersonServiceTests {
    @Autowired
    private LikeablePersonService likeablePersonService;
    @Autowired
    private MemberService memberService;

    @Test
    @DisplayName("같은 호감 상대 중복 입력 X")
    void t003() throws Exception {
        // ID가 user3인 Member 의 정보를 가져온다.
        Member member = memberService.findByUsername("user3").get();

        // user3이 insta_user4에게 attractiveTypeCode를 1로해서 호감표시를 한다.
        // 기존에 user3의 좋아요 정보에 있는 데이터기 때문에 추가가 되면 안된다.
        likeablePersonService.like(member, "insta_user4", 1);

        // 호감 표시 후의 user3의 좋아요 정보를 가져온다.
        List<LikeablePerson> list = member.getInstaMember().getFromLikeablePeople();

        // 호감 표시 전과 후의 좋아요 리스트의 크기가 일치해야한다.
        // 중복된 데이터는 추가가 안되었기 때문
        assertThat(list.size()).isEqualTo(2);
    }

    @Test
    @DisplayName("호감 목록 11개 이상 추가X")
    void t004() throws Exception {
        // ID가 user4인 Member 의 정보를 가져온다.
        Member member = memberService.findByUsername("user4").get();

        // 호감 상대 10명 추가
        for (int i = 0; i < 10; i ++){
            likeablePersonService.like(member, "insta_test" + i, 1);
        }

        // 호감 상대 1명 더 추가 -> 데이터가 삽입되면 안된다.
        likeablePersonService.like(member, "insta_test11", 1);

        // user4의 좋아요 정보를 가져온다.
        List<LikeablePerson> list = member.getInstaMember().getFromLikeablePeople();

        // 11번째 데이터는 추가되지 않아서 좋아요 리스트의 크기는 10이 돼야한다.
        assertThat(list.size()).isEqualTo(10);
    }

    @Test
    @DisplayName("매력 포인트를 다르게 입력하면 기존 데이터 수정")
    void t005() throws Exception {
        // ID가 user3인 Member 의 정보를 가져온다.
        Member member = memberService.findByUsername("user3").get();

        // user3이 insta_user4에게 attractiveTypeCode를 2로해서 호감표시를 한다.
        likeablePersonService.like(member, "insta_user4", 2);

        // 호감 표시 후의 user3의 좋아요 정보를 가져온다.
        List<LikeablePerson> list = likeablePersonService.findByFromInstaMemberId(member.getInstaMember().getId());

        // 좋아요 정보를 탐색하면서 toInstaMemberUsername이 insta_user4인 데이터를 찾는다.
        // 해당 데이터의 매력포인트가 2(성격)인지 확인한다.
        for (LikeablePerson likeablePerson : list){
            if (likeablePerson.getToInstaMemberUsername().equals("insta_user4")){
                assertThat(likeablePerson.getAttractiveTypeCode()).isEqualTo(2);
            }
        }
    }
}
