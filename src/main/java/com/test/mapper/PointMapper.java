package com.test.mapper;

import com.test.dto.PointHistoryDto;

import java.util.List;

public interface PointMapper {

    void insertHistory(PointHistoryDto history);

    List<PointHistoryDto> findByMemberId(Long memberId);
}
