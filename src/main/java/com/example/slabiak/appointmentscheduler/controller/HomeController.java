package com.example.slabiak.appointmentscheduler.controller;

import com.example.slabiak.appointmentscheduler.security.CustomUserDetails;
import com.example.slabiak.appointmentscheduler.service.AppointmentService;
import com.example.slabiak.appointmentscheduler.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @Autowired
    UserService userService;

    @Autowired
    AppointmentService appointmentService;

    @GetMapping("/")
    public String showHome(Model model,@AuthenticationPrincipal CustomUserDetails currentUser) {
        appointmentService.updateUserAppointmentsStatuses(currentUser.getId());
        model.addAttribute("user",userService.findById(currentUser.getId()));
        return "home";
    }

    @GetMapping("/login")
    public String login(Model model,@AuthenticationPrincipal CustomUserDetails currentUser) {
        if(currentUser !=null){
             return "redirect:/";
        }
        return "login";
    }

    @GetMapping("/access-denied")
    public String showAccessDenied() {
        return "access-denied";
    }

}
