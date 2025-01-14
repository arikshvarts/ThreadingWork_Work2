package bgu.spl.mics.application.objects;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.FileWriter;
import java.io.IOException;

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
    private StatisticalFolder stat;



    // Singleton instance holder
    private static class FusionSlamHolder {
        private static final FusionSlam instance = new FusionSlam();

    }
    public FusionSlam() {
        this.landmarks=new ArrayList<LandMark>();
        this.poses=new ArrayList<Pose>();
        this.stats = StatisticalFolder.getInstance();
        this.waitingObjs=new ArrayList<TrackedObject>();
        this.stat = StatisticalFolder.getInstance();
    }
    public static FusionSlam getInstance() {
        return FusionSlamHolder.instance;
    }
  
    /**
 * Adds a new landmark or updates an existing one based on the given `TrackedObject`.
 *
 * Preconditions:
 * - The `trackedObject` parameter must not be null.
 * - The `trackedObject` must have a valid `id`, `description`, and a non-empty list of coordinates.
 *
 * Postconditions:
 * - If a landmark with the same ID already exists, its coordinates are updated using an average calculation.
 * - If no matching landmark exists and pose data is available, a new landmark is added.
 * - If no matching landmark exists and pose data is missing, the object is added to the `waitingObjs` list for later processing.
 *
 * Invariants:
 * - The `landmarks` list must not contain duplicate landmarks with the same ID.
 * - The size of the `landmarks` list should only increase when a new landmark is added.
 * - The `waitingObjs` list should only grow if pose data is unavailable for the tracked object.
 */

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

    /**
 * Translates the local coordinates of a `TrackedObject` into global coordinates based on its timestamp and pose.
 *
 * Preconditions:
 * - The `trackedObject` parameter must not be null.
 * - The `trackedObject` must have a valid `time` field and at least one coordinate point.
 * - The `poses` list must contain at least one `Pose` object matching the tracked object's time.
 *
 * Postconditions:
 * - If a corresponding `Pose` exists, a new list of global `CloudPoint` coordinates is returned.
 * - If no matching `Pose` is found, the method returns `null`.
 *
 * Invariants:
 * - The `poses` and `trackedObject` data must remain unchanged after this method executes.
 * - The returned list of global coordinates, if not null, should match the size of the input coordinate list.
 */

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
    public ArrayList<LandMark> getLandMarks(){
        return landmarks;
    }

     //createoutput function, will be called if all services terminated without crash.
    //still need to check how the file looks
    public void createOutput() {
    JsonObject outputJson = new JsonObject();

    // Add statistics
    JsonObject statistics = new JsonObject();
    statistics.addProperty("systemRuntime", stat.getSystemRuntime().get());
    statistics.addProperty("numDetectedObjects", stat.getNumDetectedObjects().get());
    statistics.addProperty("numTrackedObjects", stat.getNumTrackedObjects().get());
    statistics.addProperty("numLandmarks", stat.getNumLandmarks().get());
    outputJson.add("statistics", statistics);

    // Add landmarks
    JsonArray landMarksArray = new JsonArray();
    for (LandMark landmark : getLandMarks()) { // Assuming getLandMarks() returns a list of LandMark objects
        JsonObject landMarkJson = new JsonObject();
        landMarkJson.addProperty("id", landmark.getId());
        landMarkJson.addProperty("description", landmark.getDescription());

        JsonArray coordinatesArray = new JsonArray();
        for (CloudPoint point : landmark.getCoordinates()) {
            JsonObject coordinate = new JsonObject();
            coordinate.addProperty("x", point.getX());
            coordinate.addProperty("y", point.getY());
            coordinatesArray.add(coordinate);
        }
        landMarkJson.add("coordinates", coordinatesArray);
        landMarksArray.add(landMarkJson);
    }
    outputJson.add("landMarks", landMarksArray);

    // Write JSON to file
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    try (FileWriter file = new FileWriter("output_file.json")) {
        gson.toJson(outputJson, file);
        file.flush();
    } catch (IOException e) {
        e.printStackTrace();
    }
}
}
    
