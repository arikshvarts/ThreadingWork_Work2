package bgu.spl.mics.application.services;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.StatisticalFolder;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
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
    public TimeService(int TickTime, int Duration) {
        super("TimeService");
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
            terminate();
        });
    
        while (currentTick < totalTicks) {
            // Broadcast the current tick to all subscribed microservices
            sendBroadcast(new TickBroadcast(currentTick));
            currentTick++;
            StatisticalFolder.getInstance().updateRuntime(StatisticalFolder.getInstance().getSystemRuntime()+1);
            try {
                // Simulate the passage of time for this tick
                Thread.sleep(tickDuration);
            } catch (InterruptedException e) {
                // Handle interruption and terminate the service
                Thread.currentThread().interrupt();
                break;
            }
        }
        // Signal termination after all ticks are completed
        sendBroadcast(new TerminatedBroadcast("TimeService"));
        terminate();

    }
}
