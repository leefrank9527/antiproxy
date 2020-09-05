package com.neat.core;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MsgQueue {
    private static final Map<Long, MsgPackage> queue = new HashMap<>();

    public static MsgPackage push(String firstLine, Socket socket, InputStream inputStream, OutputStream outputStream) {
        MsgPackage msg = new MsgPackage();
        msg.setFirstLine(firstLine);
        msg.setSocket(socket);
        msg.setInputStream(inputStream);
        msg.setOutputStream(outputStream);

        return push(msg);
    }

    synchronized public static MsgPackage push(MsgPackage msg) {
        queue.put(msg.getId(), msg);
        return msg;
    }

    synchronized public static List<MsgPackage> listMsg() {
        List<MsgPackage> result = new ArrayList<>();
        queue.values().forEach(msgPack -> {
            if (!msgPack.isSent()) {
                msgPack.setSent(true);
                result.add(msgPack);
            }
        });
        return result;
    }

    synchronized public static MsgPackage remove(long id) {
        return queue.remove(id);
    }

    synchronized public static MsgPackage get(long id) {
        return queue.get(id);
    }
}
