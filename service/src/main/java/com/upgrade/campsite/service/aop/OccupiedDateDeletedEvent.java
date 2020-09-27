package com.upgrade.campsite.service.aop;

import com.upgrade.campsite.model.OccupiedDate;

public class OccupiedDateDeletedEvent extends OccupiedDateEvent {

    public OccupiedDateDeletedEvent(OccupiedDate occupiedDate) {
        super(occupiedDate);
    }
}
