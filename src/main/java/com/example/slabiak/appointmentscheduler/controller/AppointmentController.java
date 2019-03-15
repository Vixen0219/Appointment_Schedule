package com.example.slabiak.appointmentscheduler.controller;

import com.example.slabiak.appointmentscheduler.entity.Appointment;
import com.example.slabiak.appointmentscheduler.entity.ChatMessage;
import com.example.slabiak.appointmentscheduler.security.CustomUserDetails;
import com.example.slabiak.appointmentscheduler.service.AppointmentService;
import com.example.slabiak.appointmentscheduler.service.EmailService;
import com.example.slabiak.appointmentscheduler.service.UserService;
import com.example.slabiak.appointmentscheduler.service.WorkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/appointments")
public class AppointmentController {

    @Autowired
    WorkService workService;

    @Autowired
    UserService userService;

    @Autowired
    AppointmentService appointmentService;

    @Autowired
    EmailService emailService;



    @GetMapping("")
    public String showAllAppointments(Model model, @AuthenticationPrincipal CustomUserDetails currentUser) {
        if (currentUser.hasRole("ROLE_CUSTOMER")) {
            model.addAttribute("appointments",appointmentService.findByCustomerId(currentUser.getId()));
        } else if(currentUser.hasRole("ROLE_PROVIDER")) {
            model.addAttribute("appointments",appointmentService.findByProviderId(currentUser.getId()));
        } else if(currentUser.hasRole("ROLE_ADMIN")) {
            model.addAttribute("appointments",appointmentService.findAll());
        }
        return "appointments/listAppointments";
    }

    @GetMapping("/{id}")
    public String showAppointmentDetail(@PathVariable("id") int appointmentId, Model model,@AuthenticationPrincipal CustomUserDetails currentUser) {
        Appointment appointment = appointmentService.findById(appointmentId);
        model.addAttribute("appointment", appointment);
        model.addAttribute("chatMessage", new ChatMessage());
        boolean allowDeny = appointmentService.isUserAllowedToDenyThatAppointmentTookPlace(currentUser.getId(),appointmentId);
        model.addAttribute("allowDeny",allowDeny);
        if(allowDeny){
            model.addAttribute("remainingTime", formatDuration(Duration.between(LocalDateTime.now(),appointment.getEnd().plusHours(24))));
        }
        String cancelNotAllowedReason = appointmentService.getCancelNotAllowedReason(currentUser.getId(), appointmentId);
        model.addAttribute("allowCancel", cancelNotAllowedReason == null);
        model.addAttribute("cancelNotAllowedReason", cancelNotAllowedReason);
        return "appointments/appointmentDetail";
        }


    @PostMapping("/deny")
    public String denyAppointment(@RequestParam("appointmentId") int appointmentId, @AuthenticationPrincipal CustomUserDetails currentUser, Model model) {
        int customerId = currentUser.getId();
        appointmentService.denyAppointment(appointmentId,customerId);
        return "redirect:/appointments/"+appointmentId;
    }

    @GetMapping("/deny")
    public String denyAppointment(@RequestParam("token") String token, Model model) {
        boolean result = appointmentService.denyAppointment(token);
        model.addAttribute("result",result);
        return "appointments/denyConfirmation";
    }

    @PostMapping("/messages/new")
    public String adNewChatMessage(@ModelAttribute("chatMessage") ChatMessage chatMessage, @RequestParam("appointmentId") int appointmentId, @AuthenticationPrincipal CustomUserDetails currentUser) {
        int authorId = currentUser.getId();
        appointmentService.addMessageToAppointmentChat(appointmentId,authorId, chatMessage);
        return "redirect:/appointments/"+appointmentId;
    }


    @GetMapping("/new")
  public String selectService(Model model) {
        model.addAttribute("works", workService.findAll());
        return "appointments/select-service";
    }

   @GetMapping("/new/{workId}")
    public String selectProvider(@PathVariable("workId") int workId, Model model) {
        model.addAttribute("providers", userService.findByWorks(workService.findById(workId)));
        model.addAttribute("workId",workId);
        return "appointments/select-provider";
    }

    @GetMapping("/new/{workId}/{providerId}")
    public String selectDate(@PathVariable("workId") int workId,@PathVariable("providerId") int providerId, Model model){
        model.addAttribute("providerId",providerId);
        model.addAttribute("workId",workId);
        return "appointments/select-date";
    }

    @GetMapping("/new/{workId}/{providerId}/{dateTime}")
    public String confirm(@PathVariable("workId") int workId,@PathVariable("providerId") int providerId,@PathVariable("dateTime") String dateTime,Model model){
        model.addAttribute(workId);
        model.addAttribute(providerId);
        model.addAttribute("start",dateTime);
        return "appointments/confirm";
    }

    @PostMapping("/new")
    public String saveAppointment(@RequestParam("workId") int workId,@RequestParam("providerId") int providerId,@RequestParam("start") String start, @AuthenticationPrincipal CustomUserDetails currentUser){
        int customerId= currentUser.getId();
        appointmentService.save(workId,providerId,customerId,LocalDateTime.parse(start));
        return "redirect:/customers/";
    }

    @PostMapping("/cancel")
    public String cancelAppointment(@RequestParam("appointmentId") int appointmentId, @AuthenticationPrincipal CustomUserDetails currentUser){
        appointmentService.cancelById(appointmentId,currentUser.getId());
        return "redirect:/appointments";
    }


    public static String formatDuration(Duration duration) {
        long s = duration.getSeconds();
        return   String.format("%dh%02dm", s / 3600, (s % 3600) / 60);
    }

}
