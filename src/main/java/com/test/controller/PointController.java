package com.test.controller;

import com.test.config.exception.UnauthorizedException;
import com.test.dto.MemberDto;
import com.test.service.MemberService;
import com.test.service.PointService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/point")
public class PointController {

    private final PointService pointService;
    private final MemberService memberService;

    public PointController(PointService pointService, MemberService memberService) {
        this.pointService = pointService;
        this.memberService = memberService;
    }

    @GetMapping("/history")
    public ModelAndView history(HttpServletRequest request) {
        MemberDto loginMember = (MemberDto) request.getAttribute("loginMember");
        if (loginMember == null) throw new UnauthorizedException();

        MemberDto member = memberService.findById(loginMember.getId());

        ModelAndView mv = new ModelAndView("point/history");
        mv.addObject("histories", pointService.getHistory(member.getId()));
        mv.addObject("pointBalance", member.getPointBalance());
        return mv;
    }
}
