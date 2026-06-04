package com.goskom.site.controlles;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handle404(HttpServletRequest request, NoHandlerFoundException ex) {
        String hxRequest = request.getHeader("HX-Request");
        
        if (hxRequest != null) {
            // Если запрос пришел изнутри личного кабинета через HTMX
            return new ModelAndView("404 :: content", HttpStatus.NOT_FOUND);
        }
        
        // Если вбили руками в браузер: берем твой главный index.html
        ModelAndView modelAndView = new ModelAndView("index", HttpStatus.NOT_FOUND);
        // Говорим Thymeleaf вставить в <main> именно шаблон 404
        modelAndView.addObject("page", "404"); 
        return modelAndView;
    }
}