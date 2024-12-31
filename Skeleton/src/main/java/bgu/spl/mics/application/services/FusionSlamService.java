package bgu.spl.mics.application.services;

import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.Track;

import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.PoseEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;
import bgu.spl.mics.application.objects.FusionSlam;
import bgu.spl.mics.application.objects.LandMark;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.objects.ServiceCounter;
import bgu.spl.mics.application.objects.TrackedObject;
import bgu.spl.mics.application.objects.StatisticalFolder;

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

        subscribeBroadcast(CrashedBroadcast.class, (CrashedBroadcast) ->
        {
            terminate();
        });

        subscribeBroadcast(TerminatedBroadcast.class, (TerminatedBroadcast) ->
        {
            if (ServiceCounter.getInstance().getNumThreads() == 3){
            terminate();
            sendBroadcast(new TerminatedBroadcast("FusionSlam"));
            }
        });
        subscribeBroadcast(TickBroadcast.class, (TerminatedBroadcast) ->
        {
            if (ServiceCounter.getInstance().getNumThreads() == 3){
            terminate();
            sendBroadcast(new TerminatedBroadcast("FusionSlam"));
            }
        });
    }
        

}
