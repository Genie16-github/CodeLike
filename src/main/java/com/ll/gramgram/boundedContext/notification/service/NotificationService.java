package com.ll.gramgram.boundedContext.notification.service;

import com.ll.gramgram.boundedContext.instaMember.entity.InstaMember;
import com.ll.gramgram.boundedContext.likeablePerson.entity.LikeablePerson;
import com.ll.gramgram.boundedContext.notification.entity.Notification;
import com.ll.gramgram.boundedContext.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public List<Notification> findByToInstaMember(InstaMember toInstaMember) {
        return notificationRepository.findByToInstaMemberOrderByCreateDateDesc(toInstaMember);
    }

    @Transactional
    public void whenAfterLike(LikeablePerson likeablePerson) {
        Notification notification = Notification
                .builder()
                .fromInstaMember(likeablePerson.getFromInstaMember()) // 호감을 표시하는 사람의 인스타 멤버
                .toInstaMember(likeablePerson.getToInstaMember()) // 호감을 받는 사람의 인스타 멤버
                // 호감사유(1=외모, 2=능력, 3=성격)
                .newAttractiveTypeCode(likeablePerson.getAttractiveTypeCode())
                .typeCode("Like") // "Like" -> 좋아요 알림
                .build();

        notificationRepository.save(notification);
    }

    @Transactional
    public void whenAfterModifyAttractiveType(LikeablePerson likeablePerson, int oldAttractiveTypeCode) {
        Notification notification = Notification
                .builder()
                .fromInstaMember(likeablePerson.getFromInstaMember())
                .toInstaMember(likeablePerson.getToInstaMember())
                .newAttractiveTypeCode(likeablePerson.getAttractiveTypeCode())
                .oldAttractiveTypeCode(oldAttractiveTypeCode)
                .typeCode("Modify") // "Modify" -> 수정 알림
                .build();

        notificationRepository.save(notification);
    }
}
