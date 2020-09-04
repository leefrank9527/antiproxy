package com.neat.core;

import com.neat.util.IOHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ProxySwitcher implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(ProxySwitcher.class);
    private final String switcherName;
    private final InputStream northInputStream;
    private final OutputStream northOutputStream;
    private final InputStream southInputStream;
    private final OutputStream southOutputStream;

    public ProxySwitcher(String switcherName, InputStream northInputStream, OutputStream northOutputStream, InputStream southInputStream, OutputStream southOutputStream) {
        this.switcherName = switcherName;
        this.northInputStream = northInputStream;
        this.northOutputStream = northOutputStream;
        this.southInputStream = southInputStream;
        this.southOutputStream = southOutputStream;
    }

    @Override
    public void run() {
        Runnable pipeA = () -> {
            try {
                IOHelper.copy(southInputStream, northOutputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        };

        Runnable pipeB = () -> {
            try {
                IOHelper.copy(northInputStream, southOutputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        };

        Thread tA = new Thread(pipeA);
        Thread tB = new Thread(pipeB);

        tA.start();
        tB.start();

        try {
            tA.join();
            tB.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        log.debug("Finished");
    }

    public String getSwitcherName() {
        return switcherName;
    }

    public InputStream getNorthInputStream() {
        return northInputStream;
    }

    public OutputStream getNorthOutputStream() {
        return northOutputStream;
    }

    public InputStream getSouthInputStream() {
        return southInputStream;
    }

    public OutputStream getSouthOutputStream() {
        return southOutputStream;
    }
}
