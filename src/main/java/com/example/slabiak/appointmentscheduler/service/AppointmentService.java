package com.example.slabiak.appointmentscheduler.service;

import com.example.slabiak.appointmentscheduler.entity.Appointment;
import com.example.slabiak.appointmentscheduler.entity.ChatMessage;
import com.example.slabiak.appointmentscheduler.entity.User;
import com.example.slabiak.appointmentscheduler.model.AppointmentRegisterForm;
import com.example.slabiak.appointmentscheduler.model.TimePeroid;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentService {
    void save(AppointmentRegisterForm appointment);
    void update(Appointment appointment);
    Appointment findById(int id);
    List<Appointment> findAll();
    void deleteById(int id);
    List<Appointment> findByCustomer(User user);
    List<Appointment> findByProvider(User user);
    List<Appointment> findByProviderAndDate(User user, LocalDate date);
    List<Appointment> getAvailableAppointments(int providerId, int workId, LocalDate date);
    public List<TimePeroid> getProviderAvailableTimePeroids(int providerId, int workId, LocalDate date);

    void save(int workId, int providerId, int customerId, LocalDateTime start);

    void cancelById(int appointmentId, int userId);

    void addChatMessageToAppointment(int appointmentId, int authorId, ChatMessage chatMessage);


    boolean isUserAllowedToCancelAppointment(int userId, int appointmentId);
    List<Appointment> getAppointmentsCanceledByUserInThisMonth(int userId);
    void updateUserAppointmentsStatuses(int userId);
    public void updateAllAppointmentsStatuses();


    boolean isUserAllowedToDenyThatAppointmentTookPlace(Integer id, int appointmentId);

    boolean denyAppointment(int appointmentId, int customerId);

    boolean denyAppointment(String token);
}
