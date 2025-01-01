package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Manages the fusion of sensor data for simultaneous localization and mapping (SLAM).
 * Combines data from multiple sensors (e.g., LiDAR, camera) to build and update a global map.
 * Implements the Singleton pattern to ensure a single instance of FusionSlam exists.
 */





public class FusionSlam {
    // private final ConcurrentHashMap<String, LandMark>
    private ArrayList<LandMark> landmarks; // Global map of landmarks
    private ArrayList<Pose> poses; // list of Poses of the robot
    private StatisticalFolder stats;
    private ArrayList<TrackedObject> waitingObjs;
    private boolean flag=false;



    // Singleton instance holder
    private static class FusionSlamHolder {
        private static final FusionSlam instance = new FusionSlam();

    }
    public FusionSlam() {
        this.landmarks=new ArrayList<LandMark>();
        this.poses=new ArrayList<Pose>();
        this.stats = StatisticalFolder.getInstance();
        this.waitingObjs=new ArrayList<TrackedObject>();
    }
    public static FusionSlam getInstance() {
        return FusionSlamHolder.instance;
    }
  
       public void addOrUpdateLandMark(TrackedObject trackedObject) {
        LandMark existingLandMark = landMarkExist(trackedObject);
        if (existingLandMark == null) { //in case the LandMark doesn't exist.
            ArrayList<CloudPoint> coor = translateCoordinateSys(trackedObject);
            if (coor != null) {
                LandMark landMarkToAdd = new LandMark(trackedObject.getId(), trackedObject.getDescription(), coor);
                landmarks.add(landMarkToAdd);
                stats.incrementLandmarks(1);
            }
            else
            waitingObjs.add(trackedObject);
        }
        else {
            ArrayList<CloudPoint> updatedCoordinates = avgCalc(existingLandMark, trackedObject);
            if (updatedCoordinates != null) {
                existingLandMark.setCoordinates(updatedCoordinates);
            }
            else {
                waitingObjs.add(trackedObject);
            }
        }
    }


    public void InsertPose(Pose pose) {
        poses.add(pose);
    }
    //return the corresponding LandMark if exists, null otherwise:
    public LandMark landMarkExist(TrackedObject trackedObject) {
        String objectID = trackedObject.getId();
        for (LandMark landMark : landmarks) {
            if (landMark.getId().equals(objectID)) {
                return landMark;
            }
        }
        return null;
    }

    public ArrayList<CloudPoint> translateCoordinateSys(TrackedObject trackedObject) {

        int trackedTime = trackedObject.getTime();
        Pose relevantPose = null;
        //finding the corresponding pose:
        for (Pose pose : this.poses) {
            if (pose.getTime() == trackedTime) {
                relevantPose = pose;
                break;
            }
        }

        if (relevantPose == null) {
            return null;
        }

        ArrayList<CloudPoint> globalCoordinates = new ArrayList<>();
        double yaw_rad=relevantPose.getYaw()*Math.PI/180;
        for (CloudPoint localPoint : trackedObject.getCoordinates()) {
            double x_global=localPoint.getX()*Math.cos(yaw_rad)-localPoint.getY()*Math.sin(yaw_rad)+relevantPose.getX();
            double y_global=localPoint.getX()*Math.sin(yaw_rad)+localPoint.getY()*Math.cos(yaw_rad)+relevantPose.getY();
            globalCoordinates.add(new CloudPoint(x_global, y_global));
        }
        return globalCoordinates;
    }

    public ArrayList<CloudPoint> avgCalc(LandMark landMark, TrackedObject trackedObject) {
        ArrayList<CloudPoint> toReturn = new ArrayList<>();
        ArrayList<CloudPoint> coor = translateCoordinateSys(trackedObject);
        if (coor == null) {
            return null;
        }
        ArrayList<CloudPoint> globalCoordinates = coor;
        ArrayList<CloudPoint> existingCoordinates = landMark.getCoordinates();
        Iterator<CloudPoint> iterator1 = globalCoordinates.iterator();
        Iterator<CloudPoint> iterator2 = existingCoordinates.iterator();
        while (iterator1.hasNext() && iterator2.hasNext()) {
            CloudPoint curr1 = iterator1.next();
            CloudPoint curr2 = iterator2.next();
            CloudPoint c1 = new CloudPoint((curr1.getX() + curr2.getX())/2.0, (curr1.getY() + curr2.getY())/2.0);
            toReturn.add(new CloudPoint(c1.getX(), c1.getY()));
        }
        while (iterator1.hasNext()) {
            toReturn.add(iterator1.next());
        }
        while (iterator2.hasNext()) {
            toReturn.add(iterator2.next());
        }
        return toReturn;
    }

        public void handleWaitObj(Pose pose) {
            Iterator<TrackedObject> iter = waitingObjs.iterator();
            while (iter.hasNext()) {
                TrackedObject trackedObject = iter.next();
                if (trackedObject.getTime() == pose.getTime()) {
                    iter.remove();
                    addOrUpdateLandMark(trackedObject);
            }

        }
    }

}
    
