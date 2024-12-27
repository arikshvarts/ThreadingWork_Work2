package bgu.spl.mics;
import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBusImpl implements MessageBus {
    
        private final ConcurrentHashMap<MicroService, BlockingQueue<Message>> MicroServices_Queues = new ConcurrentHashMap<>();
        private final ConcurrentHashMap<Class<? extends Broadcast>, ArrayList<MicroService>> broadcast_Subscribers = new ConcurrentHashMap<>();
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
            rwLock.readLock().lock();
            try {
                event_Subscribers.computeIfAbsent(type, k -> new LinkedBlockingQueue<>()).add(m);
            } finally {
                rwLock.readLock().unlock();
            }
        }
    
        @Override
        public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
            rwLock.readLock().lock();
            try {
                broadcast_Subscribers.computeIfAbsent(type, k -> new ArrayList<>()).add(m);
            } finally {
                rwLock.readLock().unlock();
            }
        }
    
        @Override
        public <T> void complete(Event<T> e, T result) {
            rwLock.readLock().lock();
            try {
                Future<T> future = (Future<T>) FutureToEvent.get(e);
                if (future != null) {
                    future.resolve(result);
                } else {
                    throw new IllegalStateException("This event is not registered");
                }
            } finally {
                rwLock.readLock().unlock();
            }
        }
    
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
    
        @Override
        public void register(MicroService m) {
            rwLock.readLock().lock();
            try {
                MicroServices_Queues.putIfAbsent(m, new LinkedBlockingQueue<>());
            } finally {
                rwLock.readLock().unlock();
            }
        }
    
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
    
        @Override
        public Message awaitMessage(MicroService m) throws InterruptedException {
            rwLock.readLock().lock();
            try {
                BlockingQueue<Message> queue = MicroServices_Queues.get(m);
                if (queue == null) {
                    throw new IllegalStateException("This MicroService is not registered");
                }
                return queue.take();
            } finally {
                rwLock.readLock().unlock();
            }
        }
    }
    

