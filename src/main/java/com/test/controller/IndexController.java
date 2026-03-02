package com.test.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
public class IndexController {

    @RequestMapping("/")
    public String home() {
        return "index";
    }

    @RequestMapping("/index")
    public void aaaa(){
        if(true)throw new RuntimeException("zzzz");
        System.out.println("aaaaaaa");
    }

//    @Override
//    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
//        System.out.println("aaaaaaaaaaaaaaaaa");
//        ModelAndView mv = new ModelAndView();
//        mv.addObject("data", "Hello World!");
//        mv.setViewName("index");
//
//        return mv;
//    }
}
