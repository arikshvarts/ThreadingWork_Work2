package bgu.spl.mics.application.services;

import java.util.ArrayList;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.ErrorInfo;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.PoseEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.GPSIMU;
import bgu.spl.mics.application.objects.Pose;

/**
 * PoseService is responsible for maintaining the robot's current pose (position and orientation)
 * and broadcasting PoseEvents at every tick.
 */
public class PoseService extends MicroService {
    GPSIMU gpsimu=null;
    ArrayList<Pose> kol_haposot; //the Array of all the poses for use in the ErrorInfo
    /**
     * Constructor for PoseService.
     *
     * @param gpsimu The GPSIMU object that provides the robot's pose data.
     */
    public PoseService(GPSIMU gpsimu) {
        super("PoseService");
        this.gpsimu=gpsimu;
        this.kol_haposot = new ArrayList<>();    
    }

    /**
     * Initializes the PoseService.
     * Subscribes to TickBroadcast and sends PoseEvents at every tick based on the current pose.
     */
    @Override
    protected void initialize() {
        subscribeBroadcast(TickBroadcast.class, (TickBroadcast) ->
        {
            Pose pose = gpsimu.getTickAtTime();//chack if this is implementation
            gpsimu.setCurrentTick(gpsimu.getCurrentTick()+1);
            if (pose != null) {
                sendEvent(new PoseEvent(pose));
                kol_haposot.add(pose);
            }
        });
        subscribeBroadcast(CrashedBroadcast.class, (CrashedBroadcast) ->
        {
            ErrorInfo.getInstance().setPoses(kol_haposot);
            terminate();
        });

        subscribeBroadcast(TerminatedBroadcast.class, (TerminatedBroadcast) ->
        {
            if (TerminatedBroadcast.getSender().equals("TimeService")) {
                terminate();}
        });
        }
}
