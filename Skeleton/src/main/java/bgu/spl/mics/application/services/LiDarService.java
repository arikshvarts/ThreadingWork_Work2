package bgu.spl.mics.application.services;

import java.util.ArrayList;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;
import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.LiDarDataBase;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import bgu.spl.mics.application.objects.STATUS;
import bgu.spl.mics.application.objects.StatisticalFolder;
import bgu.spl.mics.application.objects.LiDarDataBase;


/**
 * LiDarService is responsible for processing data from the LiDAR sensor and
 * sending TrackedObjectsEvents to the FusionSLAM service.
 * 
 * This service interacts with the LiDarTracker object to retrieve and process
 * cloud point data and updates the system's StatisticalFolder upon sending its
 * observations.
 */
public class LiDarService extends MicroService {

    private LiDarWorkerTracker liDarTracker;    
    //the events that are ready to send and wait for the frequency suspend to be sent
      ArrayList<TrackedObjectsEvent> events_to_send = new ArrayList<>();
    //leshanoy im meshanin Pirsor MehaLidarDataBase
    int last_detected_time = LiDarDataBase.getInstance().getCloudPoints().get(LiDarDataBase.getInstance().getCloudPoints().size() - 1).getTime();
    private StatisticalFolder stat;

    
    public LiDarService(LiDarWorkerTracker liDarTracker) {
        super("Lidar");
        this.liDarTracker = liDarTracker;
        this.stat = StatisticalFolder.getInstance();

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
        if(liDarTracker.getStatus() == STATUS.ERROR){
            sendBroadcast(new CrashedBroadcast(getName(), "Error in the sensor"));
            terminate();
            //if when we get the service its STATUS is ERROR, send crashedBroadcast and terminate
        }


        subscribeBroadcast(TickBroadcast.class, (TickBroadcast c) -> {
                      if(c.getCurrentTick() < last_detected_time + liDarTracker.getFrequency())   {
            //stop after last time you detected something + the frequency
            liDarTracker.setStatus(STATUS.DOWN);
                terminate();
            }
            else{
                if(liDarTracker.getStatus() == STATUS.UP){
                    if(events_to_send != null){
                        for(TrackedObjectsEvent eve : events_to_send) { //looping all proccessed events and check if ready to send
                            if(c.getCurrentTick() >= eve.getTime() + liDarTracker.getFrequency()){
                                //currTick > eve.time + freq  if the camera frequency greater than lidar frequency
                                stat.incrementTrackedObjects(eve.getTrackedObjects().size());
                                sendEvent(eve);
                                events_to_send.remove(eve);
                                
                            }
                        } 
                    }
                }
            }
        });


        subscribeBroadcast(TerminatedBroadcast.class, (TerminatedBroadcast c) -> {
            if(c.getSender() == "TimeService"){
                terminate(); //if the sender of the TerminatedBroadcast is TimeService, the duration is over
                liDarTracker.setStatus(STATUS.DOWN);
            }
        });


        subscribeBroadcast(CrashedBroadcast.class, (CrashedBroadcast c) -> {
            terminate(); //when recieving a CrashedBroadcast from another objects, each service stops immediately
        });

        subscribeEvent(DetectObjectsEvent.class, (DetectObjectsEvent c) -> { 
            //The LiDarWorker gets the X’s,Y’s coordinates from the DataBase of them and sends a new TrackedObjectsEvent to the Fusion.
            // After the LiDar Worker completes the event, it saves the coordinates in the lastObjects variable in DataBase and sends True value to the Camera.
            TrackedObjectsEvent new_eve = liDarTracker.handleDetectedObjectsEvent(c);
            if(liDarTracker.getStatus() == STATUS.ERROR){
                //if in proccessing of the detected objects asked we face an obect with id=ERROR     
                sendBroadcast(new CrashedBroadcast(getName(), "the Lidar detected obj with id=error"));
                //CHANGE AFTER THE PIRSOR (bring logic from handle DetectedEvent to here)
                terminate();
            }
            else{
            events_to_send.add(new_eve); 
            } 
        });
    }
}
