package com.neat.app.server.controller;

import com.neat.core.MsgPackage;
import com.neat.core.MsgQueue;
import com.neat.core.ProxyConstants;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
public class ProxyController {
    @RequestMapping(path = ProxyConstants.CONTROLLER_SERVICE_HEARTBEAT, method = {RequestMethod.POST, RequestMethod.GET})
    public List<MsgPackage> heartbeat() {
//        List<String> list = MsgQueue.listMsg().stream().map(MsgPackage::getFirstLine).collect(Collectors.toList());
//
//        return String.join("\r\n", list);
        return MsgQueue.listMsg();
    }

    @RequestMapping(path = ProxyConstants.CONTROLLER_SERVICE_DETECT, method = {RequestMethod.POST, RequestMethod.GET})
    public boolean detect() {
        return true;
    }
}
