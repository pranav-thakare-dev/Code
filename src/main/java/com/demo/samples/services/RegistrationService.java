package com.demo.samples.services;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.springframework.boot.web.server.context.WebServerInitializedEvent;
import org.springframework.boot.web.server.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class RegistrationService {

    private final ServletWebServerApplicationContext context;    private int port;

    private final ServiceDiscovery<Void> serviceDiscovery;

    public RegistrationService(ServletWebServerApplicationContext context, CuratorFramework client, ServiceDiscovery<Void> serviceDiscovery) throws Exception {
        this.context = context;
        this.serviceDiscovery = serviceDiscovery;
    }


    @EventListener
    public void onApplicationEvent(WebServerInitializedEvent event) throws Exception {
        int actualPort = event.getWebServer().getPort();

        ServiceInstance<Void> instance = ServiceInstance.<Void>builder()
                .name("order-service")
                .address("localhost")
                .port(actualPort) // This will now correctly be 8073
                .build();

        serviceDiscovery.registerService(instance);
        System.out.println("Successfully registered to Zookeeper on port: " + actualPort);
    }


}