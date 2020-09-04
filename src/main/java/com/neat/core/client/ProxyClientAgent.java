package com.neat.core.client;

import com.neat.core.MsgPackage;
import com.neat.core.ProxySwitcher;
import com.neat.util.IOHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ProxyClientAgent {
    private static final Logger log = LoggerFactory.getLogger(ProxyClientAgent.class);
    private final Map<String, Boolean> CONNECTIVE_MAP = Collections.synchronizedMap(new HashMap<>());
    private final String proxyHost;
    private final int proxyPort;

    private final String southHost;
    private final int southPort;

    private final MsgPackage msgPackage;

    private ProxyClientAgent(String proxyHost, int proxyPort, String southHost, int southPort, MsgPackage msgPackage) {
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.southHost = southHost;
        this.southPort = southPort;
        this.msgPackage = msgPackage;
    }

    public static ProxyClientAgent launchSwitcher(String proxyHost, int proxyPort, String southHost, int southPort, MsgPackage msgPackage) {
        ProxyClientAgent switcher = new ProxyClientAgent(proxyHost, proxyPort, southHost, southPort, msgPackage);
        boolean initResult = switcher.init();
        if (!initResult) {
            return null;
        }

        return switcher;
    }

    private boolean init() {
        Socket northSocket = getSocketFromUrl(msgPackage.getUrl());
        if (northSocket == null) {
            log.debug("Not able to find address of URL: {}, try to use proxy", msgPackage.getUrl());
            northSocket = new Socket(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(proxyHost, proxyPort)));
        }

        Socket southSocket;
        try {
            southSocket = new Socket(southHost, southPort);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Not able to initial connection to: {}, {}", southHost, southPort);
            return false;
        }

        try {
            InputStream northInputStream = northSocket.getInputStream();
            OutputStream northOutputStream = northSocket.getOutputStream();
            InputStream southInputStream = southSocket.getInputStream();
            OutputStream southOutputStream = southSocket.getOutputStream();

            //Feedback the message ID
            IOHelper.writeln(southOutputStream, Long.toString(msgPackage.getId()));

            ProxySwitcher switcher = new ProxySwitcher("ProxyClient: " + msgPackage.getUrl(), northInputStream, northOutputStream, southInputStream, southOutputStream);
            Thread switcherProcessor = new Thread(switcher);
            switcherProcessor.start();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private Socket getSocketFromUrl(String urlName) {
        try {
            URL url = new URL(urlName);
            String hostname = url.getHost();
            if (CONNECTIVE_MAP.containsKey(hostname)) {
                if (!CONNECTIVE_MAP.get(hostname)) {
                    return null;
                }
            } else {
                boolean isConnective = IOHelper.isConnective(urlName);
                CONNECTIVE_MAP.put(hostname, isConnective);
                if (!isConnective) {
                    return null;
                }
            }

            int port = url.getPort();
            if (port == -1) {
                port = 80;
            }
            return new Socket(hostname, port);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}