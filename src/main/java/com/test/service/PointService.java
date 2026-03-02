package com.test.service;

import com.test.dto.PointHistoryDto;
import com.test.mapper.PointMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PointService {

    private final PointMapper pointMapper;

    public PointService(PointMapper pointMapper) {
        this.pointMapper = pointMapper;
    }

    public List<PointHistoryDto> getHistory(Long memberId) {
        return pointMapper.findByMemberId(memberId);
    }
}
