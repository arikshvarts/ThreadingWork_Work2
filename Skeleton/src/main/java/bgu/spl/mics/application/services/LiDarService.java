package bgu.spl.mics.application.services;

import java.util.ArrayList;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;
import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import bgu.spl.mics.application.objects.StatisticalFolder;

/**
 * LiDarService is responsible for processing data from the LiDAR sensor and
 * sending TrackedObjectsEvents to the FusionSLAM service.
 * 
 * This service interacts with the LiDarTracker object to retrieve and process
 * cloud point data and updates the system's StatisticalFolder upon sending its
 * observations.
 */
public class LiDarService extends MicroService {

    LiDarWorkerTracker liDarTracker;    
    //the events that are ready to send and wait for the frequency suspend to be sent
    ArrayList<TrackedObjectsEvent> events_to_send = new ArrayList<>();
    /**
     * Constructor for LiDarService.
     *
     * @param liDarTracker The LiDAR tracker object that this service will use to process data.
     */
    public LiDarService(LiDarWorkerTracker liDarTracker) {
        super("Lidar");
        this.liDarTracker = liDarTracker;
    }

    /**
     * Initializes the LiDarService.
     * Registers the service to handle DetectObjectsEvents and TickBroadcasts,
     * and sets up the necessary callbacks for processing data.
     */
    @Override
    protected void initialize() {
        //The LiDarWorker gets the X’s,Y’s coordinates from the DataBase of them and sends a new TrackedObjectsEvent to the Fusion (can be multiple events).
        // Subscribes to TickBroadcast, TerminatedBroadcast, CrashedBroadcast, DetectObjectsEvent.
        subscribeBroadcast(TickBroadcast.class, (TickBroadcast c) -> {
            if(events_to_send != null){
                for(TrackedObjectsEvent eve : events_to_send) { //looping all proccessed events and check if ready to send
                    if(c.getCurrentTick() >= eve.getTime() + liDarTracker.getFrequency()){
                        //currTick > eve.time + freq  if the camera frequency greater than lidar frequency
                        sendEvent(eve);
                        events_to_send.remove(eve);
                    }
                } 
            }

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
            events_to_send.add(liDarTracker.handleDetectedObjectsEvent(c)); 
            
        });
    }
}
