package com.upgrade.campsite.service.aop;

import com.upgrade.campsite.model.OccupiedDate;

public abstract class OccupiedDateEvent {
    private OccupiedDate occupiedDate;

    public OccupiedDateEvent(OccupiedDate occupiedDate) {
        this.occupiedDate = occupiedDate;
    }

    public OccupiedDate getOccupiedDate() {
        return occupiedDate;
    }
}


