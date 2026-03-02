package com.test.service;

import com.test.mapper.NoticeMapper;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class NoticeService {
    private final NoticeMapper noticeMapper;

    public NoticeService(NoticeMapper noticeMapper) {
        this.noticeMapper = noticeMapper;
    }

    public List<String> getNoticeList() {
        return noticeMapper.getNoticeList();
    }
}
