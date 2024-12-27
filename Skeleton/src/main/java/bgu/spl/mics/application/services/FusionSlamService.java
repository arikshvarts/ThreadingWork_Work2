package bgu.spl.mics.application.services;

import javax.sound.midi.Track;

import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.PoseEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
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
    FusionSlam fusionSlam;

    /**
     * Constructor for FusionSlamService.
     *
     * @param fusionSlam The FusionSLAM object responsible for managing the global map.
     */
    public FusionSlamService(FusionSlam fusionSlam) {
        super("Change_This_Name");
        FusionSlam fusionSlam = new FusionSlam();
    }

    /**
     * Initializes the FusionSlamService.
     * Registers the service to handle TrackedObjectsEvents, PoseEvents, and TickBroadcasts,
     * and sets up callbacks for updating the global map.
     */
    @Override
    protected void initialize() {


//         to TickBroadcast, TrackedObjectsEvent, PoseEvent, ,
// CrashedBroadcast.



        subscribeEvent(TrackedObjectsEvent.class,(TrackedObjectsEvent)->{

        });


        subscribeEvent(PoseEvent.class,(PoseEvent)->{
            PoseEvent.getPose
        });

        subscribeBroadcast(TickBroadcast.class,(TickBroadcast)->{

         });

       subscribeBroadcast(TerminatedBroadcast.class,(TerminatedBroadcast)->
       {
              terminate(); 
       });


       subscribeBroadcast(CrashedBroadcast.class,(subscribeBroadcast)->
       {
              terminate(); 
       });
        }
}
