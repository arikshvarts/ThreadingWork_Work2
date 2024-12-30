package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.PoseEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;
import bgu.spl.mics.application.objects.FusionSlam;

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
    public FusionSlamService(FusionSlam fusionSlam) {
        super("fusionSlam");
        fusionSlam = FusionSlam.getInstance();

    }

    /**
     * Initializes the FusionSlamService.
     * Registers the service to handle TrackedObjectsEvents, PoseEvents, and TickBroadcasts,
     * and sets up callbacks for updating the global map.
     */
    @Override
    protected void initialize() {
        subscribeEvent(TrackedObjectsEvent.class,(TrackedObjectsEvent)  ->{
            fusionSlam.processObjectsAtTime(TrackedObjectsEvent.getTrackedObjects(),TrackedObjectsEvent.getTime());
        });

        subscribeEvent(PoseEvent.class,(PoseEvent) ->{
            fusionSlam.addPose(PoseEvent.getPose());
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
            terminate();
        });
        }

}
