package com.demo.samples.config;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ZookeeperConfig {
    @Bean(initMethod = "start", destroyMethod = "close")
    public CuratorFramework curatorFramework() {
        return CuratorFrameworkFactory.newClient(
                "localhost:2181",
                new ExponentialBackoffRetry(1000, 3)
        );
    }


    @Bean
    public ServiceDiscovery<Void> serviceDiscovery(CuratorFramework client) throws Exception {
        // This creates the "Manager" for your ephemeral nodes
        return ServiceDiscoveryBuilder.builder(Void.class)
                .client(client)
                .basePath("/services") // The root path in Zookeeper
                .build();
    }
}