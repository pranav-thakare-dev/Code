package com.demo.samples;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;


import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/zk-test")
public class ZookeeperTestController {

    private final CuratorFramework client;
    private final ServiceDiscovery<Void> serviceDiscovery;

    public ZookeeperTestController(CuratorFramework client, ServiceDiscovery<Void> serviceDiscovery) {
        this.client = client;
        this.serviceDiscovery = serviceDiscovery;
    }

    // 1. Check all registered instances of "order-service"
    @GetMapping("/services")
    public List<String> getActiveServices() throws Exception {
        return serviceDiscovery.queryForInstances("order-service")
                .stream()
                .map(instance -> instance.getAddress() + ":" + instance.getPort())
                .collect(Collectors.toList());
    }

    // 2. See which node is currently the leader
    @GetMapping("/leader")
    public String getLeader() throws Exception {
        // We look at the data inside the leader path
        byte[] data = client.getData().forPath("/leader/job-manager");
        return data != null ? new String(data) : "Leader path exists, but no data set.";
    }
}