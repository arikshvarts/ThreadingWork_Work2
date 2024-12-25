package bgu.spl.mics.application.services;

import java.util.List;

import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.objects.*;

/**
 * CameraService is responsible for processing data from the camera and
 * sending DetectObjectsEvents to LiDAR workers.
 * 
 * This service interacts with the Camera object to detect objects and updates
 * the system's StatisticalFolder upon sending its observations.
 */
public class CameraService extends MicroService {
    private final Camera camera;

    /**
     * Constructor for CameraService.
     *
     * @param camera The Camera object that this service will use to detect objects.
     */
    public CameraService(Camera camera) {
        super("Camera");
        this.camera = camera;
        msgBus =MessageBusImpl.getInstance()
    }

    /**
     * Initializes the CameraService.
     * Registers the service to handle TickBroadcasts and sets up callbacks for sending
     * DetectObjectsEvents.
     */
    @Override
    protected void initialize() {
        (TickBroadcast.class,(TickBroadcast c)->{
            detectedObjectsEvent eve = camera.handleTick(c.getCurrentTime());
            if (eve!=null){
                Future<boolean> fut= MessageBusImpl.getInstance().sendEvent(eve);
                if (fut.get()==false){
                    //crash
                }
            }
        });
        subscribeBroadcast(TerminateBroadcast.class, (TerminateBroadcast terminateBroadcast) -> {
            terminate();
        }); 
        CrashedBroadcast(CrashedBroadcast.class, (CrashedBroadcast terminateBroadcast) -> {
            terminate();
        }); 
    }
}
