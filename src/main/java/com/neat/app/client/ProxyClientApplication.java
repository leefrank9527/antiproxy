package com.neat.app.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neat.core.MsgPackage;
import com.neat.core.client.ProxyClientAgent;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

@SuppressWarnings("all")
public class ProxyClientApplication {
    private static String proxyServerAddress;
    private static URI proxyServerUri;
    private static int idlePrintLength = 1;

    public static void main(String[] args) throws URISyntaxException {
        if (args == null || args.length != 1) {
            System.out.println("Usage: java -jar xxxx.jar ServerAddress");
            System.exit(0);
        }

        proxyServerAddress = args[0];

        String proxyServerUrl = String.format("http://%s:3080/heartbeat", proxyServerAddress);
        proxyServerUri = new URI(proxyServerUrl);

        System.out.println("^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^");
        System.out.println("-----------------------------------------Proxy Agent Initialed--------------------------------------------------------");
        System.out.println("^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^");
        while (true) {
            try {
                heartbeat();
            } catch (Throwable e) {
                System.out.println("Heart beat failed: " + e.getMessage());
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    private static void heartbeat() throws IOException, InterruptedException {
        ObjectMapper objectMapper = new ObjectMapper();

        HttpRequest httpRequest = HttpRequest.newBuilder(proxyServerUri)
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();

        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NEVER)
                .connectTimeout(Duration.ofSeconds(30))
                .build();

        HttpResponse<byte[]> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofByteArray());
        if (httpResponse.statusCode() != 200) {
            System.out.println("Heartbeat failed. Status Code: " + httpResponse.statusCode());
            return;
        }

        byte[] httpResponseBody = httpResponse.body();
        List<MsgPackage> httpRequestList = objectMapper.readValue(httpResponseBody, new TypeReference<List<MsgPackage>>() {
        });

        if (httpRequestList == null || httpRequestList.size() == 0) {
            if (idlePrintLength == 120) {
                System.out.println(".");
                idlePrintLength = 1;
            } else {
                System.out.print(".");
                idlePrintLength++;
            }
            return;
        }

        for (MsgPackage msg : httpRequestList) {
            ProxyClientAgent.launchSwitcher("wlgproxyforservers.dia.govt.nz", 8080, proxyServerAddress, 3081, msg);
        }
    }
}