package bgu.spl.mics.application.objects;

import java.util.ArrayList;
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
    private FusionSlam() {
        this.landmarks=new ArrayList<LandMark>();
        this.poses=new ArrayList<Pose>();
        this.stats = StatisticalFolder.getInstance();
    }
    public static FusionSlam getInstance() {
        return FusionSlamHolder.instance;
    }

    // public void processObjects(ArrayList<TrackedObject> trackedObjects) {

    //     for (TrackedObject trackedObject : trackedObjects) {
    //         LandMark lndMark = translateCoordinateSys(trackedObject);
    //         updateLandmarks(lndMark);
    //     }
    // }   
    public void processObjectsAtTime(ArrayList<TrackedObject> trackedObjects,int time) {
        for (TrackedObject trackedObject : trackedObjects) {
            for (Pose pose : poses){
                if (pose.getTime()==trackedObject.getTime()){
                    LandMark lndMark = translateCoordinateSys(trackedObject,pose);
                    updateLandmarks(lndMark);
                    flag=true;
                }
            }
            if (flag==false){
                waitingObjs.add(trackedObject);
            }

    }
    }   


    public  void updatePose(ArrayList<Pose> recPoses) {
        this.poses=recPoses;//check if synchronized is needed

    }
    public  void addPose(Pose lastPose) {
        this.poses.add(lastPose);//check if synchronized is needed

    }



    public LandMark translateCoordinateSys(TrackedObject trackedObject,Pose pose) {
        ArrayList<CloudPoint> globalCoordinates = new ArrayList<>();
        double yaw_rad=pose.getYaw()*Math.PI/180;
        for (ArrayList<Double> localPoint : trackedObject.getCoordinates()) {
            double x_global=localPoint.get(0)*Math.cos(yaw_rad)-localPoint.get( 1)*Math.sin(yaw_rad)+pose.getX();
            double y_global=localPoint.get(0)*Math.sin(yaw_rad)+localPoint.get(1)*Math.cos(yaw_rad)+pose.getY();
            globalCoordinates.add(new CloudPoint(x_global, y_global,0));
        }
        return new LandMark(trackedObject.getId(),trackedObject.getDescription(),globalCoordinates);

    }


    private ArrayList<CloudPoint> averageCoordinates(List<CloudPoint> existing, List<CloudPoint> incoming) {
        ArrayList<CloudPoint> result = new ArrayList<>();
        int sizeSmaller = Math.min(existing.size(), incoming.size());
        int sizeBigger = Math.max(existing.size(), incoming.size());
        List<CloudPoint> BigggerList = existing.size() > incoming.size() ? existing : incoming;

        for (int i = 0; i < sizeSmaller; i++) {
            double avgX = (existing.get(i).getX() + incoming.get(i).getX()) / 2;
            double avgY = (existing.get(i).getY() + incoming.get(i).getY()) / 2;
            result.add(new CloudPoint(avgX, avgY,0));
            if (i == sizeSmaller - 1) {
                stats.incrementLandmarks(1);
            }
        }
        for (int i = sizeSmaller; i < sizeBigger; i++) {

            result.add(new CloudPoint(BigggerList.get(i).getX(), BigggerList.get(i).getY(),0));
            if (i == sizeBigger - 1) {
                stats.incrementLandmarks(1);
            }
        }

        return result;
    }
    private  void updateLandmarks(LandMark newLandmark) {//check if this realy needs to be synchronized or there is better solution!!!!
        boolean flag =true;
        for (LandMark landM : landmarks){
            if (landM.getId().equals(newLandmark.getId())){
                flag=false;
                ArrayList<CloudPoint> averagedCoordinates = averageCoordinates(landM.getCoordinates(), newLandmark.getCoordinates());
                landM.setCoordinates(averagedCoordinates);
            }
        }

        // Refine the existing landmark by averaging coordinates
        if (!flag) {
            stats.incrementLandmarks(1);
            landmarks.add(newLandmark);
        }
        }
    
    public List<LandMark> getLandmarks() {
        return landmarks;
    }
}
    
