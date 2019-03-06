package com.example.slabiak.appointmentscheduler.model;

import java.sql.Time;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DayPlan {

    private TimePeroid workingHours;
    private List<TimePeroid> breaks;

    public DayPlan(){
        breaks = new ArrayList<TimePeroid>();
    }

    public DayPlan(TimePeroid workingHours){
        this.workingHours = workingHours;
        this.breaks = new ArrayList<TimePeroid>();
    }

    public ArrayList<TimePeroid> peroidsWithBreaksExcluded(){
        ArrayList<TimePeroid> breaksExcluded = new ArrayList<>();
        breaksExcluded.add(getWorkingHours());
        List<TimePeroid> breaks = getBreaks();

        if(breaks.size()>0) {
            ArrayList<TimePeroid> toAdd = new ArrayList<TimePeroid>();
            for (TimePeroid break1 : breaks) {
                if(break1.getStart().isBefore(workingHours.getStart())){
                    break1.setStart(workingHours.getStart());
                }
                if(break1.getEnd().isAfter(workingHours.getEnd())){
                    break1.setEnd(workingHours.getEnd());
                }
                for (TimePeroid peroid : breaksExcluded) {
                    if (break1.getStart().equals(peroid.getStart()) && break1.getEnd().isAfter(peroid.getStart()) && break1.getEnd().isBefore(peroid.getEnd())) {
                        peroid.setStart(break1.getEnd());
                    }
                    if (break1.getStart().isAfter(peroid.getStart()) && break1.getStart().isBefore(peroid.getEnd()) && break1.getEnd().equals(peroid.getEnd())) {
                        peroid.setEnd(break1.getStart());
                    }
                    if (break1.getStart().isAfter(peroid.getStart()) && break1.getEnd().isBefore(peroid.getEnd())) {
                        toAdd.add(new TimePeroid(peroid.getStart(), break1.getStart()));
                        peroid.setStart(break1.getEnd());
                    }
                }
            }
            breaksExcluded.addAll(toAdd);
            Collections.sort(breaksExcluded);
        }


        return breaksExcluded;
    }

    public TimePeroid getWorkingHours() {
        return workingHours;
    }

    public void setWorkingHours(TimePeroid workingHours) {
        this.workingHours = workingHours;
    }

    public List<TimePeroid> getBreaks() {
        return breaks;
    }

    public void setBreaks(List<TimePeroid> breaks) {
        this.breaks = breaks;
    }

    public void removeBreak(TimePeroid breakToRemove){
        System.out.println("usuwam:"+ breakToRemove.getStart());
        breaks.remove(breakToRemove);
    }

    public void addBreak(TimePeroid breakToAdd){
        breaks.add(breakToAdd);
    }

}
