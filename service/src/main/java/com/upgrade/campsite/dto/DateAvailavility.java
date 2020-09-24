package com.upgrade.campsite.dto;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;

@ToString
@EqualsAndHashCode
public class DateAvailavility {

    private LocalDate date;
    private boolean vacant;

    public DateAvailavility() {
    }

    public DateAvailavility(LocalDate date, boolean vacant) {
        this.date = date;
        this.vacant = vacant;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public boolean isVacant() {
        return vacant;
    }

    public void setVacant(boolean vacant) {
        this.vacant = vacant;
    }
}
