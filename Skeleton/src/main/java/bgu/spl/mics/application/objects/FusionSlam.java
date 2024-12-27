package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
/**
 * Manages the fusion of sensor data for simultaneous localization and mapping (SLAM).
 * Combines data from multiple sensors (e.g., LiDAR, camera) to build and update a global map.
 * Implements the Singleton pattern to ensure a single instance of FusionSlam exists.
 */
public class FusionSlam {
    private final ConcurrentHashMap<String, LandMark> landmarks; // Global map of landmarks
    private  ArrayList<Pose> poses; // list of Poses of the robot


    // Singleton instance holder
    private static class FusionSlamHolder {
        private static final FusionSlam instance = new FusionSlam();
    }
    private FusionSlam() {
        this.landmarks=new ConcurrentHashMap<>();
        this.poses=new ArrayList<>();
    }
    public static FusionSlam getInstance() {
        return FusionSlamHolder.instance;
    }

    public void processObjects(ArrayList<TrackedObject> trackedObjects) {

        for (TrackedObject trackedObject : trackedObjects) {
            LandMark lndMark = translateCoordinateSys(trackedObject);
            updateLandmarks(lndMark);
        }
    }   


    public synchronized void updatePose(ArrayList<Pose> recPoses) {
        this.poses=recPoses;

    }



    public LandMark translateCoordinateSys(TrackedObject trackedObject) {
        ArrayList<CloudPoint> globalCoordinates = new ArrayList<>();
        double yaw_rad=poses.getLast().getYaw()*Math.PI/180;
        for (CloudPoint localPoint : trackedObject.getCoordinates()) {
            double x_global=localPoint.getX()*Math.cos(yaw_rad)-localPoint.getY()*Math.sin(yaw_rad)+poses.getLast().getX();
            double y_global=localPoint.getX()*Math.sin(yaw_rad)+localPoint.getY()*Math.cos(yaw_rad)+poses.getLast().getY();
            globalCoordinates.add(new CloudPoint(x_global, y_global));
        }
        return new LandMark(trackedObject.getId(),trackedObject.getDescription(),globalCoordinates);

    }


    private ArrayList<CloudPoint> averageCoordinates(List<CloudPoint> existing, List<CloudPoint> incoming) {
        ArrayList<CloudPoint> result = new ArrayList<>();
        int size = Math.min(existing.size(), incoming.size());

        for (int i = 0; i < size; i++) {
            double avgX = (existing.get(i).getX() + incoming.get(i).getX()) / 2;
            double avgY = (existing.get(i).getY() + incoming.get(i).getY()) / 2;
            result.add(new CloudPoint(avgX, avgY));
        }

        return result;
    }
    private  void updateLandmarks(LandMark newLandmark) {//check if this realy needs to be synchronized or there is better solution!!!!

        LandMark oldMark = landmarks.get(newLandmark.getId());
        if (oldMark == null) {
            // Add the new landmark to the map
            landmarks.put(newLandmark.getId(), newLandmark);
        }
        // Refine the existing landmark by averaging coordinates
        else{
        ArrayList<CloudPoint> averagedCoordinates = averageCoordinates(oldMark.getCoordinates(), newLandmark.getCoordinates());
        oldMark.setCoordinates(averagedCoordinates);
        }
    }
    public Map<String, LandMark> getLandmarks() {
        return landmarks;
    }
}
    
