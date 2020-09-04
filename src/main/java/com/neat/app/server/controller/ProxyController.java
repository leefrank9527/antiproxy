package com.neat.app.server.controller;

import com.neat.app.server.MsgPackage;
import com.neat.app.server.MsgQueue;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class ProxyController {
    @RequestMapping(path = "/heartbeat", method = {RequestMethod.POST, RequestMethod.GET})
    public String heartbeat() {
        List<String> list = MsgQueue.listMsg().stream().map(MsgPackage::getFirstLine).collect(Collectors.toList());

        return String.join("\r\n", list);
    }
}
