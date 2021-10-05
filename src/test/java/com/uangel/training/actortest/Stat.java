package com.uangel.training.actortest;

import java.time.OffsetDateTime;

public class Stat {
    int NumReq;
    OffsetDateTime LastEvent;

    public Stat(int numReq, OffsetDateTime lastEventTime) {
        this.NumReq = numReq;
        this.LastEvent = lastEventTime;
    }
}
