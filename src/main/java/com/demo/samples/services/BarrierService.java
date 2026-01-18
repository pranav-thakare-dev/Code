package com.demo.samples.services;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.barriers.DistributedBarrier;
import org.apache.curator.framework.recipes.barriers.DistributedDoubleBarrier;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class BarrierService {

    private final CuratorFramework client;
    private final DistributedBarrier distributedBarrier;
    private DistributedDoubleBarrier distributedDoubleBarrier;

    public BarrierService(CuratorFramework client) {
        this.client = client;
        this.distributedBarrier = new DistributedBarrier(client, "/barriers/simple-barrier");
    }

    // ========== DistributedBarrier Methods ==========

    public Map<String, Object> setBarrier() throws Exception {
        Map<String, Object> result = new HashMap<>();
        distributedBarrier.setBarrier();
        result.put("status", "success");
        result.put("message", "Barrier has been set");
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }

    public Map<String, Object> removeBarrier() throws Exception {
        Map<String, Object> result = new HashMap<>();
        distributedBarrier.removeBarrier();
        result.put("status", "success");
        result.put("message", "Barrier has been removed");
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }

    public Map<String, Object> waitOnBarrier(long timeout) throws Exception {
        Map<String, Object> result = new HashMap<>();
        long startTime = System.currentTimeMillis();
        
        if (timeout > 0) {
            boolean success = distributedBarrier.waitOnBarrier(timeout, TimeUnit.SECONDS);
            result.put("barrierCleared", success);
            if (!success) {
                result.put("message", "Barrier wait timed out");
            }
        } else {
            distributedBarrier.waitOnBarrier();
            result.put("barrierCleared", true);
        }
        
        long endTime = System.currentTimeMillis();
        result.put("waitTimeMs", endTime - startTime);
        result.put("timestamp", endTime);
        return result;
    }

    // ========== DistributedDoubleBarrier Methods ==========

    public Map<String, Object> createDoubleBarrier(int memberQty) {
        Map<String, Object> result = new HashMap<>();
        distributedDoubleBarrier = new DistributedDoubleBarrier(client, "/barriers/double-barrier", memberQty);
        result.put("status", "success");
        result.put("message", "Double barrier created with member quantity: " + memberQty);
        result.put("memberQty", memberQty);
        return result;
    }

    public Map<String, Object> enterDoubleBarrier(long timeout) throws Exception {
        Map<String, Object> result = new HashMap<>();
        if (distributedDoubleBarrier == null) {
            result.put("status", "error");
            result.put("message", "Double barrier not initialized. Call /create-double-barrier first.");
            return result;
        }

        long startTime = System.currentTimeMillis();
        boolean entered = distributedDoubleBarrier.enter(timeout, TimeUnit.SECONDS);
        long endTime = System.currentTimeMillis();
        
        result.put("entered", entered);
        result.put("waitTimeMs", endTime - startTime);
        result.put("timestamp", endTime);
        
        if (entered) {
            result.put("message", "Successfully entered the double barrier");
        } else {
            result.put("message", "Failed to enter barrier within timeout");
        }
        
        return result;
    }

    public Map<String, Object> leaveDoubleBarrier(long timeout) throws Exception {
        Map<String, Object> result = new HashMap<>();
        if (distributedDoubleBarrier == null) {
            result.put("status", "error");
            result.put("message", "Double barrier not initialized.");
            return result;
        }

        long startTime = System.currentTimeMillis();
        boolean left = distributedDoubleBarrier.leave(timeout, TimeUnit.SECONDS);
        long endTime = System.currentTimeMillis();
        
        result.put("left", left);
        result.put("waitTimeMs", endTime - startTime);
        result.put("timestamp", endTime);
        
        if (left) {
            result.put("message", "Successfully left the double barrier");
        } else {
            result.put("message", "Failed to leave barrier within timeout");
        }
        
        return result;
    }
}
