# ZooKeeper Curator Recipes - Learning Project

This project demonstrates all major Apache Curator recipes for ZooKeeper with testable REST API endpoints.

## ZooKeeper Curator Recipes Mapping

| ZooKeeper Concept | Curator Recipe                         | Service Class              |
| ----------------- | -------------------------------------- | -------------------------- |
| Leader Election   | `LeaderLatch`, `LeaderSelector`        | `LeaderElectionService`    |
| Distributed Lock  | `InterProcessMutex`                    | `DistributedLockService`   |
| Read/Write Lock   | `InterProcessReadWriteLock`            | `ReadWriteLockService`     |
| Barrier           | `DistributedBarrier`                   | `BarrierService`           |
| Double Barrier    | `DistributedDoubleBarrier`             | `BarrierService`           |
| Queue             | `DistributedQueue`                     | `DistributedQueueService`  |
| Shared Counter    | `SharedCount`, `DistributedAtomicLong` | `SharedCounterService`     |

## Prerequisites

- Java 17+
- Maven
- ZooKeeper running on `localhost:2181`

### Starting ZooKeeper (Docker)

```bash
docker run -d --name zookeeper -p 2181:2181 zookeeper:latest
```

## Running the Application

```bash
./mvnw spring-boot:run
```

## API Endpoints

Access the summary endpoint to see all available recipes:

```
GET http://localhost:8080/zk-test/recipes
```

### 1. Leader Election

**LeaderLatch**
- `GET /zk-test/leader-latch/status` - Check if this instance is the leader

**LeaderSelector**
- `GET /zk-test/leader-selector/status` - Check leadership status

### 2. Distributed Lock (InterProcessMutex)

- `POST /zk-test/lock/acquire?timeout=5` - Acquire distributed lock
- `POST /zk-test/lock/release` - Release distributed lock
- `GET /zk-test/lock/status` - Check lock status
- `POST /zk-test/lock/critical-section?operation=myOp` - Execute critical section with auto lock/unlock

### 3. Read/Write Lock (InterProcessReadWriteLock)

- `POST /zk-test/rwlock/read/acquire?timeout=5` - Acquire read lock
- `POST /zk-test/rwlock/read/release` - Release read lock
- `POST /zk-test/rwlock/write/acquire?timeout=5` - Acquire write lock
- `POST /zk-test/rwlock/write/release` - Release write lock
- `GET /zk-test/rwlock/status` - Check R/W lock status
- `POST /zk-test/rwlock/read?resource=myResource` - Perform read operation
- `POST /zk-test/rwlock/write?resource=myResource&data=myData` - Perform write operation

### 4. Barrier (DistributedBarrier)

- `POST /zk-test/barrier/set` - Set a barrier
- `POST /zk-test/barrier/remove` - Remove the barrier
- `POST /zk-test/barrier/wait?timeout=10` - Wait on barrier (blocks until removed)

### 5. Double Barrier (DistributedDoubleBarrier)

- `POST /zk-test/double-barrier/create?memberQty=3` - Create double barrier with member count
- `POST /zk-test/double-barrier/enter?timeout=30` - Enter barrier (waits for all members)
- `POST /zk-test/double-barrier/leave?timeout=30` - Leave barrier (waits for all members)

### 6. Queue (DistributedQueue)

- `POST /zk-test/queue/put?message=hello` - Add message to queue
- `GET /zk-test/queue/consumed` - Get consumed messages
- `POST /zk-test/queue/clear` - Clear consumed messages

### 7. SharedCount

- `GET /zk-test/counter/shared/get` - Get shared counter value
- `POST /zk-test/counter/shared/set?value=10` - Set shared counter value
- `POST /zk-test/counter/shared/increment` - Increment shared counter

### 8. DistributedAtomicLong

- `GET /zk-test/counter/atomic/get` - Get atomic long value
- `POST /zk-test/counter/atomic/increment` - Increment atomic long
- `POST /zk-test/counter/atomic/decrement` - Decrement atomic long
- `POST /zk-test/counter/atomic/add?delta=5` - Add delta to atomic long
- `POST /zk-test/counter/atomic/set?value=100` - Set atomic long value
- `GET /zk-test/counter/events` - Get counter change events
- `POST /zk-test/counter/events/clear` - Clear event log

## Testing Examples

### Test Leader Election
```bash
# Check leader status
curl http://localhost:8080/zk-test/leader-latch/status
```

### Test Distributed Lock
```bash
# Acquire lock
curl -X POST "http://localhost:8080/zk-test/lock/acquire?timeout=5"

# Check status
curl http://localhost:8080/zk-test/lock/status

# Release lock
curl -X POST http://localhost:8080/zk-test/lock/release
```

### Test Distributed Queue
```bash
# Put messages
curl -X POST "http://localhost:8080/zk-test/queue/put?message=hello"
curl -X POST "http://localhost:8080/zk-test/queue/put?message=world"

# Check consumed messages
curl http://localhost:8080/zk-test/queue/consumed
```

### Test Shared Counter
```bash
# Set counter
curl -X POST "http://localhost:8080/zk-test/counter/shared/set?value=10"

# Increment
curl -X POST http://localhost:8080/zk-test/counter/shared/increment

# Get value
curl http://localhost:8080/zk-test/counter/shared/get
```

### Test Double Barrier
```bash
# Create barrier for 2 members
curl -X POST "http://localhost:8080/zk-test/double-barrier/create?memberQty=2"

# In one terminal
curl -X POST "http://localhost:8080/zk-test/double-barrier/enter?timeout=30"

# In another terminal (or another instance)
curl -X POST "http://localhost:8080/zk-test/double-barrier/enter?timeout=30"

# Both will proceed once both have entered
```

## Multi-Instance Testing

To test distributed behavior, run multiple instances:

```bash
# Terminal 1
./mvnw spring-boot:run

# Terminal 2 (different port)
./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
```

Then test coordination between instances using the different ports.

## Project Structure

```
src/main/java/com/demo/samples/
├── Application.java                    # Main Spring Boot application
├── ZookeeperTestController.java        # REST API endpoints
├── config/
│   └── ZookeeperConfig.java           # ZooKeeper configuration
└── services/
    ├── LeaderElectionService.java     # Leader election recipes
    ├── DistributedLockService.java    # Distributed lock recipes
    ├── ReadWriteLockService.java      # Read/Write lock recipes
    ├── BarrierService.java            # Barrier recipes
    ├── DistributedQueueService.java   # Queue recipes
    └── SharedCounterService.java      # Counter recipes
```

## Learning Path

1. **Start Simple**: Begin with `SharedCount` and `DistributedAtomicLong` to understand distributed state
2. **Synchronization**: Try `InterProcessMutex` for basic locking
3. **Advanced Locking**: Experiment with `InterProcessReadWriteLock` for concurrent reads
4. **Coordination**: Use `Barrier` and `DoubleBarrier` for synchronization points
5. **Leader Election**: Test `LeaderLatch` and `LeaderSelector` with multiple instances
6. **Messaging**: Use `DistributedQueue` for distributed messaging

## Notes

- All services are auto-started with the application
- Listeners and consumers run in the background
- ZooKeeper must be running before starting the application
- For production use, add proper error handling and monitoring
