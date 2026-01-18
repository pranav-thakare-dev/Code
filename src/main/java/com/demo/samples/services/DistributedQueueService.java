package com.demo.samples.services;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.queue.DistributedQueue;
import org.apache.curator.framework.recipes.queue.QueueBuilder;
import org.apache.curator.framework.recipes.queue.QueueConsumer;
import org.apache.curator.framework.recipes.queue.QueueSerializer;
import org.apache.curator.framework.state.ConnectionState;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class DistributedQueueService {

    private final CuratorFramework client;
    private DistributedQueue<String> queue;
    private final ConcurrentLinkedQueue<String> consumedMessages = new ConcurrentLinkedQueue<>();

    public DistributedQueueService(CuratorFramework client) {
        this.client = client;
    }

    @PostConstruct
    public void init() throws Exception {
        // Custom serializer for String messages
        QueueSerializer<String> serializer = new QueueSerializer<String>() {
            @Override
            public byte[] serialize(String item) {
                return item.getBytes(StandardCharsets.UTF_8);
            }

            @Override
            public String deserialize(byte[] bytes) {
                return new String(bytes, StandardCharsets.UTF_8);
            }
        };

        // Custom consumer to handle consumed messages
        QueueConsumer<String> consumer = new QueueConsumer<String>() {
            @Override
            public void consumeMessage(String message) throws Exception {
                consumedMessages.add(message);
                System.out.println("Consumed message: " + message);
            }

            @Override
            public void stateChanged(CuratorFramework client, ConnectionState newState) {
                System.out.println("Connection state changed to: " + newState);
            }
        };

        // Build the distributed queue
        queue = QueueBuilder.builder(client, consumer, serializer, "/queues/distributed-queue")
                .buildQueue();
        
        queue.start();
    }

    public Map<String, Object> putMessage(String message) throws Exception {
        Map<String, Object> result = new HashMap<>();
        queue.put(message);
        result.put("status", "success");
        result.put("message", "Message added to queue: " + message);
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }

    public Map<String, Object> getConsumedMessages() {
        Map<String, Object> result = new HashMap<>();
        List<String> messages = new ArrayList<>(consumedMessages);
        result.put("consumedMessages", messages);
        result.put("count", messages.size());
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }

    public Map<String, Object> clearConsumedMessages() {
        Map<String, Object> result = new HashMap<>();
        int count = consumedMessages.size();
        consumedMessages.clear();
        result.put("status", "success");
        result.put("message", "Cleared " + count + " consumed messages");
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }

    @PreDestroy
    public void cleanup() throws Exception {
        if (queue != null) {
            queue.close();
        }
    }
}
