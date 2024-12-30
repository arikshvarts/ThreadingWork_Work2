package bgu.spl.mics.application;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.objects.StatisticalFolder;

public class ErrorInfo {
    private ArrayList<DetectObjectsEvent> cameras_last_frames; //the last frame of all the cameras
    private ArrayList<TrackedObjectsEvent> lidars_last_frames; //the last frame of all the Lidars
    private ArrayList<Pose> poses; //All the robot poses up to the tick where the error occurred.
    private StatisticalFolder stat;

    
    private ErrorInfo() {
        this.cameras_last_frames = new ArrayList<>();
        this.lidars_last_frames = new ArrayList<>();
        this.poses = new ArrayList<>();
        this.stat = StatisticalFolder.getInstance();
    }

    /**
     * Nested static class responsible for holding the singleton instance of ErrorInfo.
     * This ensures lazy initialization and thread safety without requiring synchronization.
     */
    private static class ErrorInfoHolder {
        private static final ErrorInfo INSTANCE = new ErrorInfo();
    }

    /**
     * Public method to provide access to the singleton instance.
     * @return the singleton instance of ErrorInfo
     */
    public static ErrorInfo getInstance() {
        return ErrorInfoHolder.INSTANCE;
    }

    // Getter and Setter methods for the fields

    public ArrayList<DetectObjectsEvent> getCamerasLastFrames() {
        return cameras_last_frames;
    }

    public void AddCamerasLastFrames(DetectObjectsEvent frame) {
        cameras_last_frames.add(frame);
    }

    public ArrayList<TrackedObjectsEvent> getLidarsLastFrames() {
        return lidars_last_frames;
    }

    public void AddLidarsLastFrames(TrackedObjectsEvent frame) {
        lidars_last_frames.add(frame);
    }

    public ArrayList<Pose> getPoses() {
        return poses;
    }

    public void setPoses(ArrayList<Pose> poses) {
        this.poses = poses;
    }


    /**
     * Creates a JSON file named error_output.json containing the serialized error information.
     * 
     * @param stat an object providing system statistics
     */

     //Arik Ah shely this function that create the error_output.json is AhuSharmuta gpt so we need to check if it create the file in the format we excpected <3 
    public void createOutput() {
        JsonObject outputJson = new JsonObject();

        // Serialize cameras_last_frames
        JsonArray camerasArray = new JsonArray();
        for (DetectObjectsEvent event : cameras_last_frames) {
            camerasArray.add(event.toString());
        }
        outputJson.add("cameras_last_frames", camerasArray);

        // Serialize lidars_last_frames
        JsonArray lidarsArray = new JsonArray();
        for (TrackedObjectsEvent event : lidars_last_frames) {
            lidarsArray.add(event.toString());
        }
        outputJson.add("lidars_last_frames", lidarsArray);

        // Serialize lidars_last_frames
        JsonArray posesArray = new JsonArray();
        for (Pose pose : poses) {
            posesArray.add(pose.toString());
        }
        outputJson.add("lidars_last_frames", posesArray);
        // Add system statistics
        outputJson.addProperty("system_runtime", stat.getSystemRuntime());
        outputJson.addProperty("num_detected_objects", stat.getNumDetectedObjects());
        outputJson.addProperty("num_tracked_objects", stat.getNumTrackedObjects());
        outputJson.addProperty("num_landmarks", stat.getNumLandmarks());

        // Write to JSON file
        try (FileWriter file = new FileWriter("error_output.json")) {
            file.write(outputJson.toString());
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}