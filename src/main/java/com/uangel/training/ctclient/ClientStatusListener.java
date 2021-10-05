package com.uangel.training.ctclient;

public interface ClientStatusListener {
    void connected(ClientConnection conn);

    void disconnected(ClientConnection conn);
}
