package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;

/**
 * LiDarService is responsible for processing data from the LiDAR sensor and
 * sending TrackedObjectsEvents to the FusionSLAM service.
 * 
 * This service interacts with the LiDarTracker object to retrieve and process
 * cloud point data and updates the system's StatisticalFolder upon sending its
 * observations.
 */
public class LiDarService extends MicroService {

    
    /**
     * Constructor for LiDarService.
     *
     * @param liDarTracker The LiDAR tracker object that this service will use to process data.
     */
    public LiDarService(LiDarWorkerTracker liDarTracker) {
        super("Change_This_Name");
        // TODO Implement this
    }

    /**
     * Initializes the LiDarService.
     * Registers the service to handle DetectObjectsEvents and TickBroadcasts,
     * and sets up the necessary callbacks for processing data.
     */
    @Override
    protected void initialize() {
<<<<<<< HEAD
        //The LiDarWorker gets the X’s,Y’s coordinates from the DataBase of them and sends a new TrackedObjectsEvent to the Fusion (can be multiple events).
        // Subscribes to TickBroadcast, TerminatedBroadcast, CrashedBroadcast, DetectObjectsEvent.
        subscribeBroadcast(TickBroadcast.class, (TickBroadcast c) -> {
            //how to handle tick?

        });


        subscribeBroadcast(TerminatedBroadcast.class, (TerminatedBroadcast c) -> {
            terminate();
        });


        subscribeBroadcast(CrashedBroadcast.class, (CrashedBroadcast c) -> {
            terminate(); //when recieving a CrashedBroadcast from another objects, each service stops immediately
        });

        subscribeEvent(DetectObjectsEvent.class, (DetectObjectsEvent c) -> { 
            //The LiDarWorker gets the X’s,Y’s coordinates from the DataBase of them and sends a new TrackedObjectsEvent to the Fusion.
            // After the LiDar Worker completes the event, it saves the coordinates in the lastObjects variable in DataBase and sends True value to the Camera.
            TrackedObjectsEvent eve;
            liDarTracker.handleDetectedObjectsEvent(c);          
        });
=======
        
>>>>>>> parent of 015e846 (my work on cameras lidar and statistical from shabat)
    }
}
