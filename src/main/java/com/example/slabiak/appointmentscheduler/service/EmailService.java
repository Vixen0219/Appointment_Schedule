package com.example.slabiak.appointmentscheduler.service;

import com.example.slabiak.appointmentscheduler.entity.Appointment;
import com.example.slabiak.appointmentscheduler.entity.Invoice;
import org.thymeleaf.context.Context;

import java.io.File;

public interface EmailService {
    void sendEmail(String to, String subject, String templateName, Context templateContext, File attachment);
    void sendAppointmentFinishedNotification(Appointment appointment);
    void sendAppointmentRejectionRequestedNotification(Appointment appointment);
    void sendNewAppointmentScheduledNotification(Appointment appointment);
    void sendAppointmentCanceledByCustomerNotification(Appointment appointment);
    void sendAppointmentCanceledByProviderNotification(Appointment appointment);
    void sendInvoice(Invoice invoice);
    void sendAppointmentRejectionAcceptedNotification(Appointment appointment);
}
