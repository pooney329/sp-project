package com.test.mapper;

import com.test.dto.MemberDto;

public interface MemberMapper {

    MemberDto findByEmail(String email);

    MemberDto findById(Long id);

    void insert(MemberDto member);

    void updatePointBalance(Long memberId, int pointBalance);

    /** 현재 point_balance에 delta 값을 더함 (음수이면 차감) */
    void addPointBalance(Long memberId, int delta);
}
