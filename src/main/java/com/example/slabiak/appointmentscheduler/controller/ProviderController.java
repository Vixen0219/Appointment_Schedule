package com.example.slabiak.appointmentscheduler.controller;

import com.example.slabiak.appointmentscheduler.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/providers")
public class ProviderController {

    @Autowired
    UserService userService;

    @GetMapping("/")
    public String showProvidersHome(Model model, Authentication authentication){
        model.addAttribute("provider", userService.findByUserName(authentication.getName()));
        return "providers/provider";
    }
}
