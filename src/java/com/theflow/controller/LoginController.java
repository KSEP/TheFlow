/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.theflow.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 * @author Stas
 */

@Controller
public class LoginController {
    
    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public ModelAndView showLoginPage(@RequestParam(value = "error",
            required = false) String error, @RequestParam(value = "logout",
                    required = false) String logout) {
        ModelAndView model = new ModelAndView();
        model.setViewName("login");

        return model;
    }
    
    @RequestMapping(value = "home", method = RequestMethod.GET)
    public String redirect() {

        return "redirect:/pages/home.html";
    }
}
