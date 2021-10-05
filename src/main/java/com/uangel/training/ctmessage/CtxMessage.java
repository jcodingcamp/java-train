package com.uangel.training.ctmessage;

public class CtxMessage {
    long trid;
    String msg;

    public CtxMessage(long trid, String msg) {
        this.trid = trid;
        this.msg = msg;
    }

    public long getTrid() {
        return trid;
    }

    public String getMsg() {
        return msg;
    }
}
