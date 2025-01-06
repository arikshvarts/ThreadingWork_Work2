package bgu.spl.mics.application;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.FileWriter;
import java.io.IOException;
import com.google.gson.GsonBuilder;

import bgu.spl.mics.ParsingJsonFiles;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;
import bgu.spl.mics.application.objects.CloudPoint;
import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.objects.StatisticalFolder;
import bgu.spl.mics.application.objects.TrackedObject;
import bgu.spl.mics.ParsingJsonFiles;

public class ErrorInfo {
    private ArrayList<DetectObjectsEvent> cameras_last_frames; //the last frame of all the cameras
    private ArrayList<String> cameras_keys_match_frame;
    private ArrayList<TrackedObjectsEvent> lidars_last_frames; //the last frame of all the Lidars
    private ArrayList<Pose> poses; //All the robot poses up to the tick where the error occurred.
    private StatisticalFolder stat;
    private CrashedBroadcast crash;

    
    public ErrorInfo(int num_of_cameras, int num_of_lidars) {
        this.cameras_last_frames = new ArrayList<>();
        for(int i=0 ; i<num_of_cameras ; i++){
            cameras_last_frames.add(new DetectObjectsEvent(0, new ArrayList<DetectedObject>()));
        }
        this.lidars_last_frames = new ArrayList<>();
        for(int i=0 ; i<num_of_lidars ; i++){
            lidars_last_frames.add(new TrackedObjectsEvent(new ArrayList<TrackedObject>(), 0));
        }
        this.poses = new ArrayList<>();
        this.stat = StatisticalFolder.getInstance();
        this.cameras_keys_match_frame = new ArrayList<>();
        this.crash = null;
    }

    /**
     * Nested static class responsible for holding the singleton instance of ErrorInfo.
     * This ensures lazy initialization and thread safety without requiring synchronization.
     */
    private static class ErrorInfoHolder {
        private static ErrorInfo INSTANCE = null;
    }

    /**
     * Public method to provide access to the singleton instance.
     * @return the singleton instance of ErrorInfo
     */
    public static ErrorInfo getInstance() {
        if(ErrorInfoHolder.INSTANCE != null){
        return ErrorInfoHolder.INSTANCE;
        }
        else{ throw new IllegalStateException("ErrorInfo has not been initialized yet!");}
    }

    public static void initalize(int num_of_cameras, int num_of_lidars){
        if(ErrorInfoHolder.INSTANCE == null){
            ErrorInfoHolder.INSTANCE = new ErrorInfo(num_of_cameras, num_of_lidars);
        }
        else{
            throw new IllegalStateException("ErrorInfo has already been initialized!");
        }
    }


    // Getter and Setter methods for the fields
    
    public void set_crashed_brod(CrashedBroadcast c){
        this.crash = c;
    }

    public ArrayList<DetectObjectsEvent> getCamerasLastFrames() {
        return cameras_last_frames;
    }

    public void UpdateCamerasLastFrames(DetectObjectsEvent frame, String key) {
        int index = cameras_keys_match_frame.indexOf(key);
        //updating the camera in the index matching the key with last frame
        cameras_last_frames.set(index, frame);
    }

    public ArrayList<TrackedObjectsEvent> getLidarsLastFrames() {
        return lidars_last_frames;
    }

    public void UpdateLidarsLastFrames(TrackedObjectsEvent frame, int Lidar_ID) {
        lidars_last_frames.set(Lidar_ID - 1 , frame) ;
    }

    public ArrayList<Pose> getPoses() {
        return poses;
    }

    public void setPoses(ArrayList<Pose> poses) {
        this.poses = poses;
    }

    public void add_cameras_keys_match_frame(String key){
        cameras_keys_match_frame.add(key);
    }

