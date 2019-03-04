package com.example.slabiak.appointmentscheduler.controller;

import com.example.slabiak.appointmentscheduler.entity.Appointment;
import com.example.slabiak.appointmentscheduler.model.AppointmentRegisterForm;
import com.example.slabiak.appointmentscheduler.service.AppointmentService;
import com.example.slabiak.appointmentscheduler.service.UserService;
import com.example.slabiak.appointmentscheduler.service.WorkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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

    @GetMapping("")
    public String showAllAppointments(Model model, Authentication authentication) {
        model.addAttribute("user",userService.findByUserName(authentication.getName()));
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_CUSTOMER"))) {
            return "appointments/customer-appointments";
        } else if(authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_PROVIDER")))
        return "appointments/provider-appointments";
        else{
            return "home";
        }
    }

    @GetMapping("/{id}")
    public String showAppointmentDetail(@PathVariable("id") int id, Model model, Authentication authentication) {
        model.addAttribute("appointment", appointmentService.findById(id));
            return "appointments/appointmentDetail";
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
    public String saveAppointment(@RequestParam("workId") int workId,@RequestParam("providerId") int providerId,@RequestParam("start") String start, Authentication authentication){
        int customerId= userService.findByUserName(authentication.getName()).getId();
        appointmentService.save(workId,providerId,customerId,LocalDateTime.parse(start));
        return "redirect:/customers/";
    }

    @PostMapping("/cancel")
    public String cancelAppointment(@RequestParam("id") int id, Authentication authentication){
        appointmentService.deleteById(id);
        return "redirect:/appointments";
    }

}
