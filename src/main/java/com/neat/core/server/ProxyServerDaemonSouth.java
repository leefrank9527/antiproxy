package com.neat.core.server;

import com.neat.core.MsgPackage;
import com.neat.core.MsgQueue;
import com.neat.core.ProxyConstants;
import com.neat.util.IOHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

@SuppressWarnings("InfiniteLoopStatement")
public class ProxyServerDaemonSouth implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(ProxyServerDaemonNorth.class);
    private static final int port = ProxyConstants.PORT_FOR_BROWSER;

    @Override
    public void run() {
        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        while (true) {
            try {
                Socket southSocket = serverSocket.accept();
                InputStream southInputStream = southSocket.getInputStream();
                OutputStream southOutputStream = southSocket.getOutputStream();

                String firstLine = IOHelper.readln(southInputStream);
                MsgPackage msg = MsgQueue.push(firstLine.trim(), new ArrayList<>(), southInputStream, southOutputStream);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }
}
