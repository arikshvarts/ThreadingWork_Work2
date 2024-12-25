package bgu.spl.mics;
import java.util.ArrayList;
import java.util.concurrent.*;
/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBusImpl implements MessageBus {


    private ConcurrentHashMap<MicroService, BlockingQueue<Message>> MicroServices_Queues = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Class<? extends Broadcast>, ArrayList<MicroService>> broadcast_Subscribers = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Class<? extends Event>, BlockingQueue<MicroService>> event_Subscribers = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Event<?>, Future<?>> FutureToEvent = new ConcurrentHashMap<>();

    // Private constructor to ensure class is a singleton
    private MessageBusImpl() {}

    // Static nested class for holding the instance
    private static class MessageBusHelper {
        private static final MessageBusImpl INSTANCE = new MessageBusImpl();
    }

    // Public method to access the singleton instance
    public static MessageBusImpl getInstance() {
        return MessageBusHelper.INSTANCE;
    }


	@Override
    public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
        if(event_Subscribers.containsKey(type) == true){
            event_Subscribers.get(type).add(m);
        }
        else{ //if this type of event doesn't already exist in the hashmap
			BlockingQueue<MicroService> HashMap_to_add = new LinkedBlockingQueue<MicroService>();
            HashMap_to_add.add(m);
			event_Subscribers.put(type, HashMap_to_add);
        }

    }

    @Override
    public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
        if(broadcast_Subscribers.containsKey(type) == true){
            broadcast_Subscribers.get(type).add(m);
        }
        else{ //if this type of broadcast doesn't already exist in the hashmap
            ArrayList<MicroService> list_to_add = new ArrayList<MicroService>();
            list_to_add.add(m);
            broadcast_Subscribers.put(type, list_to_add);
        }
        
    }

    @Override
    public <T> void complete(Event<T> e, T result) {	
		Future<T> future = (Future<T>) FutureToEvent.get(e);
        if(future == null){
            throw new IllegalStateException("This event is not registered");  
        }
		future.resolve(result);
    }

    @Override
    public void sendBroadcast(Broadcast b) {
		if(broadcast_Subscribers.containsKey(b.getClass()) == true){
			for(MicroService m : broadcast_Subscribers.get(b.getClass())){
				MicroServices_Queues.get(m).add(b);
			}
		}
    }

    @Override
public synchronized <T> Future<T> sendEvent(Event<T> event) {
    Class<? extends Event> eventType = event.getClass(); // Get the event type

    // Fetch the queue of subscribers for this event type
    BlockingQueue<MicroService> subscribers = event_Subscribers.get(event);        
    // If no subscribers are registered, return null
    if (subscribers == null || subscribers.isEmpty()) {
        return null;
    }

    // Get the next microservice in the round-robin queue // Synchronize access to the queue to ensure thread safety
        MicroService targetMicroservice = subscribers.poll(); // Remove from the head
        subscribers.add(targetMicroservice); // Add back to the tail for round-robin logic

        // Fetch the message queue for the target microservice
        BlockingQueue<Message> targetQueue = MicroServices_Queues.get(targetMicroservice);
        if (targetQueue != null) {
            Future<T> future = new Future<>(); 
			FutureToEvent.put(event, future);
			try {
                targetQueue.put(event); 
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); 
                return null;
            }

            return future; // Return the Future to the sender
        }

    return null; // Return null if something went wrong
}


    @Override
    public void register(MicroService m) {
        LinkedBlockingQueue<Message> queue_to_add = new LinkedBlockingQueue<Message>();
        //if this name doesnt exist as a key in the HashMap, add it to the map with a value of an empty queue
        MicroServices_Queues.putIfAbsent(m, queue_to_add);
    }

    @Override
    public synchronized void unregister(MicroService m) {
		MicroServices_Queues.remove(m);
		for (ArrayList<MicroService> ls : broadcast_Subscribers.values()) {
			if(ls.contains(m))
			ls.remove(m);
		}
		for (BlockingQueue<MicroService> ls  : event_Subscribers.values()) {
			if(ls.contains(m))
			ls.remove(m);
		}	
}


@Override
public Message awaitMessage(MicroService m) throws InterruptedException {
    BlockingQueue<Message> queue = MicroServices_Queues.get(m);
    if (queue == null) {
        throw new IllegalStateException("This MicroService is not registered");
    }
    return queue.take();
}
}
