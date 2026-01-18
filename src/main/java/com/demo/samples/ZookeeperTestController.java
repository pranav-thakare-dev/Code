package com.demo.samples;

import com.demo.samples.services.*;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/zk-test")
public class ZookeeperTestController {

    private final CuratorFramework client;
    private final ServiceDiscovery<Void> serviceDiscovery;
    private final LeaderElectionService leaderElectionService;
    private final DistributedLockService distributedLockService;
    private final ReadWriteLockService readWriteLockService;
    private final BarrierService barrierService;
    private final SharedCounterService sharedCounterService;

    public ZookeeperTestController(
            CuratorFramework client,
            ServiceDiscovery<Void> serviceDiscovery,
            LeaderElectionService leaderElectionService,
            DistributedLockService distributedLockService,
            ReadWriteLockService readWriteLockService,
            BarrierService barrierService,
            SharedCounterService sharedCounterService) {
        this.client = client;
        this.serviceDiscovery = serviceDiscovery;
        this.leaderElectionService = leaderElectionService;
        this.distributedLockService = distributedLockService;
        this.readWriteLockService = readWriteLockService;
        this.barrierService = barrierService;
        this.sharedCounterService = sharedCounterService;
    }

    // ========== Basic ZooKeeper Tests ==========

    @GetMapping("/services")
    public List<String> getActiveServices() throws Exception {
        return serviceDiscovery.queryForInstances("order-service")
                .stream()
                .map(instance -> instance.getAddress() + ":" + instance.getPort())
                .collect(Collectors.toList());
    }

    @GetMapping("/leader")
    public String getLeader() throws Exception {
        byte[] data = client.getData().forPath("/leader/job-manager");
        return data != null ? new String(data) : "Leader path exists, but no data set.";
    }

    // ========== Leader Election Endpoints ==========

    @GetMapping("/leader-latch/status")
    public Map<String, Object> getLeaderLatchStatus() throws Exception {
        return leaderElectionService.getLeaderLatchStatus();
    }

    @GetMapping("/leader-selector/status")
    public Map<String, Object> getLeaderSelectorStatus() {
        return leaderElectionService.getLeaderSelectorStatus();
    }

    // ========== Distributed Lock Endpoints ==========

    @PostMapping("/lock/acquire")
    public Map<String, Object> acquireLock(@RequestParam(defaultValue = "5") long timeout) throws Exception {
        return distributedLockService.acquireLock(timeout);
    }

    @PostMapping("/lock/release")
    public Map<String, Object> releaseLock() throws Exception {
        return distributedLockService.releaseLock();
    }

    @GetMapping("/lock/status")
    public Map<String, Object> getLockStatus() {
        return distributedLockService.getLockStatus();
    }

    @PostMapping("/lock/critical-section")
    public Map<String, Object> performCriticalSection(@RequestParam String operation) throws Exception {
        return distributedLockService.performCriticalSection(operation);
    }

    // ========== Read/Write Lock Endpoints ==========

    @PostMapping("/rwlock/read/acquire")
    public Map<String, Object> acquireReadLock(@RequestParam(defaultValue = "5") long timeout) throws Exception {
        return readWriteLockService.acquireReadLock(timeout);
    }

    @PostMapping("/rwlock/read/release")
    public Map<String, Object> releaseReadLock() throws Exception {
        return readWriteLockService.releaseReadLock();
    }

    @PostMapping("/rwlock/write/acquire")
    public Map<String, Object> acquireWriteLock(@RequestParam(defaultValue = "5") long timeout) throws Exception {
        return readWriteLockService.acquireWriteLock(timeout);
    }

    @PostMapping("/rwlock/write/release")
    public Map<String, Object> releaseWriteLock() throws Exception {
        return readWriteLockService.releaseWriteLock();
    }

    @GetMapping("/rwlock/status")
    public Map<String, Object> getRWLockStatus() {
        return readWriteLockService.getLockStatus();
    }

    @PostMapping("/rwlock/read")
    public Map<String, Object> performRead(@RequestParam String resource) throws Exception {
        return readWriteLockService.performRead(resource);
    }

    @PostMapping("/rwlock/write")
    public Map<String, Object> performWrite(@RequestParam String resource, @RequestParam String data) throws Exception {
        return readWriteLockService.performWrite(resource, data);
    }

    // ========== Barrier Endpoints ==========

    @PostMapping("/barrier/set")
    public Map<String, Object> setBarrier() throws Exception {
        return barrierService.setBarrier();
    }

    @PostMapping("/barrier/remove")
    public Map<String, Object> removeBarrier() throws Exception {
        return barrierService.removeBarrier();
    }

    @PostMapping("/barrier/wait")
    public Map<String, Object> waitOnBarrier(@RequestParam(defaultValue = "10") long timeout) throws Exception {
        return barrierService.waitOnBarrier(timeout);
    }

    // ========== Double Barrier Endpoints ==========

    @PostMapping("/double-barrier/create")
    public Map<String, Object> createDoubleBarrier(@RequestParam int memberQty) {
        return barrierService.createDoubleBarrier(memberQty);
    }

    @PostMapping("/double-barrier/enter")
    public Map<String, Object> enterDoubleBarrier(@RequestParam(defaultValue = "30") long timeout) throws Exception {
        return barrierService.enterDoubleBarrier(timeout);
    }

    @PostMapping("/double-barrier/leave")
    public Map<String, Object> leaveDoubleBarrier(@RequestParam(defaultValue = "30") long timeout) throws Exception {
        return barrierService.leaveDoubleBarrier(timeout);
    }


