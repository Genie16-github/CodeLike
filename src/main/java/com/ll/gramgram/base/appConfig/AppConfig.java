package com.ll.gramgram.base.appConfig;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    @Getter
    private static long likeablePersonFromMax;

    @Value("${custom.likeablePerson.max}")
    public void setLikeablePersonFromMax(long likeablePersonFromMax) {
        AppConfig.likeablePersonFromMax = likeablePersonFromMax;
    }

    @Getter
    private static long likeablePersonModifyCoolTime;

    @Value("${custom.likeablePerson.modifyCoolTime}")
    public void setLikeablePersonModifyCoolTime(long likeablePersonModifyCoolTime) {
        AppConfig.likeablePersonModifyCoolTime = likeablePersonModifyCoolTime;
    }

    @Getter
    private static long likeablePersonCancelCoolTime;

    @Value("${custom.likeablePerson.cancelCoolTime}")
    public void setLikeablePersonCancelCoolTime(long likeablePersonCancelCoolTime) {
        AppConfig.likeablePersonCancelCoolTime = likeablePersonCancelCoolTime;
    }
}
