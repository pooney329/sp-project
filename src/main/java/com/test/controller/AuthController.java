package com.test.controller;

import com.test.service.MemberService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final MemberService memberService;

    public AuthController(MemberService memberService) {
        this.memberService = memberService;
    }

    /** OTP 발송 */
    @PostMapping("/send-code")
    public String sendCode(@RequestParam String email,
                           RedirectAttributes redirectAttributes) throws Exception {
        memberService.sendOtp(email);
        redirectAttributes.addFlashAttribute("message", "인증 코드를 발송했습니다. 이메일을 확인해 주세요.");
        redirectAttributes.addFlashAttribute("email", email);
        return "redirect:/member/register";
    }

    /** OTP 검증 */
    @PostMapping("/verify-code")
    public String verifyCode(@RequestParam String email,
                             @RequestParam String code,
                             RedirectAttributes redirectAttributes) {
        memberService.verifyOtp(email, code);
        redirectAttributes.addFlashAttribute("verified", true);
        redirectAttributes.addFlashAttribute("email", email);
        return "redirect:/member/register";
    }
}
