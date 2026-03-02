package com.test.controller;

import com.test.service.NoticeService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
public class NoticeController {

    private final NoticeService noticeService;

    public NoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    @RequestMapping("/notice")
    public ModelAndView noticeList() {
        List<String> noticeList = noticeService.getNoticeList();
        noticeList.forEach(System.out::println);
        ModelAndView mv = new ModelAndView();
        mv.addObject("data", "Hello World!");
        mv.setViewName("list");
        return mv;
    }
}
