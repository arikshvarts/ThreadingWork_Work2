package bgu.spl.mics;

import java.util.concurrent.TimeUnit;

/**
 * A Future object represents a promised result - an object that will
 * eventually be resolved to hold a result of some operation. The class allows
 * Retrieving the result once it is available.
 * 
 * Only private methods may be added to this class.
 * No public constructor is allowed except for the empty constructor.
 */
public class Future<T> {
    private T result;
    private volatile boolean isDone;
    private Object lock;
    
    public Future() {
        this.result = null;
        this.isDone = false;
        this.lock = new Object();
    }
    
    /**
     * retrieves the result the Future object holds if it has been resolved.
     * This is a blocking method! It waits for the computation in case it has
     * not been completed.
     * <p>
     * @return return the result of type T if it is available, if not wait until it is available.
     *         
     */
    public T get() {
        while(!isDone) {
            synchronized (lock) {
            try {
                lock.wait();
            } catch (InterruptedException e) {                
                Thread.currentThread().interrupt();
            }
        }
    }
        return result;
    }
    
    /**
     * Resolves the result of this Future object.
     */
    public void resolve (T result) {
        if(!isDone) {
        this.result = result;
        this.isDone=true;
        synchronized(lock) {
        notifyAll();
        }
    }
}
    
    /**
     * @return true if this object has been resolved, false otherwise
     */
    public boolean isDone() {//no need for synchronization because of vollatile
        return isDone;  
    }
    
    /**
     * retrieves the result the Future object holds if it has been resolved,
     * This method is non-blocking, it has a limited amount of time determined
     * by {@code timeout}
     * <p>
     * @param timout    the maximal amount of time units to wait for the result.
     * @param unit      the {@link TimeUnit} time units to wait.
     * @return return the result of type T if it is available, if not, 
     *         wait for {@code timeout} TimeUnits {@code unit}. If time has
     *         elapsed, return null.
     */
    public T get(long timeout, TimeUnit unit) {
        long millisTimeout = unit.toMillis(timeout); // Convert timeout to milliseconds
        long endTime = System.currentTimeMillis() + millisTimeout; // Calculate the absolute end time
    
        synchronized (lock) {
            while (!isDone) {
                long remainingTime = endTime - System.currentTimeMillis();
                if (remainingTime <= 0) {
                    return null; // Timeout has elapsed
                }
                try {
                    lock.wait(remainingTime); // Wait for the remaining time or until notified
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Restore the interrupt status
                    return null; // Return null on interruption
                }
            }
            return result; // Return the resolved result
        }
    }
        

}




