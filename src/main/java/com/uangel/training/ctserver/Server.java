package com.uangel.training.ctserver;

import io.netty.channel.Channel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

// server channel 과 accept 된 connection 들을 관리하는 class
public class Server implements AutoCloseable {
    Channel serverChannel;


    // lock 을 사용하기 때문에,  actor 로 전환하는 것을 고려할 필요 있음
    ReentrantLock lock = new ReentrantLock();

    List<Channel> connections = new ArrayList<>();


    public void Bind(Channel serverChannel) {
        this.serverChannel = serverChannel;
    }

    // server close 할 때 모든 connection 같이 close
    public void close() {
        serverChannel.close();

        lock.lock();
        try {
            connections.stream().forEach(channel -> channel.close());
            connections = new ArrayList<>();
        } finally {
            lock.unlock();
        }
    }



    //accept 되었을 때
    public void channelConnected(Channel channel) {
        lock.lock();
        try {
            connections.add(channel);
        } finally {
            lock.unlock();
        }
    }

    // accept 된 connection 끊어졌을 때
    public void channelDisconnected(Channel channel) {
        lock.lock();
        try {
            connections.remove(channel);
        } finally {
            lock.unlock();
        }
    }

    // request의 response 전송

}
