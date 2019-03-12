package com.example.slabiak.appointmentscheduler.service;

import com.example.slabiak.appointmentscheduler.dao.AppointmentRepository;
import com.example.slabiak.appointmentscheduler.dao.ChatMessageRepository;
import com.example.slabiak.appointmentscheduler.dao.InvoiceRepository;
import com.example.slabiak.appointmentscheduler.dao.WorkingPlanRepository;
import com.example.slabiak.appointmentscheduler.entity.*;
import com.example.slabiak.appointmentscheduler.model.AppointmentRegisterForm;
import com.example.slabiak.appointmentscheduler.model.DayPlan;
import com.example.slabiak.appointmentscheduler.model.TimePeroid;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Service
public class AppointmentServiceImpl implements AppointmentService{

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private WorkService workService;

    @Autowired
    private WorkingPlanRepository workingPlanRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    EmailService emailService;

    @Autowired
    JwtTokenService jwtTokenService;

    @Autowired
    private InvoiceService invoiceService;

    public AppointmentServiceImpl() {
    }


    @Override
    public void save(AppointmentRegisterForm appointmentRegisterForm) {
        Appointment appointment = new Appointment();
        appointment.setStatus("scheduled");
        appointment.setCustomer(userService.findById(appointmentRegisterForm.getCustomerId()));
        appointment.setProvider(userService.findById(appointmentRegisterForm.getProviderId()));
        Work work = workService.findById(appointmentRegisterForm.getWorkId());
        appointment.setWork(work);
        appointment.setStart(appointmentRegisterForm.getStart());
        appointment.setEnd(appointmentRegisterForm.getStart().plusMinutes(work.getDuration()));
        appointmentRepository.save(appointment);
    }

    @Override
    public void update(Appointment appointment) {
        appointmentRepository.save(appointment);
    }

    @Override
    public Appointment findById(int id) {
        Optional<Appointment> result = appointmentRepository.findById(id);
        Appointment appointment = null;

        if (result.isPresent()) {
            appointment = result.get();
        }
        else {
            // todo throw new excep
        }

        return appointment;
    }

    @Override
    public List<Appointment> findAll() {
        return appointmentRepository.findAll();
    }

    @Override
    public void deleteById(int id) {
        appointmentRepository.deleteById(id);
    }

    @Override
    public List<Appointment> findByCustomer(User customer) {
        return appointmentRepository.findByCustomer(customer);
    }

    @Override
    public List<Appointment> findByProvider(User provider) {
        return appointmentRepository.findByCustomer(provider);
    }

    @Override
    public List<Appointment> findByProviderAndDate(User user, LocalDate date) {
        return appointmentRepository.findByProviderAndDate(user,date.atStartOfDay(), date.atStartOfDay().plusHours(24));
    }

    @Override
    public List<Appointment> getAvailableAppointments(int providerId, int workId, LocalDate date) {
        return null;
    }

    @Override
    public List<TimePeroid> getProviderAvailableTimePeroids(int providerId, int workId, LocalDate date){
       WorkingPlan workingPlan = workingPlanRepository.getOne(1);
        DayPlan selectedDay = workingPlan.getDay(date.getDayOfWeek().toString().toLowerCase());

        List<Appointment> providerAppointments = findByProviderAndDate(userService.findById(providerId),date);

        List<TimePeroid> availablePeroids = new ArrayList<TimePeroid>();
        // get peroids from working hours for selected day excluding breaks

        availablePeroids = selectedDay.peroidsWithBreaksExcluded();
        // exclude provider's appointments from available peroids
        availablePeroids = excludeAppointmentsFromTimePeroids(availablePeroids,providerAppointments);
       return calculateAvailableHours(availablePeroids,workService.findById(workId));
       // return availablePeroids;
    }

    @Override
    public void save(int workId, int providerId, int customerId, LocalDateTime start) {
        Appointment appointment = new Appointment();
        appointment.setStatus("scheduled");
        appointment.setCustomer(userService.findById(customerId));
        appointment.setProvider(userService.findById(providerId));
        Work work = workService.findById(workId);
        appointment.setWork(work);
        appointment.setStart(start);
        appointment.setEnd(start.plusMinutes(work.getDuration()));
        appointmentRepository.save(appointment);
    }

