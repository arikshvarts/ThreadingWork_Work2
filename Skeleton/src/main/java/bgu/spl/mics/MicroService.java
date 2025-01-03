package bgu.spl.mics;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import bgu.spl.mics.application.objects.ServiceCounter;
import bgu.spl.mics.application.objects.StatisticalFolder;

/**
 * The MicroService is an abstract class that any micro-service in the system
 * must extend. The abstract MicroService class is responsible to get and
 * manipulate the singleton {@link MessageBus} instance.
 * <p>
 * Derived classes of MicroService should never directly touch the message-bus.
 * Instead, they have a set of internal protected wrapping methods (e.g.,
 * {@link #sendBroadcast(bgu.spl.mics.Broadcast)}, {@link #sendBroadcast(bgu.spl.mics.Broadcast)},
 * etc.) they can use. When subscribing to message-types,
 * the derived class also supplies a {@link Callback} that should be called when
 * a message of the subscribed type was taken from the micro-service
 * message-queue (see {@link MessageBus#register(bgu.spl.mics.MicroService)}
 * method). The abstract MicroService stores this callback together with the
 * type of the message is related to.
 * 
 * Only private fields and methods may be added to this class.
 * <p>
 */
public abstract class MicroService implements Runnable {

    private boolean terminated = false;
    private final String name;
    protected ConcurrentHashMap<Class<? extends Message>, Callback<?>> messageCallBack = new ConcurrentHashMap<>();
    private MessageBusImpl msg_bus;
    protected StatisticalFolder statsManager;
    protected CountDownLatch latch;

    /**
     * @param name the micro-service name (used mainly for debugging purposes -
     *             does not have to be unique)
     */
    public MicroService(String name, CountDownLatch latch) {
        this.name = name;
        this.msg_bus = MessageBusImpl.getInstance();
        this.statsManager = StatisticalFolder.getInstance();
        this.latch = latch;

    }

    /**
     * Subscribes to events of type {@code type} with the callback
     * {@code callback}. This means two things:
     * 1. Subscribe to events in the singleton event-bus using the supplied
     * {@code type}
     * 2. Store the {@code callback} so that when events of type {@code type}
     * are received it will be called.
     * <p>
     * For a received message {@code m} of type {@code type = m.getClass()}
     * calling the callback {@code callback} means running the method
     * {@link Callback#call(java.lang.Object)} by calling
     * {@code callback.call(m)}.
     * <p>
     * @param <E>      The type of event to subscribe to.
     * @param <T>      The type of result expected for the subscribed event.
     * @param type     The {@link Class} representing the type of event to
     *                 subscribe to.
     * @param callback The callback that should be called when messages of type
     *                 {@code type} are taken from this micro-service message
     *                 queue.
     */
    protected final <T, E extends Event<T>> void subscribeEvent(Class<E> type, Callback<E> callback) {
    // Register the callback for the given event type
    messageCallBack.put(type, callback);

    // Register this microservice's subscription with the singleton MessageBus
    msg_bus.subscribeEvent(type, this);
}
        

    /**
     * Subscribes to broadcast message of type {@code type} with the callback
     * {@code callback}. This means two things:
     * 1. Subscribe to broadcast messages in the singleton event-bus using the
     * supplied {@code type}
     * 2. Store the {@code callback} so that when broadcast messages of type
     * {@code type} received it will be called.
     * <p>
     * For a received message {@code m} of type {@code type = m.getClass()}
     * calling the callback {@code callback} means running the method
     * {@link Callback#call(java.lang.Object)} by calling
     * {@code callback.call(m)}.
     * <p>
     * @param <B>      The type of broadcast message to subscribe to
     * @param type     The {@link Class} representing the type of broadcast
     *                 message to subscribe to.
     * @param callback The callback that should be called when messages of type
     *                 {@code type} are taken from this micro-service message
     *                 queue.
     */
    protected final <B extends Broadcast> void subscribeBroadcast(Class<B> type, Callback<B> callback) {
            // Register the callback for the given event type
        messageCallBack.put(type, callback);

        // Register this microservice's subscription with the singleton MessageBus
        msg_bus.subscribeBroadcast(type, this);
        }

    /**
     * Sends the event {@code e} using the message-bus and receive a {@link Future<T>}
     * object that may be resolved to hold a result. This method must be Non-Blocking since
     * there may be events which do not require any response and resolving.
     * <p>
     * @param <T>       The type of the expected result of the request
     *                  {@code e}
     * @param e         The event to send
     * @return  		{@link Future<T>} object that may be resolved later by a different
     *         			micro-service processing this event.
     * 	       			null in case no micro-service has subscribed to {@code e.getClass()}.
     */
    protected final <T> Future<T> sendEvent(Event<T> e) {
        return msg_bus.sendEvent(e);
    }

    /**
     * A Micro-Service calls this method in order to send the broadcast message {@code b} using the message-bus
     * to all the services subscribed to it.
     * <p>
     * @param b The broadcast message to send
     */
    protected final void sendBroadcast(Broadcast b) {
       this.msg_bus.sendBroadcast(b);
    }

    /**
     * Completes the received request {@code e} with the result {@code result}
     * using the message-bus.
     * <p>
     * @param <T>    The type of the expected result of the processed event
     *               {@code e}.
     * @param e      The event to complete.
     * @param result The result to resolve the relevant Future object.
     *               {@code e}.
     */
    protected final <T> void complete(Event<T> e, T result) {
        msg_bus.complete(e, result);
    }

    /**
     * this method is called once when the event loop starts.
     */
    protected abstract void initialize();

    /**
     * Signals the event loop that it must terminate after handling the current
     * message.
     */
    protected final void terminate() {
        this.terminated = true;
    }

    /**
     * @return the name of the service - the service name is given to it in the
     *         construction time and is used mainly for debugging purposes.
     */
    public final String getName() {
        return name;
    }


    @SuppressWarnings("unchecked")
    private <T> void handleMessage(Message message) {
        Callback<T> callback = (Callback<T>) messageCallBack.get(message.getClass());
        if (callback != null) {
            callback.call((T) message); // Execute the callback
            // if (message instanceof Event) {
            //     complete((Event<Boolean>) message, true); // Completing the event
            // }
        } else {
            throw new IllegalStateException("No callback found for message type: " + message.getClass());
        }

    }
    
    @Override
    public final void run() {
        msg_bus.register(this);
        System.out.println("MicroService " + getName() + " started");

        initialize();
        System.out.println("MicroService " + getName() + " Initialized");

        latch.countDown();
        try {
            // Wait until the latch count reaches zero
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Main thread interrupted!");
        }
        ServiceCounter.getInstance().incrementThreads();
        while (!terminated) {
            try {
                System.out.println("MicroService " + getName() + " isRunning");

                Message msg = msg_bus.awaitMessage(this);
                if (msg != null) {
                    System.out.println(getName() + " processing message: " + msg.getClass().getSimpleName());
                    this.handleMessage(msg);     
             }
        
    }
         catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            // terminate();
        }
    }
    System.out.println(getName() + " terminated: " );

    msg_bus.unregister(this);
    System.out.println("msg_bus unregistered: " + getName());


}
}
