package com.ll.gramgram.boundedContext.likeablePerson.controller;

import com.ll.gramgram.base.rq.Rq;
import com.ll.gramgram.base.rsData.RsData;
import com.ll.gramgram.boundedContext.instaMember.entity.InstaMember;
import com.ll.gramgram.boundedContext.likeablePerson.entity.LikeablePerson;
import com.ll.gramgram.boundedContext.likeablePerson.service.LikeablePersonService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@Controller
@RequestMapping("/likeablePerson")
@RequiredArgsConstructor
public class LikeablePersonController {
    private final Rq rq;
    private final LikeablePersonService likeablePersonService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/add")
    public String showAdd() {
        return "usr/likeablePerson/add";
    }

    @AllArgsConstructor
    @Getter
    public static class AddForm {
        private final String username;
        private final int attractiveTypeCode;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/add")
    public String add(@Valid AddForm addForm) {
        // 기존에 호감 표시를 한 적이 있는 상대인지 확인
        RsData<LikeablePerson> overlapCheckRsData = likeablePersonService.overlapCheck(rq.getMember(), addForm.getUsername(), addForm.getAttractiveTypeCode());

        // 입력한 데이터가 로그인한 유저의 좋아요 정보 안에 있는 경우(중복 입력)
        // 'F-' 로 시작하는 메시지를 전달 받았을 경우 historyBack
        if (overlapCheckRsData.isFail()) {
            return rq.historyBack(overlapCheckRsData);
        }

        RsData<LikeablePerson> createRsData = likeablePersonService.like(rq.getMember(), addForm.getUsername(), addForm.getAttractiveTypeCode());

        if (createRsData.isFail()) {
            // 호감 목록에 10개의 데이터가 다 찬 경우
            // 호감 목록으로 이동
            if (createRsData.getResultCode().equals("F-3")) return rq.redirectWithMsg("/likeablePerson/list", createRsData);

            // 본인의 인스타 아이디가 없는 경우
            // 본인의 인스타 아이디에 호감 표시를 한 경우
            return rq.historyBack(createRsData);
        }

        return rq.redirectWithMsg("/likeablePerson/list", createRsData);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/list")
    public String showList(Model model) {
        InstaMember instaMember = rq.getMember().getInstaMember();

        // 인스타인증을 했는지 체크
        if (instaMember != null) {
            List<LikeablePerson> likeablePeople = instaMember.getFromLikeablePeople();
            model.addAttribute("likeablePeople", likeablePeople);
        }

        return "usr/likeablePerson/list";
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        // 이상한 ID 값이 들어올 수도 있다. -> null로 처리
        LikeablePerson likeablePerson = likeablePersonService.findById(id).orElse(null);

        // 삭제를 시도하는 유저가 권한이 있는지 확인. 소유권 확인
        RsData<LikeablePerson> canActorDeleteRsData = likeablePersonService.canActorDelete(rq.getMember(), likeablePerson);

        // 'F-' 로 시작하는 메시지를 전달 받았을 경우
        if (canActorDeleteRsData.isFail()) return rq.historyBack(canActorDeleteRsData);

        // likeablePerson 객체가 null 이 아닐 경우 삭제
        RsData<LikeablePerson> deleteRsData = likeablePersonService.delete(Objects.requireNonNull(likeablePerson));

        // 'S-' 로 시작하는 메시지를 전달 받지 못했을 경우
        if (deleteRsData.isFail()) return rq.historyBack(deleteRsData);

        return rq.redirectWithMsg("/likeablePerson/list", deleteRsData); // deleteRsData : "S-1", "xxx 님에 대한 호감을 취소하였습니다."
    }
}
