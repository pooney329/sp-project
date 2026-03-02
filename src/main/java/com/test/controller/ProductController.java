package com.test.controller;

import com.test.dto.MemberDto;
import com.test.service.MemberService;
import com.test.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/product")
public class ProductController {

    private final ProductService productService;
    private final MemberService memberService;

    public ProductController(ProductService productService, MemberService memberService) {
        this.productService = productService;
        this.memberService = memberService;
    }

    @GetMapping("/list")
    public ModelAndView list() {
        ModelAndView mv = new ModelAndView("product/list");
        mv.addObject("products", productService.getProductList());
        return mv;
    }

    @GetMapping("/{id}")
    public ModelAndView detail(@PathVariable Long id, HttpServletRequest request) {
        ModelAndView mv = new ModelAndView("product/detail");
        mv.addObject("product", productService.getProduct(id));

        MemberDto loginMember = (MemberDto) request.getAttribute("loginMember");
        if (loginMember != null) {
            mv.addObject("loginMember", memberService.findById(loginMember.getId()));
        }

        return mv;
    }
}
