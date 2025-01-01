package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.ErrorInfo;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.PoseEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;
import bgu.spl.mics.application.objects.CloudPoint;
import bgu.spl.mics.application.objects.FusionSlam;
import bgu.spl.mics.application.objects.LandMark;
import bgu.spl.mics.application.objects.ServiceCounter;
import bgu.spl.mics.application.objects.StatisticalFolder;
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
            complete(TrackedObjectsEvent, true);
        });

        subscribeEvent(PoseEvent.class,(PoseEvent) ->{
            fusionSlam.InsertPose(PoseEvent.getPose());
            fusionSlam.handleWaitObj(PoseEvent.getPose()); //checks if the current pose corresponds to some of the waiting objects.
            // complete(PoseEvent, true);

        });

        subscribeBroadcast(CrashedBroadcast.class, (CrashedBroadcast c) ->
        {
            //we need to use this to infos when closing the program and write them to the ERROR_Output json file
            ErrorInfo.getInstance().set_crashed_brod(c);
            ErrorInfo.getInstance().createOutput(); //is this the right place to call createOutput()?
            terminate();
        });

        subscribeBroadcast(TerminatedBroadcast.class, (TerminatedBroadcast) ->
        {
            if (TerminatedBroadcast.getSender().equals("TimeService")) {
                terminate();
                fusionSlam.createOutput();
            }
            else if (ServiceCounter.getInstance().getNumThreads() ==  2){
                //it means all the microservices are terminated except Fusion and Time
            fusionSlam.createOutput();
            terminate();
            sendBroadcast(new TerminatedBroadcast("FusionSlam"));
            }
        });
        subscribeBroadcast(TickBroadcast.class, (TerminatedBroadcast) ->
        {
        });
    }
        


}
