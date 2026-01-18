package com.demo.samples.services;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;
import org.apache.curator.x.discovery.ServiceInstance;
import org.springframework.boot.web.server.context.WebServerInitializedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class MasterWorkerService {
    private CuratorFramework client;

    public MasterWorkerService(CuratorFramework client) {
        this.client = client;
    }

    @EventListener
    public void onApplicationEvent(WebServerInitializedEvent event) throws Exception {
        int actualPort = event.getWebServer().getPort();
        System.out.println("master worker service");

        LeaderSelector selector = new LeaderSelector(client, "/leader/job-manager",
                new LeaderSelectorListenerAdapter() {
                    @Override
                    public void takeLeadership(CuratorFramework client) throws Exception {
                        String leaderInfo = "Leader is: Instance-on-Port-" + actualPort;

                        //  Write this instance's info into the ZNode
                        client.setData().forPath("/leader/job-manager", leaderInfo.getBytes());

                        System.out.println("I'm the leader" + leaderInfo);

                        try {
                            // Keep the leader alive
                            Thread.sleep(Long.MAX_VALUE);
                        } catch (InterruptedException e) {
                            System.out.println("Leadership interrupted.");
                        }
                    }
                });

        selector.autoRequeue(); // If we lose leadership, try to get it back later
        selector.start();
    }
}