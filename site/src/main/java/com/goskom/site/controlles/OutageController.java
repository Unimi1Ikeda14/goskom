package com.goskom.site.controlles;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.servlet.ModelAndView;

import com.goskom.site.repositories.OutageRepository;

@Controller
public class OutageController {

    @Autowired
    private OutageRepository outageRepository;

    @GetMapping("/outages")
    public ModelAndView showOutagesPage(@RequestHeader(value = "HX-Request", required = false) String hxRequest, 
                                        Model model) {

        model.addAttribute("outages", outageRepository.findAllByOrderByCreatedAtDesc());

        if (hxRequest != null) {
            return new ModelAndView("outages :: content", HttpStatus.OK);
        } else {
            model.addAttribute("page", "outages");
            return new ModelAndView("index", HttpStatus.OK);
        }
    }
}