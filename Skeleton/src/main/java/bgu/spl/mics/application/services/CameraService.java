package bgu.spl.mics.application.services;

import bgu.spl.mics.Future;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.STATUS;
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
    // private int last_tick_detected;

    /**
     * Constructor for CameraService.
     *
     * @param camera The Camera object that this service will use to detect
     * objects.
     */
    public CameraService(Camera camera) {
        super("Camera");
        this.camera = camera;
        this.last_detected_time = camera.getCameraData().get(camera.getCameraData().size() - 1).getTime();
        StatisticalFolder stat = StatisticalFolder.getInstance();
    }

    /**
     * Initializes the CameraService. Registers the service to handle
     * TickBroadcasts and sets up callbacks for sending DetectObjectsEvents.
     */
    @Override
    protected void initialize() {
        if(camera.getStatus() == STATUS.ERROR){
            sendBroadcast(new CrashedBroadcast(getName(), "Error in the sensor"));
        }
        
        subscribeBroadcast(TickBroadcast.class, (TickBroadcast c) -> {
        if(c.getCurrentTick() < camera.get_last_detected_time())   {
            //
            camera.setStatus(STATUS.DOWN);
                terminate();
        }
        else{
            if(camera.getStatus()== STATUS.UP){
                //if the DetectedObjectsList is empty
                if(camera.getDetectedObjectsList().isEmpty()==true){ //efshar lehorid im nagdir bapirsor im empty = -1
                    camera.setStatus(STATUS.DOWN);
                    terminate();
                    sendBroadcast(new TerminatedBroadcast(getName()));
                }
                DetectObjectsEvent eve = camera.handleTick(c.getCurrentTick());
                
                if (eve != null) {
                    stat.incrementDetectedObjects(eve.getObjects().size());
                    //if() send only if frequency delay passed
                    for(DetectedObject det : eve.getObjects()){
                        if (det.getId() == "ERROR"){
                            camera.setStatus(STATUS.ERROR);
                            sendBroadcast(new CrashedBroadcast(det.getId(), "this item is error"));
                            break;
                        }
                    }
                    //there is not object ERROR
                    statsManager.incrementDetectedObjects(eve.getObjects().size()); //according to the assignment forum, numDetectedObjects the total detecting and not unique objects
                    Future<Boolean> fut = MessageBusImpl.getInstance().sendEvent(eve);
                    if (fut.get() == false) {
                        sendBroadcast(new CrashedBroadcast(getName(), "Failure occurred while processing DetectObjectsEvent."));
                        //lo BATUAH im nachon hasend Crashrd
                    }
                    
                }
            }
        }
    });

        subscribeBroadcast(TerminatedBroadcast.class, (TerminatedBroadcast c) -> {
            if(c.getSender() == "TimeService"){
                terminate(); //if the sender of the TerminatedBroadcast is TimeService, the duration is over
                camera.setStatus(STATUS.DOWN);
            }
        });


        subscribeBroadcast(CrashedBroadcast.class, (CrashedBroadcast c) -> {
            camera.setStatus(STATUS.DOWN);
            terminate(); //when recieving a CrashedBroadcast from another objects, each service stops immediately
        });

        }
}
