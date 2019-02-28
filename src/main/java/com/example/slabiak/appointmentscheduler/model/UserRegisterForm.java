package com.example.slabiak.appointmentscheduler.model;


import javax.persistence.criteria.CriteriaBuilder;
import java.util.List;

public class UserRegisterForm {

    private String userName;

    private String password;

    private String matchingPassword;

    private String firstName;

    private String lastName;

    private String email;

    private List<Integer> selectedWorks;

    public UserRegisterForm(){

    }


    public UserRegisterForm(String userName, String password, String matchingPassword, String firstName, String lastName, String email) {
        this.userName = userName;
        this.password = password;
        this.matchingPassword = matchingPassword;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMatchingPassword() {
        return matchingPassword;
    }

    public void setMatchingPassword(String matchingPassword) {
        this.matchingPassword = matchingPassword;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<Integer> getSelectedWorks() {
        return selectedWorks;
    }

    public void setSelectedWorks(List<Integer> selectedWorks) {
        this.selectedWorks = selectedWorks;
    }
}
