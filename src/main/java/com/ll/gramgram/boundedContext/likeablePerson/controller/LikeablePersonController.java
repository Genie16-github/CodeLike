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
        RsData<LikeablePerson> createRsData = likeablePersonService.like(rq.getMember(), addForm.getUsername(), addForm.getAttractiveTypeCode());

        if (createRsData.isFail()) {
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
        RsData<LikeablePerson> deleteRs = likeablePersonService.delete(Objects.requireNonNull(likeablePerson));

        // 'S-' 로 시작하는 메시지를 전달 받지 못했을 경우
        if (deleteRs.isFail()) return rq.historyBack(deleteRs);

        return rq.redirectWithMsg("/likeablePerson/list", deleteRs); // deleteRs : "S-1", "xxx 님에 대한 호감을 취소하였습니다."
    }
}
