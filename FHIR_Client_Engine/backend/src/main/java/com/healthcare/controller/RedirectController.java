package com.healthcare.controller;

import com.healthcare.constants.Constants;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping(value = "/callback")
public class RedirectController {

    @GetMapping(value = "")
    public ModelAndView redirectUser(@RequestParam(value = "code") String code){
        Constants.code = code;
        return new ModelAndView("redirect:http://localhost:4200/sign-in?code="+code);
    }
}
