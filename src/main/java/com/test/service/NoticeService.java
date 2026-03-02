package com.test.service;

import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;


@Service
public class NoticeService {
    private final DataSource dataSource;

    public NoticeService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<String> getNoticeList() {
        System.out.println(dataSource);
        List<String> list = new ArrayList<>();
        list.add("aaaa");
        list.add("bbbb");
        return list;
    }
}
