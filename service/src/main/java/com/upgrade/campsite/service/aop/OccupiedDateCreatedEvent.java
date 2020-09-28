package com.upgrade.campsite.service.aop;

import com.upgrade.campsite.model.OccupiedDate;

public class OccupiedDateCreatedEvent extends OccupiedDateEvent {

    public OccupiedDateCreatedEvent(OccupiedDate occupiedDate) {
        super(occupiedDate);
    }

}
