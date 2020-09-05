package com.neat.app.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neat.core.MsgPackage;
import com.neat.core.ProxyConstants;
import com.neat.core.client.ProxyClientAgent;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

@SuppressWarnings("all")
public class ProxyClientDaemon implements Runnable {
    private String EXCUTE_STATUS_RUNNING = "RUNNING";
    private String EXCUTE_STATUS_FINISHED = "FINISHED";

    private String proxyServerAddress = null;
    private URI proxyServerUriHeartbeat;
    private AtomicLong idlePrintLength = new AtomicLong(0);

    @Override
    public void run() {
        //Detecting the IP4 Address to ProxyServer
        while (proxyServerAddress == null) {
            try {
                proxyServerAddress = detectProxyServerAddress();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }

            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //Initialing the heartbeat service url to ProxyServer
        try {
            proxyServerUriHeartbeat = new URI(String.format("http://%s:%d/%s", proxyServerAddress, ProxyConstants.PORT_FOR_PROXY_SERVER, ProxyConstants.CONTROLLER_SERVICE_HEARTBEAT));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        System.out.println("Proxy Server IP detected: " + proxyServerAddress);


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
                TimeUnit.MILLISECONDS.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private String detectProxyServerAddress() throws InterruptedException, ExecutionException {
        System.out.println("Detecting IP Address of ProxyServer >>>");
        Map<String, Future<Boolean>> mapExtutorResult = new HashMap<>();
//        ExecutorService executorService = new ThreadPoolExecutor(1, 50, 500L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
        ExecutorService executorService = Executors.newFixedThreadPool(50);
        for (int lastPartOfIP = 2; lastPartOfIP < 255; lastPartOfIP++) {
            printProgressFlag();

            String hostIP = String.format("192.168.1.%d", lastPartOfIP);

            Callable<Boolean> processor = new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    return detectAddress(hostIP);
                }
            };
            Future<Boolean> futureResult = executorService.submit(processor);
            mapExtutorResult.put(hostIP, futureResult);
        }

        String detectedIP = null;
        while (true) {
            printProgressFlag();

            String scanResult = scanExcutingPool(mapExtutorResult);
            if (scanResult.equals(EXCUTE_STATUS_RUNNING)) {
                TimeUnit.MILLISECONDS.sleep(200);
                continue;
            } else if (scanResult.equals(EXCUTE_STATUS_FINISHED)) {
                break;
            } else {
                detectedIP = scanResult;
                break;
            }
        }

        if (!executorService.isTerminated()) {
            executorService.awaitTermination(1, TimeUnit.SECONDS);
        }

        if (!executorService.isShutdown()) {
            executorService.shutdown();
        }

        return detectedIP;
    }

    private String scanExcutingPool(Map<String, Future<Boolean>> mapExtutorResult) throws ExecutionException, InterruptedException {
        String detectedIP = null;
        boolean isDone = true;
        for (Map.Entry<String, Future<Boolean>> entry : mapExtutorResult.entrySet()) {
            String k = entry.getKey();
            Future<Boolean> v = entry.getValue();
            if (v.isDone() && v.get()) {
                detectedIP = k;
            }
            if (!v.isDone()) {
                isDone = false;
            }
        }

        if (detectedIP != null) {
            return detectedIP;
        } else if (isDone) {
            return EXCUTE_STATUS_FINISHED;
        } else {
            return EXCUTE_STATUS_RUNNING;
        }
    }

    private boolean detectAddress(String hostIP) {
        String detectUrl = String.format("http://%s:%d/%s", hostIP, ProxyConstants.PORT_FOR_PROXY_SERVER, ProxyConstants.CONTROLLER_SERVICE_DETECT);
//        System.out.println(detectUrl);

        HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(detectUrl))
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();

        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NEVER)
                .connectTimeout(Duration.ofSeconds(30))
                .build();

        try {
            HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            String result = httpResponse.body();
            if (result.equalsIgnoreCase("true")) {
                return true;
            }
        } catch (IOException | InterruptedException e) {
            return false;
        }
        return false;
    }

    private void heartbeat() throws IOException, InterruptedException {
        ObjectMapper objectMapper = new ObjectMapper();

        HttpRequest httpRequest = HttpRequest.newBuilder(proxyServerUriHeartbeat)
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
            printProgressFlag();
            return;
        }

        for (MsgPackage msg : httpRequestList) {
            ProxyClientAgent.launchSwitcher("wlgproxyforservers.dia.govt.nz", 8080, proxyServerAddress, ProxyConstants.PORT_FOR_PROXY_CLIENT, msg);
        }
    }

    private void printProgressFlag() {
        //            if (idlePrintLength == 120) {
//                System.out.println(".");
//                idlePrintLength = 1;
//            } else {
//                System.out.print(".");
//                idlePrintLength++;
//            }


        if (idlePrintLength.getAndIncrement() % 2 == 0) {
            System.out.print("\r=-=");
        } else {
            System.out.print("\r^-^");
        }
    }
}