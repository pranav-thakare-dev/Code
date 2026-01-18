package com.demo.samples.services;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class LeaderElectionService {

    private final CuratorFramework client;
    private LeaderLatch leaderLatch;
    private LeaderSelector leaderSelector;
    private final AtomicInteger leaderCount = new AtomicInteger(0);

    public LeaderElectionService(CuratorFramework client) {
        this.client = client;
    }

    @PostConstruct
    public void init() throws Exception {
        // Initialize LeaderLatch
        leaderLatch = new LeaderLatch(client, "/leader/latch", "instance-" + System.currentTimeMillis());
        leaderLatch.start();

        // Initialize LeaderSelector
        leaderSelector = new LeaderSelector(client, "/leader/selector", new LeaderSelectorListenerAdapter() {
            @Override
            public void takeLeadership(CuratorFramework client) throws Exception {
                // This callback will be invoked when this instance becomes the leader
                leaderCount.incrementAndGet();
                System.out.println("I am the leader now via LeaderSelector!");
                // Simulate some work as leader
                Thread.sleep(5000);
            }
        });
        leaderSelector.autoRequeue(); // Important: Requeue after leadership is lost
        leaderSelector.start();
    }

    public Map<String, Object> getLeaderLatchStatus() throws Exception {
        Map<String, Object> status = new HashMap<>();
        status.put("isLeader", leaderLatch.hasLeadership());
        status.put("currentLeaderId", leaderLatch.getLeader().getId());
        status.put("participants", leaderLatch.getParticipants().size());
        return status;
    }

    public Map<String, Object> getLeaderSelectorStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("hasLeadership", leaderSelector.hasLeadership());
        status.put("leaderCount", leaderCount.get());
//        status.put("isStarted", leaderSelector.getLeader().getId());
        return status;
    }

    public boolean waitForLeadership(long timeout, TimeUnit unit) throws InterruptedException {
        return leaderLatch.await(timeout, unit);
    }

    @PreDestroy
    public void cleanup() throws Exception {
        if (leaderLatch != null) {
            leaderLatch.close();
        }
        if (leaderSelector != null) {
            leaderSelector.close();
        }
    }
}