    /**
     * Creates a JSON file named error_output.json containing the serialized error information.
     * 
     * @param stat an object providing system statistics
     */   


// public void createOutput() {
//     JsonObject outputJson = new JsonObject();

//     // Add error context from the CrashedBroadcast field
//     if (crash != null) {
//         outputJson.addProperty("error", crash.getErrorMessage());
//         outputJson.addProperty("faultySensor", crash.getFaultyServiceName());
//     } else {
//         // Default values in case crash is null
//         outputJson.addProperty("error", "Unknown error");
//         outputJson.addProperty("faultySensor", "Unknown sensor");
//     }

//     // Add last cameras frame
//     JsonObject lastCamerasFrame = new JsonObject();
//     for (int i = 0; i < cameras_last_frames.size(); i++) {
//         DetectObjectsEvent event = cameras_last_frames.get(i);
//         JsonObject cameraFrame = new JsonObject();
//         cameraFrame.addProperty("time", event.getTime());

//         JsonArray detectedObjectsArray = new JsonArray();
//         for (DetectedObject obj : event.getObjects()) {
//             JsonObject detectedObject = new JsonObject();
//             detectedObject.addProperty("id", obj.getId());
//             detectedObject.addProperty("description", obj.getDescription());
//             detectedObjectsArray.add(detectedObject);
//         }

//         cameraFrame.add("detectedObjects", detectedObjectsArray);
//         lastCamerasFrame.add(cameras_keys_match_frame.get(i), cameraFrame);
//     }
//     outputJson.add("lastCamerasFrame", lastCamerasFrame);

//     // Add last LiDAR trackers frame
//     JsonObject lastLiDarWorkerTrackersFrame = new JsonObject();
//     for (TrackedObjectsEvent event : lidars_last_frames) {
//         JsonArray trackedObjectsArray = new JsonArray();
//         for (TrackedObject obj : event.getTrackedObjects()) {
//             JsonObject trackedObject = new JsonObject();
//             trackedObject.addProperty("id", obj.getId());
//             trackedObject.addProperty("time", obj.getTime());
//             trackedObject.addProperty("description", obj.getDescription());

//             JsonArray coordinatesArray = new JsonArray();
//             for (CloudPoint point : obj.getCoordinates()) {
//                 JsonObject coordinate = new JsonObject();
//                 coordinate.addProperty("x", point.getX());
//                 coordinate.addProperty("y", point.getY());
//                 coordinatesArray.add(coordinate);
//             }

//             trackedObject.add("coordinates", coordinatesArray);
//             trackedObjectsArray.add(trackedObject);
//         }
//         lastLiDarWorkerTrackersFrame.add("LiDarWorkerTracker1", trackedObjectsArray);
//     }
//     outputJson.add("lastLiDarWorkerTrackersFrame", lastLiDarWorkerTrackersFrame);

//     // Add poses
//     JsonArray posesArray = new JsonArray();
//     for (Pose pose : poses) {
//         JsonObject poseJson = new JsonObject();
//         poseJson.addProperty("time", pose.getTime());
//         poseJson.addProperty("x", pose.getX());
//         poseJson.addProperty("y", pose.getY());
//         poseJson.addProperty("yaw", pose.getYaw());
//         posesArray.add(poseJson);
//     }
//     outputJson.add("poses", posesArray);

//     // Add statistics
//     JsonObject statistics = new JsonObject();
//     statistics.addProperty("systemRuntime", stat.getSystemRuntime());
//     statistics.addProperty("numDetectedObjects", stat.getNumDetectedObjects());
//     statistics.addProperty("numTrackedObjects", stat.getNumTrackedObjects());
//     statistics.addProperty("numLandmarks", stat.getNumLandmarks());
//     outputJson.add("statistics", statistics);

//     // Create Gson object with pretty printing enabled
//     Gson gson = new GsonBuilder().setPrettyPrinting().create();

//     // Write to JSON file with proper indentation
//     try (FileWriter file = new FileWriter("err_output.json")) {
//         gson.toJson(outputJson, file);  // This will automatically add line breaks and indentation
//         file.flush();
//     } catch (IOException e) {
//         e.printStackTrace();
//     }
// }


public void createOutput() {
    JsonObject outputJson = new JsonObject();

    // Add error context from the CrashedBroadcast field
    if (crash != null) {
        outputJson.addProperty("error", crash.getErrorMessage());
        outputJson.addProperty("faultySensor", crash.getFaultyServiceName());
    } else {
        outputJson.addProperty("error", "Unknown error");
        outputJson.addProperty("faultySensor", "Unknown sensor");
    }

    // Add last cameras frame (Unchanged)
    JsonObject lastCamerasFrame = new JsonObject();
    for (int i = 0; i < cameras_last_frames.size(); i++) {
        DetectObjectsEvent event = cameras_last_frames.get(i);
        JsonObject cameraFrame = new JsonObject();
        cameraFrame.addProperty("time", event.getTime());

        JsonArray detectedObjectsArray = new JsonArray();
        for (DetectedObject obj : event.getObjects()) {
            JsonObject detectedObject = new JsonObject();
            detectedObject.addProperty("id", obj.getId());
            detectedObject.addProperty("description", obj.getDescription());
            detectedObjectsArray.add(detectedObject);
        }
        cameraFrame.add("detectedObjects", detectedObjectsArray);
        lastCamerasFrame.add(cameras_keys_match_frame.get(i), cameraFrame);
    }
    outputJson.add("lastCamerasFrame", lastCamerasFrame);

    JsonObject lastLiDarWorkerTrackersFrame = new JsonObject();
    int lidarIndex = 1; // To track Lidar IDs
    for (TrackedObjectsEvent event : lidars_last_frames) {
        String lidarKey = "LiDarTracker" + lidarIndex;
        JsonArray trackedObjectsArray = new JsonArray();
        
        for (TrackedObject obj : event.getTrackedObjects()) {
            JsonObject trackedObject = new JsonObject();
            trackedObject.addProperty("id", obj.getId());
            trackedObject.addProperty("time", obj.getTime());
            trackedObject.addProperty("description", obj.getDescription());

            // Adding object name and time specifically as requested
            trackedObject.addProperty("name", obj.getId());
            trackedObject.addProperty("time", obj.getTime());

            JsonArray coordinatesArray = new JsonArray();
            for (CloudPoint point : obj.getCoordinates()) {
                JsonObject coordinate = new JsonObject();
                coordinate.addProperty("x", point.getX());
                coordinate.addProperty("y", point.getY());
                coordinatesArray.add(coordinate);
            }

            trackedObject.add("coordinates", coordinatesArray);
            trackedObjectsArray.add(trackedObject);
        }

        lastLiDarWorkerTrackersFrame.add(lidarKey, trackedObjectsArray);
        lidarIndex++;
    }
    outputJson.add("lastLiDarWorkerTrackersFrame", lastLiDarWorkerTrackersFrame);

    // Add poses (Unchanged)
    JsonArray posesArray = new JsonArray();
    for (Pose pose : poses) {
        JsonObject poseJson = new JsonObject();
        poseJson.addProperty("time", pose.getTime());
        poseJson.addProperty("x", pose.getX());
        poseJson.addProperty("y", pose.getY());
        poseJson.addProperty("yaw", pose.getYaw());
        posesArray.add(poseJson);
    }
    outputJson.add("poses", posesArray);

    // Add statistics (Unchanged)
    JsonObject statistics = new JsonObject();
    // stat.setSystemRuntime(stat.getSystemRuntime().decrementAndGet());
    statistics.addProperty("systemRuntime", stat.getSystemRuntime().decrementAndGet());
    statistics.addProperty("numDetectedObjects", stat.getNumDetectedObjects());
    statistics.addProperty("numTrackedObjects", stat.getNumTrackedObjects());
    statistics.addProperty("numLandmarks", stat.getNumLandmarks());
    outputJson.add("statistics", statistics);

    // Writing the JSON to a file with pretty printing
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    try (FileWriter file = new FileWriter("err_output.json")) {
        gson.toJson(outputJson, file);
        file.flush();
    } catch (IOException e) {
        e.printStackTrace();
    }
}



}