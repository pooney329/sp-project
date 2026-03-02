package com.test.service;

import com.test.config.exception.BusinessException;
import com.test.config.exception.UnauthorizedException;
import com.test.dto.MemberDto;
import com.test.mapper.EmailAuthMapper;
import com.test.mapper.MemberMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Random;

@Service
public class MemberService {

    private final MemberMapper memberMapper;
    private final EmailAuthMapper emailAuthMapper;
    private final EmailService emailService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public MemberService(MemberMapper memberMapper,
                         EmailAuthMapper emailAuthMapper,
                         EmailService emailService) {
        this.memberMapper = memberMapper;
        this.emailAuthMapper = emailAuthMapper;
        this.emailService = emailService;
    }

    /** OTP 발송 */
    public void sendOtp(String email) throws Exception {
        String code = String.format("%06d", new Random().nextInt(1_000_000));
        Date expiredAt = new Date(System.currentTimeMillis() + 5 * 60 * 1000L);
        emailAuthMapper.insert(email, code, expiredAt);
        emailService.sendOtp(email, code);
    }

    /** OTP 검증 */
    public void verifyOtp(String email, String code) {
        int updated = emailAuthMapper.verify(email, code);
        if (updated == 0) {
            throw new BusinessException("INVALID_CODE", "인증 코드가 올바르지 않거나 만료되었습니다.");
        }
    }

    /** 회원가입 */
    public void register(String email, String password, String name) {
        if (emailAuthMapper.countVerified(email) == 0) {
            throw new BusinessException("EMAIL_NOT_VERIFIED", "이메일 인증이 완료되지 않았습니다.");
        }
        if (memberMapper.findByEmail(email) != null) {
            throw new BusinessException("DUPLICATE_EMAIL", "이미 가입된 이메일입니다.");
        }
        MemberDto member = new MemberDto();
        member.setEmail(email);
        member.setPassword(passwordEncoder.encode(password));
        member.setName(name);
        memberMapper.insert(member);
    }

    /** 로그인 — 성공 시 MemberDto 반환, 실패 시 예외 */
    public MemberDto login(String email, String password) {
        MemberDto member = memberMapper.findByEmail(email);
        if (member == null || !passwordEncoder.matches(password, member.getPassword())) {
            throw new UnauthorizedException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }
        return member;
    }

    public MemberDto findByEmail(String email) {
        return memberMapper.findByEmail(email);
    }

    public MemberDto findById(Long id) {
        return memberMapper.findById(id);
    }
}
