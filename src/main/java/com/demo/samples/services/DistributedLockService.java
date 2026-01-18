package com.demo.samples.services;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class DistributedLockService {

    private final CuratorFramework client;
    private final InterProcessMutex lock;

    public DistributedLockService(CuratorFramework client) {
        this.client = client;
        this.lock = new InterProcessMutex(client, "/locks/distributed-lock");
    }

    public Map<String, Object> acquireLock(long timeout) throws Exception {
        Map<String, Object> result = new HashMap<>();
        boolean acquired = lock.acquire(timeout, TimeUnit.SECONDS);
        result.put("lockAcquired", acquired);
        result.put("isAcquiredInThisProcess", lock.isAcquiredInThisProcess());
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }

    public Map<String, Object> releaseLock() throws Exception {
        Map<String, Object> result = new HashMap<>();
        if (lock.isAcquiredInThisProcess()) {
            lock.release();
            result.put("lockReleased", true);
        } else {
            result.put("lockReleased", false);
            result.put("message", "Lock was not acquired in this process");
        }
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }

    public Map<String, Object> getLockStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("isAcquiredInThisProcess", lock.isAcquiredInThisProcess());
        return status;
    }

    public Map<String, Object> performCriticalSection(String operation) throws Exception {
        Map<String, Object> result = new HashMap<>();
        
        if (lock.acquire(10, TimeUnit.SECONDS)) {
            try {
                // Simulate critical section work
                Thread.sleep(2000);
                result.put("status", "success");
                result.put("operation", operation);
                result.put("message", "Critical section executed successfully");
            } finally {
                lock.release();
            }
        } else {
            result.put("status", "failed");
            result.put("message", "Could not acquire lock within timeout");
        }
        
        return result;
    }
}