    // ========== SharedCount Endpoints ==========

    @GetMapping("/counter/shared/get")
    public Map<String, Object> getSharedCount() {
        return sharedCounterService.getSharedCount();
    }

    @PostMapping("/counter/shared/set")
    public Map<String, Object> setSharedCount(@RequestParam int value) throws Exception {
        return sharedCounterService.setSharedCount(value);
    }

    @PostMapping("/counter/shared/increment")
    public Map<String, Object> incrementSharedCount() throws Exception {
        return sharedCounterService.incrementSharedCount();
    }

    // ========== DistributedAtomicLong Endpoints ==========

    @GetMapping("/counter/atomic/get")
    public Map<String, Object> getAtomicLong() throws Exception {
        return sharedCounterService.getAtomicLong();
    }

    @PostMapping("/counter/atomic/increment")
    public Map<String, Object> incrementAtomicLong() throws Exception {
        return sharedCounterService.incrementAtomicLong();
    }

    @PostMapping("/counter/atomic/decrement")
    public Map<String, Object> decrementAtomicLong() throws Exception {
        return sharedCounterService.decrementAtomicLong();
    }

    @PostMapping("/counter/atomic/add")
    public Map<String, Object> addAtomicLong(@RequestParam long delta) throws Exception {
        return sharedCounterService.addAtomicLong(delta);
    }

    @PostMapping("/counter/atomic/set")
    public Map<String, Object> setAtomicLong(@RequestParam long value) throws Exception {
        return sharedCounterService.setAtomicLong(value);
    }

    @GetMapping("/counter/events")
    public Map<String, Object> getCounterEvents() {
        return sharedCounterService.getCounterEvents();
    }

    @PostMapping("/counter/events/clear")
    public Map<String, Object> clearCounterEvents() {
        return sharedCounterService.clearCounterEvents();
    }

    // ========== Summary Endpoint ==========

    @GetMapping("/recipes")
    public Map<String, Object> getAllRecipes() {
        Map<String, Object> recipes = new HashMap<>();
        
        Map<String, String> leaderElection = new HashMap<>();
        leaderElection.put("LeaderLatch", "GET /zk-test/leader-latch/status");
        leaderElection.put("LeaderSelector", "GET /zk-test/leader-selector/status");
        recipes.put("Leader Election", leaderElection);
        
        Map<String, String> locks = new HashMap<>();
        locks.put("InterProcessMutex - Acquire", "POST /zk-test/lock/acquire?timeout=5");
        locks.put("InterProcessMutex - Release", "POST /zk-test/lock/release");
        locks.put("InterProcessMutex - Status", "GET /zk-test/lock/status");
        locks.put("InterProcessMutex - Critical Section", "POST /zk-test/lock/critical-section?operation=myOp");
        recipes.put("Distributed Lock", locks);
        
        Map<String, String> rwLocks = new HashMap<>();
        rwLocks.put("Read Lock - Acquire", "POST /zk-test/rwlock/read/acquire?timeout=5");
        rwLocks.put("Read Lock - Release", "POST /zk-test/rwlock/read/release");
        rwLocks.put("Write Lock - Acquire", "POST /zk-test/rwlock/write/acquire?timeout=5");
        rwLocks.put("Write Lock - Release", "POST /zk-test/rwlock/write/release");
        rwLocks.put("Read Operation", "POST /zk-test/rwlock/read?resource=myResource");
        rwLocks.put("Write Operation", "POST /zk-test/rwlock/write?resource=myResource&data=myData");
        rwLocks.put("Status", "GET /zk-test/rwlock/status");
        recipes.put("Read/Write Lock", rwLocks);
        
        Map<String, String> barriers = new HashMap<>();
        barriers.put("Set Barrier", "POST /zk-test/barrier/set");
        barriers.put("Remove Barrier", "POST /zk-test/barrier/remove");
        barriers.put("Wait on Barrier", "POST /zk-test/barrier/wait?timeout=10");
        recipes.put("Barrier", barriers);
        
        Map<String, String> doubleBarriers = new HashMap<>();
        doubleBarriers.put("Create", "POST /zk-test/double-barrier/create?memberQty=3");
        doubleBarriers.put("Enter", "POST /zk-test/double-barrier/enter?timeout=30");
        doubleBarriers.put("Leave", "POST /zk-test/double-barrier/leave?timeout=30");
        recipes.put("Double Barrier", doubleBarriers);

        Map<String, String> sharedCount = new HashMap<>();
        sharedCount.put("Get", "GET /zk-test/counter/shared/get");
        sharedCount.put("Set", "POST /zk-test/counter/shared/set?value=10");
        sharedCount.put("Increment", "POST /zk-test/counter/shared/increment");
        recipes.put("SharedCount", sharedCount);
        
        Map<String, String> atomicLong = new HashMap<>();
        atomicLong.put("Get", "GET /zk-test/counter/atomic/get");
        atomicLong.put("Increment", "POST /zk-test/counter/atomic/increment");
        atomicLong.put("Decrement", "POST /zk-test/counter/atomic/decrement");
        atomicLong.put("Add", "POST /zk-test/counter/atomic/add?delta=5");
        atomicLong.put("Set", "POST /zk-test/counter/atomic/set?value=100");
        atomicLong.put("Get Events", "GET /zk-test/counter/events");
        atomicLong.put("Clear Events", "POST /zk-test/counter/events/clear");
        recipes.put("DistributedAtomicLong", atomicLong);
        
        return recipes;
    }
}
