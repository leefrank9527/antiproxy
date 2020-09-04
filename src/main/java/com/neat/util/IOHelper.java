package com.neat.util;

import javax.net.ssl.HttpsURLConnection;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class IOHelper {
    public static String readln(InputStream inputStream) {
        ByteArrayOutputStream bufLine = new ByteArrayOutputStream();
        String line = null;
        while (true) {
            int b = 0;
            try {
                b = inputStream.read();
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }

            if (b == -1) {
                line = new String(bufLine.toByteArray());
                break;
            }

            if (b == '\n') {
                line = new String(bufLine.toByteArray());
                break;
            } else {
                bufLine.write(b);
            }
        }
        return line;
    }

    public static void writeln(OutputStream outputStream, String s) {
        try {
            outputStream.write(s.getBytes());
            outputStream.write("\r\n".getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isNull(String s) {
        return s == null || s.trim().length() == 0;
    }

    public static void copy(InputStream inputStream, OutputStream outputStream) throws IOException {
//        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        while (true) {
            int c = inputStream.read();
            if (c == -1) {
                break;
            }

            outputStream.write(c);

//            if (c == '\n') {
//                System.out.println(new String(byteArrayOutputStream.toByteArray()));
//                byteArrayOutputStream = new ByteArrayOutputStream();
//            } else {
//                byteArrayOutputStream.write(c);
//            }
        }
    }

    public static boolean isConnective(String urlLinkName) {
        try {
            URL url = new URL(urlLinkName);
            URLConnection originalConnection = url.openConnection();
            originalConnection.setConnectTimeout(3 * 1000);
            if (urlLinkName.startsWith("https")) { //HTTPS
                HttpsURLConnection conn = (HttpsURLConnection) originalConnection;
                return conn.getResponseCode() != HttpsURLConnection.HTTP_INTERNAL_ERROR;
            } else { //HTTPS
                HttpURLConnection conn = (HttpURLConnection) originalConnection;
                return conn.getResponseCode() != HttpURLConnection.HTTP_INTERNAL_ERROR;
            }
        } catch (IOException e) {
            return false;
        }
    }
}
