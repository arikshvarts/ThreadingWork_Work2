package bgu.spl.mics.application.services;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.ErrorInfo;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;
import bgu.spl.mics.application.objects.LiDarDataBase;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import bgu.spl.mics.application.objects.STATUS;
import bgu.spl.mics.application.objects.ServiceCounter;
import bgu.spl.mics.application.objects.StatisticalFolder;
import bgu.spl.mics.application.objects.TrackedObject;


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
    TrackedObjectsEvent last_frame; //in the ERROR output file we will use it for extracting the time and the objects

    
    public LiDarService(LiDarWorkerTracker liDarTracker,CountDownLatch latch) {
        super("Lidar",latch);
        this.liDarTracker = liDarTracker;
        this.stat = StatisticalFolder.getInstance();
        this.last_frame = new TrackedObjectsEvent(new ArrayList<TrackedObject>(), 0);


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
            ServiceCounter.getInstance().decrementThreads();

            terminate();
            //if when we get the service its STATUS is ERROR, send crashedBroadcast and terminate
        }


        subscribeBroadcast(TickBroadcast.class, (TickBroadcast c) -> {
            System.out.println("lidar service"+liDarTracker.getId()+ "got a tick ");

            if(c.getCurrentTick() > last_detected_time + liDarTracker.getFrequency() && events_to_send.isEmpty()==true)   {
            //stop after last time you detected something + the frequency and haa nothing more to send (that may be after last T+F because of camera F)
            liDarTracker.setStatus(STATUS.DOWN);
            ServiceCounter.getInstance().decrementThreads();
                terminate();
            }
            else{
                if(liDarTracker.getStatus() == STATUS.UP){
                    System.out.println("before synch");

                    synchronized (events_to_send) {
                        System.out.println("after synch");

                        Iterator<TrackedObjectsEvent> iterator = events_to_send.iterator();
                        while (iterator.hasNext()) {
                            TrackedObjectsEvent eve = iterator.next();
                            System.out.println("iteratorLidar Moving");
                            if (c.getCurrentTick() >= eve.getTime() + liDarTracker.getFrequency()) {
                                System.out.println("LiDarService: sending TrackedObjectsEvent");
                                stat.incrementTrackedObjects(eve.getTrackedObjects().size());
                                last_frame = eve;
                                ErrorInfo.getInstance().UpdateLidarsLastFrames(last_frame, liDarTracker.getId());
                                sendEvent(eve);
                                iterator.remove();  // Safe removal using iterator
                            }
                        }
                    }
                }
            }
        });


        subscribeBroadcast(TerminatedBroadcast.class, (TerminatedBroadcast c) -> {
            if(c.getSender() == "TimeService"){
                ServiceCounter.getInstance().decrementThreads();

                terminate(); //if the sender of the TerminatedBroadcast is TimeService, the duration is over
                liDarTracker.setStatus(STATUS.DOWN);
            }
        });


        subscribeBroadcast(CrashedBroadcast.class, (CrashedBroadcast c) -> {
            ServiceCounter.getInstance().decrementThreads();


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
                ServiceCounter.getInstance().decrementThreads();

                terminate();

            }
            else{
            events_to_send.add(new_eve); 
            } 
        });
    }
}
