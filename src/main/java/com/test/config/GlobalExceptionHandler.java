package com.test.config;

import com.test.config.exception.BusinessException;
import com.test.config.exception.NotFoundException;
import com.test.config.exception.UnauthorizedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import java.util.logging.Level;
import java.util.logging.Logger;

@ControllerAdvice
public class GlobalExceptionHandler {

    private final Logger logger = Logger.getLogger(GlobalExceptionHandler.class.getName());

    @ExceptionHandler(NotFoundException.class)
    public ModelAndView handleNotFoundException(NotFoundException e) {
        logger.log(Level.WARNING, "NotFoundException: " + e.getMessage());
        ModelAndView mv = new ModelAndView("error");
        mv.addObject("statusCode", 404);
        mv.addObject("errorCode", e.getCode());
        mv.addObject("errorMessage", e.getMessage());
        return mv;
    }

    @ExceptionHandler(UnauthorizedException.class)
    public String handleUnauthorizedException(UnauthorizedException e) {
        logger.log(Level.WARNING, "UnauthorizedException: " + e.getMessage());
        return "redirect:/member/login";
    }

    @ExceptionHandler(BusinessException.class)
    public ModelAndView handleBusinessException(BusinessException e) {
        logger.log(Level.WARNING, "BusinessException: [" + e.getCode() + "] " + e.getMessage());
        ModelAndView mv = new ModelAndView("error");
        mv.addObject("statusCode", 400);
        mv.addObject("errorCode", e.getCode());
        mv.addObject("errorMessage", e.getMessage());
        return mv;
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleException(Exception e) {
        logger.log(Level.SEVERE, "Unhandled exception", e);
        ModelAndView mv = new ModelAndView("error");
        mv.addObject("statusCode", 500);
        mv.addObject("errorCode", "INTERNAL_ERROR");
        mv.addObject("errorMessage", "서버 내부 오류가 발생했습니다.");
        return mv;
    }
}
