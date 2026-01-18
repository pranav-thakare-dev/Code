package com.demo.samples.services;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.atomic.AtomicValue;
import org.apache.curator.framework.recipes.atomic.DistributedAtomicLong;
import org.apache.curator.framework.recipes.shared.SharedCount;
import org.apache.curator.framework.recipes.shared.SharedCountListener;
import org.apache.curator.framework.recipes.shared.SharedCountReader;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.retry.RetryNTimes;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class SharedCounterService {

    private final CuratorFramework client;
    private SharedCount sharedCount;
    private DistributedAtomicLong distributedAtomicLong;
    private final List<String> counterEvents = new CopyOnWriteArrayList<>();

    public SharedCounterService(CuratorFramework client) {
        this.client = client;
    }

    @PostConstruct
    public void init() throws Exception {
        // Initialize SharedCount
        sharedCount = new SharedCount(client, "/counters/shared-count", 0);
        
        // Add listener to track changes
        sharedCount.addListener(new SharedCountListener() {
            @Override
            public void countHasChanged(SharedCountReader sharedCount, int newCount) throws Exception {
                String event = "Count changed to: " + newCount + " at " + System.currentTimeMillis();
                counterEvents.add(event);
                System.out.println(event);
            }

            @Override
            public void stateChanged(CuratorFramework client, ConnectionState newState) {
                String event = "Connection state changed to: " + newState;
                counterEvents.add(event);
                System.out.println(event);
            }
        });
        
        sharedCount.start();

        // Initialize DistributedAtomicLong
        distributedAtomicLong = new DistributedAtomicLong(
                client,
                "/counters/atomic-long",
                new RetryNTimes(10, 10)
        );
    }

    // ========== SharedCount Methods ==========

    public Map<String, Object> getSharedCount() {
        Map<String, Object> result = new HashMap<>();
        result.put("count", sharedCount.getCount());
        result.put("versionedValue", sharedCount.getVersionedValue());
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }

    public Map<String, Object> setSharedCount(int newCount) throws Exception {
        Map<String, Object> result = new HashMap<>();
        sharedCount.setCount(newCount);
        result.put("status", "success");
        result.put("newCount", newCount);
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }

    public Map<String, Object> incrementSharedCount() throws Exception {
        Map<String, Object> result = new HashMap<>();
        int currentCount = sharedCount.getCount();
        int newCount = currentCount + 1;
        boolean success = sharedCount.trySetCount(sharedCount.getVersionedValue(), newCount);
        
        result.put("success", success);
        result.put("previousCount", currentCount);
        result.put("newCount", success ? newCount : sharedCount.getCount());
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }

    // ========== DistributedAtomicLong Methods ==========

    public Map<String, Object> getAtomicLong() throws Exception {
        Map<String, Object> result = new HashMap<>();
        AtomicValue<Long> value = distributedAtomicLong.get();
        result.put("succeeded", value.succeeded());
        result.put("value", value.postValue());
        result.put("preValue", value.preValue());
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }

    public Map<String, Object> incrementAtomicLong() throws Exception {
        Map<String, Object> result = new HashMap<>();
        AtomicValue<Long> value = distributedAtomicLong.increment();
        
        result.put("succeeded", value.succeeded());
        result.put("preValue", value.preValue());
        result.put("postValue", value.postValue());
        result.put("timestamp", System.currentTimeMillis());
        
        if (!value.succeeded()) {
            result.put("message", "Increment failed - retry exhausted");
        }
        
        return result;
    }

    public Map<String, Object> decrementAtomicLong() throws Exception {
        Map<String, Object> result = new HashMap<>();
        AtomicValue<Long> value = distributedAtomicLong.decrement();
        
        result.put("succeeded", value.succeeded());
        result.put("preValue", value.preValue());
        result.put("postValue", value.postValue());
        result.put("timestamp", System.currentTimeMillis());
        
        if (!value.succeeded()) {
            result.put("message", "Decrement failed - retry exhausted");
        }
        
        return result;
    }

    public Map<String, Object> addAtomicLong(long delta) throws Exception {
        Map<String, Object> result = new HashMap<>();
        AtomicValue<Long> value = distributedAtomicLong.add(delta);
        
        result.put("succeeded", value.succeeded());
        result.put("delta", delta);
        result.put("preValue", value.preValue());
        result.put("postValue", value.postValue());
        result.put("timestamp", System.currentTimeMillis());
        
        if (!value.succeeded()) {
            result.put("message", "Add operation failed - retry exhausted");
        }
        
        return result;
    }

    public Map<String, Object> setAtomicLong(long newValue) throws Exception {
        Map<String, Object> result = new HashMap<>();
        
        // Try to set the value using compareAndSet
        AtomicValue<Long> currentValue = distributedAtomicLong.get();
        AtomicValue<Long> setValue = distributedAtomicLong.compareAndSet(currentValue.postValue(), newValue);
        
        result.put("succeeded", setValue.succeeded());
        result.put("preValue", setValue.preValue());
        result.put("postValue", setValue.postValue());
        result.put("timestamp", System.currentTimeMillis());
        
        if (!setValue.succeeded()) {
            result.put("message", "Set operation failed - value may have changed");
        }
        
        return result;
    }

    // ========== Event Tracking ==========

    public Map<String, Object> getCounterEvents() {
        Map<String, Object> result = new HashMap<>();
        result.put("events", new ArrayList<>(counterEvents));
        result.put("count", counterEvents.size());
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }

    public Map<String, Object> clearCounterEvents() {
        Map<String, Object> result = new HashMap<>();
        int count = counterEvents.size();
        counterEvents.clear();
        result.put("status", "success");
        result.put("message", "Cleared " + count + " events");
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }

    @PreDestroy
    public void cleanup() throws Exception {
        if (sharedCount != null) {
            sharedCount.close();
        }
    }
}
