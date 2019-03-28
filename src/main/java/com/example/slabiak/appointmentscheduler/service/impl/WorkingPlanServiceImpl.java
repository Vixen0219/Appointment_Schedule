package com.example.slabiak.appointmentscheduler.service.impl;

import com.example.slabiak.appointmentscheduler.dao.WorkingPlanRepository;
import com.example.slabiak.appointmentscheduler.entity.WorkingPlan;
import com.example.slabiak.appointmentscheduler.model.DayPlan;
import com.example.slabiak.appointmentscheduler.model.TimePeroid;
import com.example.slabiak.appointmentscheduler.security.CustomUserDetails;
import com.example.slabiak.appointmentscheduler.service.WorkingPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalTime;

@Service
public class WorkingPlanServiceImpl implements WorkingPlanService {

    @Autowired
    private WorkingPlanRepository workingPlanRepository;


    @Override
    @PreAuthorize("#updateData.provider.id == principal.id")
    public void updateWorkingPlan(WorkingPlan updateData) {
        WorkingPlan workingPlan = workingPlanRepository.getOne(updateData.getId());
        workingPlan.getMonday().setWorkingHours(updateData.getMonday().getWorkingHours());
        workingPlan.getTuesday().setWorkingHours(updateData.getTuesday().getWorkingHours());
        workingPlan.getWednesday().setWorkingHours(updateData.getWednesday().getWorkingHours());
        workingPlan.getThursday().setWorkingHours(updateData.getThursday().getWorkingHours());
        workingPlan.getFriday().setWorkingHours(updateData.getFriday().getWorkingHours());
        workingPlan.getSaturday().setWorkingHours(updateData.getSaturday().getWorkingHours());
        workingPlan.getSunday().setWorkingHours(updateData.getSunday().getWorkingHours());
        workingPlanRepository.save(workingPlan);
    }

    @Override
    public void addBreakToWorkingPlan(TimePeroid breakToAdd, int planId, String dayOfWeek) {
        CustomUserDetails currentUser =(CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        WorkingPlan workingPlan = workingPlanRepository.getOne(planId);
        if(workingPlan.getProvider().getId()!=currentUser.getId()){
            throw new org.springframework.security.access.AccessDeniedException("Unauthorized");
        }
        workingPlan.getDay(dayOfWeek).getBreaks().add(breakToAdd);
        workingPlanRepository.save(workingPlan);
    }

    @Override
    public void deleteBreakFromWorkingPlan(TimePeroid breakToDelete, int planId, String dayOfWeek) {
        CustomUserDetails currentUser =(CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        WorkingPlan workingPlan = workingPlanRepository.getOne(planId);
        if(workingPlan.getProvider().getId()!=currentUser.getId()){
            throw new org.springframework.security.access.AccessDeniedException("Unauthorized");
        }
        workingPlan.getDay(dayOfWeek).getBreaks().remove(breakToDelete);
        workingPlanRepository.save(workingPlan);
    }

    @Override
    public WorkingPlan generateDefaultWorkingPlan() {
        WorkingPlan wp= new WorkingPlan();
        wp.setMonday(new DayPlan(new TimePeroid(LocalTime.parse("06:00"),LocalTime.parse("18:00"))));
        wp.setTuesday(new DayPlan(new TimePeroid(LocalTime.of(06,00),LocalTime.of(18,00))));
        wp.setWednesday(new DayPlan(new TimePeroid(LocalTime.of(06,00),LocalTime.of(18,00))));
        wp.setThursday(new DayPlan(new TimePeroid(LocalTime.of(06,00),LocalTime.of(18,00))));
        wp.setFriday(new DayPlan(new TimePeroid(LocalTime.of(06,00),LocalTime.of(18,00))));
        wp.setSaturday(new DayPlan(new TimePeroid(LocalTime.of(06,00),LocalTime.of(18,00))));
        wp.setSunday(new DayPlan(new TimePeroid(LocalTime.of(06,00),LocalTime.of(18,00))));
        return wp;
    }

    @Override
    @PreAuthorize("#providerId == principal.id")
    public WorkingPlan getWorkingPlanByProviderId(int providerId) {
        return workingPlanRepository.getWorkingPlanByProviderId(providerId);
    }


}
