package com.demo.samples.services;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessReadWriteLock;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class ReadWriteLockService {

    private final CuratorFramework client;
    private final InterProcessReadWriteLock readWriteLock;

    public ReadWriteLockService(CuratorFramework client) {
        this.client = client;
        this.readWriteLock = new InterProcessReadWriteLock(client, "/locks/read-write-lock");
    }

    public Map<String, Object> acquireReadLock(long timeout) throws Exception {
        Map<String, Object> result = new HashMap<>();
        boolean acquired = readWriteLock.readLock().acquire(timeout, TimeUnit.SECONDS);
        result.put("readLockAcquired", acquired);
        result.put("isAcquiredInThisProcess", readWriteLock.readLock().isAcquiredInThisProcess());
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }

    public Map<String, Object> releaseReadLock() throws Exception {
        Map<String, Object> result = new HashMap<>();
        if (readWriteLock.readLock().isAcquiredInThisProcess()) {
            readWriteLock.readLock().release();
            result.put("readLockReleased", true);
        } else {
            result.put("readLockReleased", false);
            result.put("message", "Read lock was not acquired in this process");
        }
        return result;
    }

    public Map<String, Object> acquireWriteLock(long timeout) throws Exception {
        Map<String, Object> result = new HashMap<>();
        boolean acquired = readWriteLock.writeLock().acquire(timeout, TimeUnit.SECONDS);
        result.put("writeLockAcquired", acquired);
        result.put("isAcquiredInThisProcess", readWriteLock.writeLock().isAcquiredInThisProcess());
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }

    public Map<String, Object> releaseWriteLock() throws Exception {
        Map<String, Object> result = new HashMap<>();
        if (readWriteLock.writeLock().isAcquiredInThisProcess()) {
            readWriteLock.writeLock().release();
            result.put("writeLockReleased", true);
        } else {
            result.put("writeLockReleased", false);
            result.put("message", "Write lock was not acquired in this process");
        }
        return result;
    }

    public Map<String, Object> performRead(String resource) throws Exception {
        Map<String, Object> result = new HashMap<>();
        
        if (readWriteLock.readLock().acquire(10, TimeUnit.SECONDS)) {
            try {
                // Simulate read operation
                Thread.sleep(1000);
                result.put("status", "success");
                result.put("operation", "read");
                result.put("resource", resource);
                result.put("message", "Read operation completed successfully");
            } finally {
                readWriteLock.readLock().release();
            }
        } else {
            result.put("status", "failed");
            result.put("message", "Could not acquire read lock within timeout");
        }
        
        return result;
    }

    public Map<String, Object> performWrite(String resource, String data) throws Exception {
        Map<String, Object> result = new HashMap<>();
        
        if (readWriteLock.writeLock().acquire(10, TimeUnit.SECONDS)) {
            try {
                // Simulate write operation
                Thread.sleep(2000);
                result.put("status", "success");
                result.put("operation", "write");
                result.put("resource", resource);
                result.put("data", data);
                result.put("message", "Write operation completed successfully");
            } finally {
                readWriteLock.writeLock().release();
            }
        } else {
            result.put("status", "failed");
            result.put("message", "Could not acquire write lock within timeout");
        }
        
        return result;
    }

    public Map<String, Object> getLockStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("readLockAcquired", readWriteLock.readLock().isAcquiredInThisProcess());
        status.put("writeLockAcquired", readWriteLock.writeLock().isAcquiredInThisProcess());
        return status;
    }
}
