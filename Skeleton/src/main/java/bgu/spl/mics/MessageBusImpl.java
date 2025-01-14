package bgu.spl.mics;
import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */

import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.objects.StatisticalFolder;
public class MessageBusImpl implements MessageBus {
    
        private final ConcurrentHashMap<MicroService, BlockingQueue<Message>> MicroServices_Queues = new ConcurrentHashMap<>();
        private final ConcurrentHashMap<Class<? extends Broadcast>, CopyOnWriteArrayList<MicroService>> broadcast_Subscribers = new ConcurrentHashMap<>();
        private final ConcurrentHashMap<Class<? extends Event>, BlockingQueue<MicroService>> event_Subscribers = new ConcurrentHashMap<>();
        private final ConcurrentHashMap<Event<?>, Future<?>> FutureToEvent = new ConcurrentHashMap<>();
        private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    
        // Singleton instance
        private MessageBusImpl() {}
    
        private static class MessageBusHelper {
            private static final MessageBusImpl INSTANCE = new MessageBusImpl();
        }
    
        public static MessageBusImpl getInstance() {
            return MessageBusHelper.INSTANCE;
        }
    
        @Override
        public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
      
            event_Subscribers.computeIfAbsent(type, k -> new LinkedBlockingQueue<>()).add(m);
            System.out.println(m.getName() + "is subscribing to: " + type.getName());                    
        } 
    
        @Override
        public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {

                broadcast_Subscribers.computeIfAbsent(type, k -> new CopyOnWriteArrayList<>()).add(m);
        }
    
        @Override
        public <T> void complete(Event<T> e, T result) {
                Future<T> future = (Future<T>) FutureToEvent.get(e);
                if (future != null) {
                    future.resolve(result);
                } else {
                    throw new IllegalStateException("This event is not registered");
                }
            } 
        
            /**
 * Sends a broadcast message to all microservices subscribed to the broadcast type.
 *
 * Preconditions:
 * - `b` must be a non-null instance of a `Broadcast` message.
 * - There must be at least one microservice subscribed to the broadcast type.
 *
 * Postconditions:
 * - All registered microservices subscribed to the broadcast type receive the broadcast message.
 * - If no microservices are subscribed, no messages are sent.
 *
 * Invariants:
 * - A broadcast message is **not stored** and is only delivered to currently subscribed services.
 * - The integrity of the `broadcast_Subscribers` collection must remain consistent during delivery.
 */
    
        @Override
        public void sendBroadcast(Broadcast b) {
            rwLock.readLock().lock();
            try {
                if (broadcast_Subscribers.containsKey(b.getClass())) {
                    for (MicroService m : broadcast_Subscribers.get(b.getClass())) {
                        MicroServices_Queues.get(m).add(b);
                    }
                }
            } finally {
                rwLock.readLock().unlock();
            }
        }
    
        @Override
        public <T> Future<T> sendEvent(Event<T> event) {
            try {
                rwLock.readLock().lock();
                BlockingQueue<MicroService> subscribers = event_Subscribers.get(event.getClass());
                if (subscribers == null || subscribers.isEmpty()) {
                    System.err.println("No subscribers found for event: " + event.getClass().getName());                    
                    return null;
                }
                try {
                    rwLock.readLock().unlock();
                    rwLock.writeLock().lock();
                    MicroService targetMicroservice = subscribers.poll();
                    subscribers.add(targetMicroservice);
                    BlockingQueue<Message> targetQueue = MicroServices_Queues.get(targetMicroservice);
                    rwLock.writeLock().unlock();
                    rwLock.readLock().lock();

                    if (targetQueue != null) {
                        Future<T> future = new Future<>();
                        FutureToEvent.put(event, future);
                        targetQueue.put(event);
                        return future;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            } finally {
                if (rwLock.getReadHoldCount() > 0) {
                    rwLock.readLock().unlock();
                }
            }
            
            return null;
        }
    
        /**
 * Registers a new microservice in the message bus by allocating a message queue for it.
 *
 * Preconditions:
 * - `m` must be a non-null instance of `MicroService`.
 * - The microservice should not already be registered.
 *
 * Postconditions:
 * - A new message queue is created and associated with the microservice in `MicroServices_Queues`.
 * - No duplicate message queues should exist for the same microservice.
 *
 * Invariants:
 * - Each microservice should have exactly **one** message queue associated with it in `MicroServices_Queues`.
 * - No message queue should be shared between multiple microservices.
 */

        @Override
        public void register(MicroService m) {
                MicroServices_Queues.putIfAbsent(m, new LinkedBlockingQueue<>());

        }
    
/**
 * Unregisters a microservice by removing its message queue and cleaning references from all subscriptions.
 *
 * Preconditions:
 * - `m` must be a non-null instance of `MicroService`.
 * - The microservice must be registered prior to calling this method.
 *
 * Postconditions:
 * - The microservice's message queue is removed from `MicroServices_Queues`.
 * - The microservice is removed from all `broadcast_Subscribers` and `event_Subscribers`.
 * - If the microservice was not registered, no actions are performed.
 *
 * Invariants:
 * - After the call, the microservice should no longer receive any messages or broadcasts.
 * - If a microservice is re-registered, a new queue must be created without using the old one.
 */

        @Override
        public void unregister(MicroService m) {
            rwLock.writeLock().lock();
            try {
                MicroServices_Queues.remove(m);
                broadcast_Subscribers.values().forEach(list -> list.remove(m));
                event_Subscribers.values().forEach(queue -> queue.remove(m));
            } finally {
                rwLock.writeLock().unlock();
            }
        }
        
        public Boolean isRegistered(MicroService m){
            return(MicroServices_Queues.containsKey(m));
        }
    
        @Override
        public Message awaitMessage(MicroService m) throws InterruptedException {
                BlockingQueue<Message> queue = MicroServices_Queues.get(m);
                if (queue == null) {
                    throw new IllegalStateException("This MicroService is not registered");
                }
                return queue.take();
        }

        public int getnumListeners(Class<? extends Message> type) {
            return event_Subscribers.get(type).size();
        }
    }
    

