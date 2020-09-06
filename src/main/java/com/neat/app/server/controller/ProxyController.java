package com.neat.app.server.controller;

import com.neat.core.MsgPackage;
import com.neat.core.MsgQueue;
import com.neat.core.ProxyConstants;
import com.neat.util.IOHelper;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.Semaphore;

@RestController
public class ProxyController {
    private final Semaphore lock = new Semaphore(0);
    private String startMode;

    @RequestMapping(path = ProxyConstants.CONTROLLER_SERVICE_HEARTBEAT, method = {RequestMethod.POST, RequestMethod.GET})
    public List<MsgPackage> heartbeat() {
        return MsgQueue.listMsg();
    }

    @RequestMapping(path = ProxyConstants.CONTROLLER_SERVICE_DETECT, method = {RequestMethod.POST, RequestMethod.GET})
    public boolean detect() {
        try {
            lock.acquire();
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
        return IOHelper.isNull(startMode) || startMode.toLowerCase().contains("server");
    }

    public String getStartMode() {
        return startMode;
    }

    public void setStartMode(String startMode) {
        this.startMode = startMode;
        lock.release();
    }
}
