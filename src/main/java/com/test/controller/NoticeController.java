package com.test.controller;

import com.test.service.NoticeService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

public class NoticeController implements Controller {
    private NoticeService noticeService;
    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {

        List<String> noticeList = noticeService.getNoticeList();
        noticeList.forEach(System.out::println);
        ModelAndView mv = new ModelAndView();
        mv.addObject("data", "Hello World!");
        mv.setViewName("list");
        return mv;
    }

    public void setNoticeService(NoticeService noticeService) {
        this.noticeService = noticeService;
    }
}
