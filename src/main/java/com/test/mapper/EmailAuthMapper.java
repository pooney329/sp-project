package com.test.mapper;

import java.util.Date;

public interface EmailAuthMapper {

    void insert(String email, String authCode, Date expiredAt);

    /** 미만료 + 미인증 레코드에서 코드 검증 후 verified=1 업데이트. 업데이트된 행 수 반환. */
    int verify(String email, String authCode);

    /** 해당 이메일에 verified=1 레코드가 있는지 확인. */
    int countVerified(String email);
}
