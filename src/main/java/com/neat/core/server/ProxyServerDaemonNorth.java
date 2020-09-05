package com.neat.core.server;

import com.neat.core.MsgPackage;
import com.neat.core.MsgQueue;
import com.neat.core.ProxyConstants;
import com.neat.core.ProxySwitcher;
import com.neat.util.IOHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

@SuppressWarnings("InfiniteLoopStatement")
public class ProxyServerDaemonNorth implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(ProxyServerDaemonNorth.class);

    private static final int port = ProxyConstants.PORT_FOR_PROXY_CLIENT;

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
                Socket northSocket = serverSocket.accept();
                InputStream northInputStream = northSocket.getInputStream();
                OutputStream northOutputStream = northSocket.getOutputStream();
                String idLine = IOHelper.readln(northInputStream);
                log.debug("id: {}", idLine);
                long id = Long.parseLong(idLine.trim());

                MsgPackage msg = MsgQueue.remove(id);

                if (msg.getMethod().equalsIgnoreCase(ProxyConstants.METHOD_HTTPS_CONNECT)) { //Response for HTTPS
                    while (true) {
                        String headerLine = IOHelper.readln(msg.getInputStream());
                        if (IOHelper.isNull(headerLine)) {
                            break;
                        }
                        log.debug(headerLine.trim());
                    }

                    IOHelper.writeln(msg.getOutputStream(), "HTTP/1.1 200 Connection Established");
                    IOHelper.writeln(msg.getOutputStream(), "Proxy-agent: Netscape-Proxy/1.1");
                    IOHelper.writeln(msg.getOutputStream(), "");

                } else { //Forward HTTP first request line
                    IOHelper.writeln(northOutputStream, msg.getFirstLine());
                }

                ProxySwitcher switcher = new ProxySwitcher("ProxyServer: " + msg.getUrl(), northInputStream, northOutputStream, msg.getInputStream(), msg.getOutputStream(), () -> {
                    try {
                        northSocket.close();
                    } catch (IOException e) {
                        log.error("Failed to close north socket", e);
                    }
                    try {
                        msg.getSocket().close();
                    } catch (IOException e) {
                        log.error("Failed to close south socket", e);
                    }
                    return true;

                });
                Thread switcherProcessor = new Thread(switcher);
                switcherProcessor.start();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }
}