    @Override
    public void addChatMessageToAppointment(int appointmentId, int authorId, ChatMessage chatMessage) {
        chatMessage.setAuthor(userService.findById(authorId));
        chatMessage.setAppointment(findById(appointmentId));
        chatMessage.setCreatedAt(LocalDateTime.now());
        chatMessageRepository.save(chatMessage);
    }




    public List<TimePeroid> calculateAvailableHours(List<TimePeroid> availableTimePeroids, Work work){
        ArrayList<TimePeroid> availableHours = new ArrayList<TimePeroid>();
        for(TimePeroid peroid: availableTimePeroids){
            TimePeroid workPeroid = new TimePeroid(peroid.getStart(),peroid.getStart().plusMinutes(work.getDuration()));
            while(workPeroid.getEnd().isBefore(peroid.getEnd()) || workPeroid.getEnd().equals(peroid.getEnd())){
                availableHours.add(new TimePeroid(workPeroid.getStart(),workPeroid.getStart().plusMinutes(work.getDuration())));
                workPeroid.setStart(workPeroid.getStart().plusMinutes(work.getDuration()));
                workPeroid.setEnd(workPeroid.getEnd().plusMinutes(work.getDuration()));
            }
        }
        return availableHours;
    }

    public List<TimePeroid> excludeAppointmentsFromTimePeroids(List<TimePeroid> peroids, List<Appointment> appointments){

            List<TimePeroid> toAdd = new ArrayList<TimePeroid>();
            Collections.sort(appointments);
            for(Appointment appointment: appointments){
                for(TimePeroid peroid:peroids){
                    if((appointment.getStart().toLocalTime().isBefore(peroid.getStart()) || appointment.getStart().toLocalTime().equals(peroid.getStart())) && appointment.getEnd().toLocalTime().isAfter(peroid.getStart()) && appointment.getEnd().toLocalTime().isBefore(peroid.getEnd())){
                        peroid.setStart(appointment.getEnd().toLocalTime());
                    }
                    if(appointment.getStart().toLocalTime().isAfter(peroid.getStart())&& appointment.getStart().toLocalTime().isBefore(peroid.getEnd()) && appointment.getEnd().toLocalTime().isAfter(peroid.getEnd()) || appointment.getEnd().toLocalTime().equals(peroid.getEnd())){
                        peroid.setEnd(appointment.getStart().toLocalTime());
                    }
                    if(appointment.getStart().toLocalTime().isAfter(peroid.getStart()) && appointment.getEnd().toLocalTime().isBefore(peroid.getEnd())){
                        toAdd.add(new TimePeroid(peroid.getStart(),appointment.getStart().toLocalTime()));
                        peroid.setStart(appointment.getEnd().toLocalTime());
                    }
                }
            }
            peroids.addAll(toAdd);
            Collections.sort(peroids);
        return peroids;
    }

    public List<Appointment> getAppointmentsCanceledByUserInThisMonth(int userId){
        User user = userService.findById(userId);
        return appointmentRepository.getAppointmentsCanceledByUserInThisMonth(user, LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay());
    }

    @Override
    public void updateUserAppointmentsStatuses(int userId) {
         /*
         * find appointments which requires status change from scheudled to finished and change their status
         * (all appointments which have status 'scheduled' and their end date is before current timestamp)
         * */
        for(Appointment appointment: appointmentRepository.findUserScheduledAppointmentsWithEndBeforeDate(LocalDateTime.now(),userId)){
            appointment.setStatus("finished");
            update(appointment);
        }
         /*
         * find appointments which requires status change from finished to confirmed and change their status
         * (all appointments which have status 'finished' and their end date is more than 24 hours before current timestamp)
         * */
        for(Appointment appointment: appointmentRepository.findUserFinishedAppointmentsWithEndBeforeDate(LocalDateTime.now().minusHours(24),userId)){

            appointment.setStatus("invoiced");
            update(appointment);
        }
    }

