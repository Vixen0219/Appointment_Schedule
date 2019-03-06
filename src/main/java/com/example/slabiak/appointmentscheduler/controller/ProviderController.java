package com.example.slabiak.appointmentscheduler.controller;

import com.example.slabiak.appointmentscheduler.dao.WorkingPlanRepository;
import com.example.slabiak.appointmentscheduler.entity.User;
import com.example.slabiak.appointmentscheduler.entity.WorkingPlan;
import com.example.slabiak.appointmentscheduler.model.DayPlan;
import com.example.slabiak.appointmentscheduler.model.TimePeroid;
import com.example.slabiak.appointmentscheduler.model.UserRegisterForm;
import com.example.slabiak.appointmentscheduler.service.UserService;
import com.example.slabiak.appointmentscheduler.service.WorkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/providers")
public class ProviderController {

    @Autowired
    private UserService userService;

    @Autowired
    private WorkService workService;

    @Autowired
    private WorkingPlanRepository workingPlanRepository;

    @GetMapping("")
    public String showAllProviders(Model model) {
        model.addAttribute("providers", userService.findByRoleName("ROLE_PROVIDER"));
        return "providers/list";
    }


    @GetMapping("/new")
    public String showProviderRegistrationForm(Model model) {
        model.addAttribute("user", new UserRegisterForm());
        model.addAttribute("works", workService.findAll());
        return "providers/createProviderForm";
    }

    @PostMapping("/new")
    public String processProviderRegistration(@ModelAttribute("user") UserRegisterForm userForm, Model model) {
        User existing = userService.findByUserName(userForm.getUserName());
        if (existing != null){
            model.addAttribute("user", userForm);
            model.addAttribute("registrationError", "User name already exists.");
            return "providers/createProviderForm";
        }
        userService.registerProvider(userForm);
        return "redirect:/providers";
    }

    @GetMapping("/{id}")
    public String showProviderDetails(@PathVariable("id") int id, Model model) {
        model.addAttribute("provider", userService.findById(id));
        model.addAttribute("allWorks", workService.findAll());
        model.addAttribute("plan",userService.findById(id).getWorkingPlan());
        return "providers/updateProviderForm";
    }

    @PostMapping("/update")
    public String processProviderUpdate(@ModelAttribute("user") User userUpdateData, Model model) {
        userService.updateProvider(userUpdateData);
        return "redirect:/providers";
    }

    @PostMapping("/delete")
    public String deleteProvider(@RequestParam("providerId") int providerId){
        userService.deleteById(providerId);
        return "redirect:/providers";
    }

    @GetMapping("/availability")
    public String showAvailability(Model model, Authentication authentication){
        model.addAttribute("plan",userService.findByUserName(authentication.getName()).getWorkingPlan());
        model.addAttribute("break1", new TimePeroid());
        return "providers/showOrUpdateAvailability";
    }

    @PostMapping("/availability")
    public String updateAvailability(@ModelAttribute("plan") WorkingPlan plan){
        WorkingPlan wp = workingPlanRepository.getOne(plan.getId());
        wp.getMonday().setWorkingHours(plan.getMonday().getWorkingHours());
        wp.getTuesday().setWorkingHours(plan.getTuesday().getWorkingHours());
        wp.getWednesday().setWorkingHours(plan.getWednesday().getWorkingHours());
        wp.getThursday().setWorkingHours(plan.getThursday().getWorkingHours());
        wp.getFriday().setWorkingHours(plan.getFriday().getWorkingHours());
        wp.getSaturday().setWorkingHours(plan.getSaturday().getWorkingHours());
        wp.getSunday().setWorkingHours(plan.getSunday().getWorkingHours());
        workingPlanRepository.save(wp);

        return "redirect:/providers/availability";
    }

    @PostMapping("/availability/breakes/add")
    public String addBreak(@ModelAttribute("break1") TimePeroid break1,@RequestParam("planId") int planId,@RequestParam("day") String day ){
        WorkingPlan wp = workingPlanRepository.getOne(planId);
        wp.getDay(day).getBreaks().add(break1);
        workingPlanRepository.save(wp);
        return "redirect:/providers/availability";
    }

    @PostMapping("/availability/breakes/delete")
    public String deleteBreak(@ModelAttribute("break1") TimePeroid break1,@RequestParam("planId") int planId,@RequestParam("day") String day ){
        WorkingPlan wp = workingPlanRepository.getOne(planId);
        wp.getDay(day).getBreaks().remove(break1);
        workingPlanRepository.save(wp);
        return "redirect:/providers/availability";
    }




}
