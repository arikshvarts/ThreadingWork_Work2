package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.PoseEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;
import bgu.spl.mics.application.objects.FusionSlam;


import bgu.spl.mics.application.objects.ServiceCounter;
import bgu.spl.mics.application.objects.TrackedObject;



/**
 * FusionSlamService integrates data from multiple sensors to build and update
 * the robot's global map.
 * 
 * This service receives TrackedObjectsEvents from LiDAR workers and PoseEvents from the PoseService,
 * transforming and updating the map with new landmarks.
 */
public class FusionSlamService extends MicroService {
    private FusionSlam fusionSlam;

    /**
     * Constructor for FusionSlamService.
     *
     * @param fusionSlam The FusionSLAM object responsible for managing the global map.
     */
    public FusionSlamService(FusionSlam FusionSlam) {
        super("fusionSlam");
        this.fusionSlam = FusionSlam;

    }

    /**
     * Initializes the FusionSlamService.
     * Registers the service to handle TrackedObjectsEvents, PoseEvents, and TickBroadcasts,
     * and sets up callbacks for updating the global map.
     */
    @Override
    protected void initialize() {
        subscribeEvent(TrackedObjectsEvent.class,(TrackedObjectsEvent)  ->{
            for (TrackedObject trackedObject : TrackedObjectsEvent.getTrackedObjects()) {
                fusionSlam.addOrUpdateLandMark(trackedObject);
            }
            // complete(TrackedObjectsEvent, true);
        });

        subscribeEvent(PoseEvent.class,(PoseEvent) ->{
            fusionSlam.InsertPose(PoseEvent.getPose());
            fusionSlam.handleWaitObj(PoseEvent.getPose());
            // complete(PoseEvent, true);

        });

        subscribeBroadcast(CrashedBroadcast.class, (CrashedBroadcast c) ->
        {
            String eror_description = c.getErrorMessage();
            String faulty_sensor = c.getFaultyServiceName();
            //we need to use this to infos when closing the program and write them to the ERROR_Output json file
            terminate();
        });

        subscribeBroadcast(TerminatedBroadcast.class, (TerminatedBroadcast) ->
        {
            if (TerminatedBroadcast.getSender().equals("TimeService")) {
                terminate();
            }
            // if (TerminatedBroadcast.getSender().equals("LiDarService")) {
            //     terminate(); //we need to check if we need to terminate the service when the LiDarService is terminated
            // }


            if (ServiceCounter.getInstance().getNumThreads() ==  2){
            terminate();
            sendBroadcast(new TerminatedBroadcast("FusionSlam"));
            }
        });
        subscribeBroadcast(TickBroadcast.class, (TerminatedBroadcast) ->
        {
        });
    }
        

}
