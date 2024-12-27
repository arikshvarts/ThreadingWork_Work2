package bgu.spl.mics.application.services;

import bgu.spl.mics.Callback;
import bgu.spl.mics.Future;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.Camera;

/**
 * CameraService is responsible for processing data from the camera and sending
 * DetectObjectsEvents to LiDAR workers.
 *
 * This service interacts with the Camera object to detect objects and updates
 * the system's StatisticalFolder upon sending its observations.
 */
public class CameraService extends MicroService {

    private final Camera camera;
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

    }

    /**
     * Initializes the CameraService. Registers the service to handle
     * TickBroadcasts and sets up callbacks for sending DetectObjectsEvents.
     */
    @Override
    protected void initialize() {
        //update relevant callbacks into the messageCallBack hashmap
        messageCallBack.putIfAbsent(TickBroadcast.class, (TickBroadcast c) -> {
            DetectObjectsEvent eve = camera.handleTick(c.getCurrentTick());
            if (eve != null) {
                //if() send only if frequency delay passed
                Future<Boolean> fut = MessageBusImpl.getInstance().sendEvent(eve);
                if (fut.get() == false) {
                    sendBroadcast(new CrashedBroadcast(getName(), "Failure occurred while processing DetectObjectsEvent."));
                }
                if (fut == null) { //msd_nus.sendEvent can also return null
                    System.err.println("No service is available to handle the DetectObjectsEvent.");
                    return;
                }
            }
        });
        subscribeBroadcast(TickBroadcast.class, (Callback<TickBroadcast>) messageCallBack.get(TickBroadcast.class));

        messageCallBack.putIfAbsent(TerminatedBroadcast.class, (TerminatedBroadcast c) -> {
            terminate();
        });

        subscribeBroadcast(TerminatedBroadcast.class, (Callback<TerminatedBroadcast>) messageCallBack.get(TerminatedBroadcast.class));

        messageCallBack.putIfAbsent(CrashedBroadcast.class, (CrashedBroadcast c) -> {
            terminate(); //both of TerminatedBroadcast and CrashedBroadcast are leading to termination?
        });

        subscribeBroadcast(CrashedBroadcast.class, (Callback<CrashedBroadcast>) messageCallBack.get(CrashedBroadcast.class));

    }
}
