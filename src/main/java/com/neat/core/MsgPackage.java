package com.neat.core;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class MsgPackage {
    private static final AtomicLong idGenerator = new AtomicLong(0);
    private long id;
    private String firstLine;
    @JsonIgnore
    private Socket socket;
    @JsonIgnore
    private InputStream inputStream;
    @JsonIgnore
    private OutputStream outputStream;
    @JsonIgnore
    private boolean sent;

    public MsgPackage() {
        this.id = idGenerator.getAndIncrement();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFirstLine() {
        return firstLine;
    }

    public void setFirstLine(String firstLine) {
        this.firstLine = firstLine;
    }


    @JsonIgnore
    public Socket getSocket() {
        return socket;
    }

    @JsonIgnore
    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    @JsonIgnore
    public InputStream getInputStream() {
        return inputStream;
    }

    @JsonIgnore
    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @JsonIgnore
    public OutputStream getOutputStream() {
        return outputStream;
    }

    @JsonIgnore
    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    @JsonIgnore
    public boolean isSent() {
        return sent;
    }

    @JsonIgnore
    public void setSent(boolean sent) {
        this.sent = sent;
    }

    @JsonIgnore
    public String getUrl() {
        if (firstLine == null) {
            return null;
        }

        String[] items = firstLine.split(" ");
        if (items.length != 3) {
            return null;
        }

        if (items[0].equalsIgnoreCase("CONNECT")) {
            return String.format("https://%s", items[1]);
        } else if (items[1].startsWith("http")) {
            return items[1];
        } else {
            return String.format("http://%s", items[1]);
        }
    }

    @JsonIgnore
    public String getMethod() {
        if (firstLine == null) {
            return null;
        }

        String[] items = firstLine.split(" ");
        if (items.length != 3) {
            return null;
        }

        return items[0];
    }

    @JsonIgnore
    public String getVersion() {
        if (firstLine == null) {
            return null;
        }

        String[] items = firstLine.split(" ");
        if (items.length != 3) {
            return null;
        }

        return items[2];
    }
}
