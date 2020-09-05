package com.neat.app;

import com.neat.app.client.ProxyClientDaemon;
import com.neat.core.server.ProxyServerDaemonNorth;
import com.neat.core.server.ProxyServerDaemonSouth;
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

@SpringBootApplication
@ComponentScan(basePackages = {"com.neat.app.server.controller"})
@EnableAutoConfiguration(exclude = {SecurityAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
public class ProxyApplication implements CommandLineRunner {
    public static void main(String[] args) {
        try {
            SpringApplication.run(ProxyApplication.class, args);
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

            /*Launch server daemon threads*/
            if (args.length == 0 || args[0].equalsIgnoreCase("server+client") || args[0].equalsIgnoreCase("server")) {
                Thread northDaemon = new Thread(new ProxyServerDaemonNorth());
                northDaemon.start();

                Thread southDaemon = new Thread(new ProxyServerDaemonSouth());
                southDaemon.start();
            }

            /*Launch client daemon threads*/
            if (args.length == 0 || args[0].equalsIgnoreCase("server+client") || args[0].equalsIgnoreCase("client")) {
                Thread clientDaemon = new Thread(new ProxyClientDaemon());
                clientDaemon.start();
            }
        };
    }

    @Override
    public void run(String... args) throws Exception {
        Arrays.stream(args).forEach(System.out::println);
    }
}
