package com.neat.app;

import com.neat.app.client.ProxyClientDaemon;
import com.neat.app.server.controller.ProxyController;
import com.neat.core.server.ProxyServerDaemonNorth;
import com.neat.core.server.ProxyServerDaemonSouth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@ComponentScan(basePackages = {"com.neat.app.server.controller"})
@EnableAutoConfiguration(exclude = {SecurityAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
public class ProxyApplication implements CommandLineRunner {
    private static final String PRINTLN_SEPARATOR_LINE = "^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^";
    @Autowired
    private ProxyController proxyController;

    public static void main(String[] args) {
        try {
            String startMode = "client+server";
            if (args.length > 0) {
                startMode = args[0].toLowerCase();
            }

            /*Launch client daemon threads*/
            if (!startMode.contains("server")) {
                Thread clientDaemon = new Thread(new ProxyClientDaemon());
                clientDaemon.start();
                TimeUnit.MILLISECONDS.sleep(200);
                printGreetingMessage(startMode);
            } else {
                SpringApplication.run(ProxyApplication.class, args);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {
            // Note that this is just here for debugging purposes. It can be deleted at any time.
            System.out.println("Let's inspect the beans provided by Spring Boot:");

            String[] beanNames = ctx.getBeanDefinitionNames();
            Arrays.sort(beanNames);
            for (String beanName : beanNames) {
                System.out.println(beanName);
            }

            Arrays.stream(args).forEach(System.out::println);

            String startMode = "client+server";
            if (args.length > 0) {
                startMode = args[0].toLowerCase();
            }

            proxyController.setStartMode(startMode);

            /*Launch server daemon threads*/
            if (startMode.contains("server")) {
                Thread northDaemon = new Thread(new ProxyServerDaemonNorth());
                northDaemon.start();

                Thread southDaemon = new Thread(new ProxyServerDaemonSouth());
                southDaemon.start();
            }

            /*Launch client daemon threads*/
            if (startMode.contains("client")) {
                Thread clientDaemon = new Thread(new ProxyClientDaemon());
                clientDaemon.start();
            }

            printGreetingMessage(startMode);
        };
    }

    @Override
    public void run(String... args) throws Exception {
        Arrays.stream(args).forEach(System.out::println);
    }

    private static void printGreetingMessage(String startMode) {
        String printInfo = String.format("  Anti-Proxy Initialed: %s  ", startMode.toUpperCase());
        int prefixLength = (PRINTLN_SEPARATOR_LINE.length() - printInfo.length()) / 2;
        String prefixInfo = "-".repeat(prefixLength);
        System.out.println(PRINTLN_SEPARATOR_LINE);
        System.out.println(prefixInfo + printInfo + prefixInfo);
        System.out.println(PRINTLN_SEPARATOR_LINE);
    }
}
