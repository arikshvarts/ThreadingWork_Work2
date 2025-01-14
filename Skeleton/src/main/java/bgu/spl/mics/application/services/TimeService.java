package bgu.spl.mics.application.services;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.StatisticalFolder;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.TerminatedBroadcast;

import java.util.concurrent.CountDownLatch;

import bgu.spl.mics.Callback;
import bgu.spl.mics.MicroService;

/**
 * TimeService acts as the global timer for the system, broadcasting TickBroadcast messages
 * at regular intervals and controlling the simulation's duration.
 */
public class TimeService extends MicroService {
    private final int tickDuration;  // Duration of each tick in milliseconds
    private final int totalTicks;   // Total number of ticks before termination
    private int currentTick;        // Tracks the current tick number

    /**
     * Constructor for TimeService.
     *
     * @param TickTime  The duration of each tick in milliseconds.
     * @param Duration  The total number of ticks before the service terminates.
     */
    public TimeService(int TickTime, int Duration, CountDownLatch latch) {
        super("TimeService",latch);
        this.tickDuration = TickTime;
        this.totalTicks = Duration;
        this.currentTick = 0;

    }

    /**
     * Initializes the TimeService.
     * Starts broadcasting TickBroadcast messages and terminates after the specified duration.
     */
    @Override
    protected void initialize() {
        subscribeBroadcast(CrashedBroadcast.class, (CrashedBroadcast) ->
        {
            terminate();
        });

        subscribeBroadcast(TerminatedBroadcast.class, (TerminatedBroadcast) ->
        {
            if (TerminatedBroadcast.getSender().equals("FusionSlam")) {
                terminate();}
        });        

        subscribeBroadcast(TickBroadcast.class, (TickBroadcast) ->
        {
            sendBroadcast(new TickBroadcast(currentTick));
            currentTick++;
            System.out.println("TimeService: Tick " + currentTick);
            StatisticalFolder.getInstance().updateRuntime(1);
            try {
                // Simulate the passage of time for this tick
                Thread.sleep(tickDuration*1000);
            } catch (InterruptedException e) {
                // Handle interruption and terminate the service
                Thread.currentThread().interrupt();
            }  
            if (currentTick == totalTicks) {
                // Signal termination after all ticks are completed
                sendBroadcast(new TerminatedBroadcast("TimeService"));
                terminate();
            }
});
sendBroadcast(new TickBroadcast(currentTick));
        }
    }
        

    
