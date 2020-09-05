package com.neat.core;

public interface ProxyConstants {
    int PORT_FOR_BROWSER = 3082;
    int PORT_FOR_PROXY_CLIENT = 3081;
    int PORT_FOR_PROXY_SERVER = 3080;

    String METHOD_HTTPS_CONNECT = "CONNECT";

    String CONTROLLER_SERVICE_DETECT = "/detect";
    String CONTROLLER_SERVICE_HEARTBEAT = "/heartbeat";
}