    @Override
    public void updateAllAppointmentsStatuses() {
         /*
         * find appointments which requires status change from scheudled to finished and change their status
         * (all appointments which have status 'scheduled' and their end date is before current timestamp)
         * */
        for(Appointment appointment: appointmentRepository.findAllScheduledAppointmentsWithEndBeforeDate(LocalDateTime.now())){
            appointment.setStatus("finished");
            update(appointment);
            /*
            * user have 24h after appointment finished to deny that appointment took place
            * if it's less than 24h since the appointment finished, send him a link with a token that allows to deny that appointment took place
            * it it's more than 24h, dont send it cause it's to late to deny
            * */
            if(LocalDateTime.now().minusHours(24).isBefore(appointment.getEnd())) {
                emailService.sendFinishedAppointmentNotification(appointment);
            }
        }
         /*
         * find appointments which requires status change from finished to confirmed and change their status
         * (all appointments which have status 'finished' and their end date is more than 24 hours before current timestamp)
         * */
        for(Appointment appointment: appointmentRepository.findAllFinishedAppointmentsWithEndBeforeDate(LocalDateTime.now().minusHours(24))){
            Invoice invoice = new Invoice(invoiceService.generateInvoiceNumber(),"issued",LocalDateTime.now(),appointment);
            invoiceService.save(invoice);
            appointment.setStatus("invoiced");
            update(appointment);
        }
    }

    @Override
    public boolean isUserAllowedToCancelAppointment(int userId, int appointmentId) {
        User user = userService.findById(userId);
        Appointment appointment = findById(appointmentId);

        // only scheduled appointments can be canceled
        if(!appointment.getStatus().equals("scheduled")){
            return false;
        }
        // if user is appointment provider he can cancel it without any conditions
        else if(appointment.getProvider().equals(user)){
            return true;

            // appointments can be canceled max 24h before appointment
        }  else if(LocalDateTime.now().plusHours(24).isAfter(appointment.getStart())){
            return false;
        }
        // check considered appointments is appointment for specified user, if this type of work is allowed to be cancelled by customer
        else if(!appointment.getCustomer().equals(user) || !appointment.getWork().getEditable()){
            return false;
        }
        //and if customer total amount of canncelation in this month is not greater than 1
        else if(getAppointmentsCanceledByUserInThisMonth(userId).size()>=1){
            return false;
        }

        return true;
    }

    @Override
    public void cancelById(int appointmentId, int userId) {
        Appointment appointment = appointmentRepository.getOne(appointmentId);
        appointment.setStatus("canceled");
        appointment.setCanceler(userService.findById(userId));
        appointment.setCanceledAt(LocalDateTime.now());
        appointmentRepository.save(appointment);
    }


    @Override
    public boolean isUserAllowedToDenyThatAppointmentTookPlace(Integer userId, int appointmentId) {
        User user = userService.findById(userId);
        Appointment appointment = findById(appointmentId);

        if(!appointment.getCustomer().equals(user)){
            return false;
        } else if(!appointment.getStatus().equals("finished")){
            return false;
        } else if(LocalDateTime.now().isAfter(appointment.getEnd().plusHours(24))){
            return false;
        }else{
            return true;
        }
    }

    @Override
    public boolean denyAppointment(int appointmentId, int customerId) {
        if(isUserAllowedToDenyThatAppointmentTookPlace(customerId,appointmentId)){
            Appointment appointment = findById(appointmentId);
            appointment.setStatus("denied");
            update(appointment);
            return true;
        } else{
            return false;
        }

    }


    @Override
    public boolean denyAppointment(String token) {
        if(jwtTokenService.validateToken(token)){
            int appointmentId = jwtTokenService.getAppointmentIdFromJWT(token);
            int customerId = jwtTokenService.getCustomerIdFromJWT(token);
            return denyAppointment(appointmentId,customerId);
        }
        return false;
    }
}
