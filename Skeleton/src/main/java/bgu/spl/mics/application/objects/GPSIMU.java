package bgu.spl.mics.application.objects;

import java.io.ObjectInputFilter.Status;
import java.util.ArrayList;

/**
 * Represents the robot's GPS and IMU system.
 * Provides information about the robot's position and movement.
 */
public class GPSIMU {
    public int currentTick;
    public ArrayList<Pose> PoseList;

    public GPSIMU() {
        this.currentTick = 0;
        this.PoseList = new ArrayList<Pose>();
    }

    public int getCurrentTick() {
        return currentTick;
    }

    public void setCurrentTick(int currentTick) {
        this.currentTick = currentTick;
    }


    public ArrayList<Pose> getPoseList() {
        return PoseList;
    }

    public void setPoseList(ArrayList<Pose> poseList) {
        this.PoseList = poseList;
    }

    public Pose getTickAtTime() {
        return this.PoseList.get(this.currentTick);
    }
}
