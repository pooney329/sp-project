package com.test.controller;

import com.test.config.exception.UnauthorizedException;
import com.test.dto.MemberDto;
import com.test.dto.OrderDto;
import com.test.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/create")
    public String createOrder(@RequestParam Long productId,
                              @RequestParam int quantity,
                              @RequestParam(defaultValue = "0") int pointUsed,
                              HttpServletRequest request) {
        MemberDto member = (MemberDto) request.getAttribute("loginMember");
        if (member == null) throw new UnauthorizedException();

        OrderDto.OrderItemDto item = new OrderDto.OrderItemDto();
        item.setProductId(productId);
        item.setQuantity(quantity);

        List<OrderDto.OrderItemDto> items = new ArrayList<>();
        items.add(item);

        Long orderId = orderService.createOrder(member.getId(), items, pointUsed);
        return "redirect:/order/" + orderId;
    }

    @GetMapping("/list")
    public ModelAndView orderList(HttpServletRequest request) {
        MemberDto member = (MemberDto) request.getAttribute("loginMember");
        if (member == null) throw new UnauthorizedException();

        ModelAndView mv = new ModelAndView("order/list");
        mv.addObject("orders", orderService.getOrderList(member.getId()));
        return mv;
    }

    @GetMapping("/{id}")
    public ModelAndView orderDetail(@PathVariable Long id, HttpServletRequest request) {
        MemberDto member = (MemberDto) request.getAttribute("loginMember");
        if (member == null) throw new UnauthorizedException();

        ModelAndView mv = new ModelAndView("order/detail");
        mv.addObject("order", orderService.getOrder(id, member.getId()));
        return mv;
    }
}
