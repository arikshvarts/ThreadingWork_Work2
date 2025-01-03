package bgu.spl.mics.application.services;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import bgu.spl.mics.Future;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.ErrorInfo;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.STATUS;
import bgu.spl.mics.application.objects.ServiceCounter;
import bgu.spl.mics.application.objects.StatisticalFolder;

/**
 * CameraService is responsible for processing data from the camera and sending
 * DetectObjectsEvents to LiDAR workers.
 *
 * This service interacts with the Camera object to detect objects and updates
 * the system's StatisticalFolder upon sending its observations.
 */
public class CameraService extends MicroService {

    private final Camera camera;
    private final int last_detected_time;
    private StatisticalFolder stat;
    private DetectObjectsEvent last_frame; //in the ERROR output file we will use it for extracting the time and the objects
    // private int last_tick_detected;

    /**
     * Constructor for CameraService.
     *
     * @param camera The Camera object that this service will use to detect
     * objects.
     */
    public CameraService(Camera camera,CountDownLatch latch){
        super("Camera",latch);
        this.camera = camera;
        this.last_detected_time = camera.get_last_detected_time();
        this.stat = StatisticalFolder.getInstance();
        this.last_frame = new DetectObjectsEvent(0, new ArrayList<DetectedObject>()); //NEED to check if initialize to null can cause an error in case of ErrorInfo trying to tostring this
        //if there is a crashedBroadcast from another sensor before the first "frame" the cmaera detect, its last_frame in the error output will be time:0 with an empty list
        ErrorInfo.getInstance().UpdateCamerasLastFrames(last_frame, camera.getKey()); 
    }

    /**
     * Initializes the CameraService. Registers the service to handle
     * TickBroadcasts and sets up callbacks for sending DetectObjectsEvents.
     */
    @Override
    protected void initialize() {
        if(camera.getStatus() == STATUS.ERROR){
            sendBroadcast(new CrashedBroadcast(getName(), "Error in the sensor"));
            ServiceCounter.getInstance().decrementThreads();
            terminate();
            //if when we get the service its STATUS is ERROR, send crashedBroadcast and terminate
        }
        
        subscribeBroadcast(TickBroadcast.class, (TickBroadcast c) -> {
        if(c.getCurrentTick() > last_detected_time + camera.getFrequency())   {
            //stop after last time you detected something + the frequency
            camera.setStatus(STATUS.DOWN);
            ServiceCounter.getInstance().decrementThreads();

                terminate();
        }
        else{
            if(camera.getStatus()== STATUS.UP){
                //if the DetectedObjectsList is empty
                if(camera.getDetectedObjectsList().isEmpty()==true){ //efshar lehorid im nagdir bapirsor im empty = -1
                    camera.setStatus(STATUS.DOWN);
                    ServiceCounter.getInstance().decrementThreads();

                    terminate();
                    sendBroadcast(new TerminatedBroadcast(getName()));
                }
                DetectObjectsEvent eve = camera.handleTick(c.getCurrentTick());
                if(eve.getObjects().isEmpty() == false ) {
                    //update last_frame and send DetectObjectsEvent only if the camera saw something
                    last_frame = eve; // the last time the camera processed the envirmoent, it means, what the camera saw at currTime - frequency 
                    ErrorInfo.getInstance().UpdateCamerasLastFrames(last_frame, camera.getKey());
                    //send only if frequency delay passed (handeled by handle tick)
                    for(DetectedObject det : eve.getObjects()){
                        if (det.getId().equals("ERROR")){
                            camera.setStatus(STATUS.ERROR);
                            sendBroadcast(new CrashedBroadcast(det.getId(), "this item is error"));
                            ServiceCounter.getInstance().decrementThreads();

                            terminate();
                            break;
                        }
                    }
                    stat.incrementDetectedObjects(eve.getObjects().size());
                    System.out.println("Camera sent a DetectObjectsEvent");
                    Future<Boolean> fut = MessageBusImpl.getInstance().sendEvent(eve);
                    // if (fut.get() == false) {
                    //     sendBroadcast(new CrashedBroadcast(getName(), "Failure occurred while processing DetectObjectsEvent."));
                    //     //dont sure if its correct to semd crashed here
                    // }
                }
                }
            }
        }
    );

        subscribeBroadcast(TerminatedBroadcast.class, (TerminatedBroadcast c) -> {
            if(c.getSender() == "TimeService"){
                ServiceCounter.getInstance().decrementThreads();
                terminate(); //if the sender of the TerminatedBroadcast is TimeService, the duration is over
                camera.setStatus(STATUS.DOWN);
            }
        });


        subscribeBroadcast(CrashedBroadcast.class, (CrashedBroadcast c) -> {
            // ErrorInfo.getInstance().AddCamerasLastFrames(last_frame); //updating the last frame to the ErrorInfo
            camera.setStatus(STATUS.DOWN);
            ServiceCounter.getInstance().decrementThreads();

            terminate(); //when recieving a CrashedBroadcast from another objects, each service stops immediately
        });

        }
}
