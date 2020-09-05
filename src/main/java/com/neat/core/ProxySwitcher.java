package com.neat.core;

import com.neat.util.IOHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class ProxySwitcher implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(ProxySwitcher.class);
    private final String switcherName;
    private final InputStream northInputStream;
    private final OutputStream northOutputStream;
    private final InputStream southInputStream;
    private final OutputStream southOutputStream;
    private final Callable<Boolean> callback;

    public ProxySwitcher(String switcherName, InputStream northInputStream, OutputStream northOutputStream, InputStream southInputStream, OutputStream southOutputStream, Callable<Boolean> callback) {
        this.switcherName = switcherName;
        this.northInputStream = northInputStream;
        this.northOutputStream = northOutputStream;
        this.southInputStream = southInputStream;
        this.southOutputStream = southOutputStream;
        this.callback = callback;
    }

    //Close
    public void callback(boolean processStatus) {
        if (!processStatus) {
            try {
                TimeUnit.MILLISECONDS.sleep(500);
            } catch (InterruptedException e) {
                log.error(e.getMessage());
            }
        }

        //Callback
        try {
            callback.call();
        } catch (Exception e) {
            log.error(switcherName, e);
        }
    }

    @Override
    public void run() {
        Thread tA = new Thread(() -> {
            try {
                IOHelper.copy(southInputStream, northOutputStream);
            } catch (IOException e) {
                log.error(switcherName + " copy from south to north", e);
                callback(false);
            }
        });
        Thread tB = new Thread(() -> {
            try {
                IOHelper.copy(northInputStream, southOutputStream);
            } catch (IOException e) {
                log.error(switcherName + " copy from north to south", e);
                callback(false);
            }
        });

        tA.start();
        tB.start();

        try {
            tA.join();
            tB.join();
        } catch (InterruptedException e) {
            log.error(switcherName, e);
        }

        callback(true);

        log.debug("Finished:{}", switcherName);
    }
}
