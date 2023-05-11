package com.ll.gramgram.boundedContext.likeablePerson.controller;

import com.ll.gramgram.base.rq.Rq;
import com.ll.gramgram.base.rsData.RsData;
import com.ll.gramgram.boundedContext.instaMember.entity.InstaMember;
import com.ll.gramgram.boundedContext.likeablePerson.entity.LikeablePerson;
import com.ll.gramgram.boundedContext.likeablePerson.service.LikeablePersonService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
@RequestMapping("/usr/likeablePerson")
@RequiredArgsConstructor
public class LikeablePersonController {
    private final Rq rq;
    private final LikeablePersonService likeablePersonService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/like")
    public String showLike() {
        return "usr/likeablePerson/like";
    }

    @AllArgsConstructor
    @Getter
    public static class LikeForm {
        @NotBlank
        @Size(min = 3, max = 30)
        private final String username;
        @NotNull
        @Min(1)
        @Max(3)
        private final int attractiveTypeCode;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/like")
    public String like(@Valid LikeForm likeForm) {
        RsData<LikeablePerson> rsData = likeablePersonService.like(rq.getMember(), likeForm.getUsername(), likeForm.getAttractiveTypeCode());

        if (rsData.isFail()) {
            return rq.historyBack(rsData);
        }

        return rq.redirectWithMsg("/usr/likeablePerson/list", rsData);
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
    public String cancel(@PathVariable Long id) {
        // 이상한 ID 값이 들어올 수도 있다. -> null로 처리
        LikeablePerson likeablePerson = likeablePersonService.findById(id).orElse(null);

        // 삭제를 시도하는 유저가 권한이 있는지 확인. 소유권 확인
        RsData<LikeablePerson> canDeleteRsData = likeablePersonService.canActorCancel(rq.getMember(), likeablePerson);

        // 'F-' 로 시작하는 메시지를 전달 받았을 경우
        if (canDeleteRsData.isFail()) return rq.historyBack(canDeleteRsData);

        // likeablePerson 객체가 null 이 아닐 경우 삭제
        RsData<LikeablePerson> deleteRsData = likeablePersonService.cancel(Objects.requireNonNull(likeablePerson));

        // 'S-' 로 시작하는 메시지를 전달 받지 못했을 경우
        if (deleteRsData.isFail()) return rq.historyBack(deleteRsData);

        return rq.redirectWithMsg("/usr/likeablePerson/list", deleteRsData); // deleteRsData : "S-1", "xxx 님에 대한 호감을 취소하였습니다."
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/modify/{id}")
    public String showModify(@PathVariable Long id, Model model) {
        LikeablePerson likeablePerson = likeablePersonService.findById(id).orElseThrow();

        RsData canModifyRsData = likeablePersonService.canModify(rq.getMember(), likeablePerson);

        if (canModifyRsData.isFail()) return rq.historyBack(canModifyRsData);

        model.addAttribute("likeablePerson", likeablePerson);

        return "usr/likeablePerson/modify";
    }

    @AllArgsConstructor
    @Getter
    public static class ModifyForm {
        @NotNull
        @Min(1)
        @Max(3)
        private final int attractiveTypeCode;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/modify/{id}")
    public String modify(@PathVariable Long id, @Valid ModifyForm modifyForm) {
        RsData<LikeablePerson> rsData = likeablePersonService.modifyAttractive(rq.getMember(), id, modifyForm.getAttractiveTypeCode());

        if (rsData.isFail()) {
            return rq.historyBack(rsData);
        }

        return rq.redirectWithMsg("/usr/likeablePerson/list", rsData);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/toList")
    public String showToList(Model model, String gender,
                             @RequestParam(defaultValue = "0") int attractiveTypeCode,
                             @RequestParam(defaultValue = "1") int sortCode) {
        InstaMember instaMember = rq.getMember().getInstaMember();

        // 인스타인증을 했는지 체크
        if (instaMember != null) {
            // 해당 인스타회원이 좋아하는 사람들 목록
            Stream<LikeablePerson> likeablePeopleStream = instaMember.getToLikeablePeople().stream();

            if (gender != null && !gender.equals("")) { // 성별에 따라 필터링
                likeablePeopleStream = likeablePeopleStream.filter(
                        e -> Objects.equals(e.getFromInstaMember().getGender(), gender
                ));
            }

            if (attractiveTypeCode != 0) { // 호감사유에 따라 필터링
                likeablePeopleStream = likeablePeopleStream.filter(
                        e -> e.getAttractiveTypeCode() == attractiveTypeCode
                );
            }

            switch (sortCode) {
                case 1:
                    // 최신 순 정렬
                    likeablePeopleStream = likeablePeopleStream.sorted(
                            (lp2, lp1) -> lp1.getCreateDate().compareTo(lp2.getCreateDate())
                    );
                    break;
                case 2:
                    // 날짜 순 정렬(오래 전에 받은 호감표시를 우선적으로 표시)
                    likeablePeopleStream = likeablePeopleStream.sorted(
                            (lp2, lp1) -> lp1.getCreateDate().compareTo(lp2.getCreateDate()) * -1
                    );
                    break;
                case 3:
                    // 인기 많은 순 정렬(인기가 많은 사람의 호감표시를 우선적으로 표시)
                    likeablePeopleStream = likeablePeopleStream.sorted(
                            (lp2, lp1) -> lp1.getFromInstaMember().getToLikeablePeople().size() - lp2.getFromInstaMember().getToLikeablePeople().size()
                    );
                    break;
                case 4:
                    // 인기 적은 순 정렬
                    likeablePeopleStream = likeablePeopleStream.sorted(
                            (lp2, lp1) -> (lp1.getFromInstaMember().getToLikeablePeople().size() - lp2.getFromInstaMember().getToLikeablePeople().size()) * -1
                    );
                    break;
                case 5:
                    // 성별에 따른 정렬(여성에게 받은 호감표시를 먼저), 2순위 정렬 조건은 최신순
                    likeablePeopleStream = likeablePeopleStream.sorted(
                            (lp2, lp1) -> {
                                if (lp2.getFromInstaMember().getGender().equals(lp1.getFromInstaMember().getGender())){
                                    return lp1.getCreateDate().compareTo(lp2.getCreateDate());
                                }
                                return lp1.getFromInstaMember().getGender().compareTo(lp2.getFromInstaMember().getGender());
                            }
                    );
                    break;
                case 6:
                    // 호감사유에 따른 정렬(외모, 성격, 능력 순), 2순위 정렬 조건은 최신순
                    likeablePeopleStream = likeablePeopleStream.sorted(
                            Comparator.comparing(LikeablePerson::getAttractiveTypeCode)
                                    .thenComparing((lp2, lp1) -> lp1.getCreateDate().compareTo(lp2.getCreateDate()))
                    );
                    break;
            }

            List<LikeablePerson> likeablePeople = likeablePeopleStream.collect(Collectors.toList());

            model.addAttribute("likeablePeople", likeablePeople);
        }

        return "usr/likeablePerson/toList";
    }
}
