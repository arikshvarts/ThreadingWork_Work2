package bgu.spl.mics.application.objects;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import bgu.spl.mics.application.messages.DetectObjectsEvent;

public class Camera {

    private int id;
    private int frequency;
    private STATUS status;
    private final ArrayList<StampedDetectedObjects> detectedObjectsList;
    private final String dataFilePath; //the path to this camera data we have as a string in the Configuration JSON File
    private final ArrayList<StampedDetectedObjects> CameraData;


    public Camera(int id, int frequency, STATUS status, String dataFilePath) {
        this.id = id;
        this.frequency = frequency;
        this.status = status;
        this.detectedObjectsList = new ArrayList<>();
        this.dataFilePath = dataFilePath;
        this.CameraData = parseCameraData();

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public STATUS getStatus() {
        return status;
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }

    public ArrayList<StampedDetectedObjects> getDetectedObjectsList() {
        return detectedObjectsList;
    }

    public void addDetectedObjects(int time, ArrayList<DetectedObject> detectedObjects) {
        detectedObjectsList.add(new StampedDetectedObjects(time, detectedObjects));
    }

    //parsing from camera_data.json
    private ArrayList<StampedDetectedObjects> parseCameraData() {
        try (FileReader reader = new FileReader(dataFilePath)) {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<StampedDetectedObjects>>() {
            }.getType();
            return gson.fromJson(reader, listType);
        } catch (IOException e) {
            System.err.println("Error reading camera data file for Camera " + id + ": " + e.getMessage());
            return new ArrayList<>(); // Return an empty list if an error occurs
        }
    }

    public DetectObjectsEvent handleTick(int currTime) {
        for (StampedDetectedObjects data : CameraData) {
            if (data.getTime() == currTime - frequency) {
                //we detected objects at tick-frequency
                // for (DetectedObject obj : data.getDetectedObjects()) {
                    //the logic is to check for each object the camera detected now if it didn't detect untill now
                    //add it to allOjects in the Statsanager and increment num of detected objects
                    
                    // if (StatsManager.getAllObjects().contains(obj) == false) {
                    //     StatsManager.getAllObjects().add(obj);
                    //     statsFolder.incrementDetectedObjects(1);
                    // }
                    
                return new DetectObjectsEvent(currTime, data.getDetectedObjects());
            }
            // Return an empty List if there are no objects that detected at this time
        }
        return new DetectObjectsEvent(currTime, new ArrayList<>());
    }

    @Override
    public String toString() {
        return "Camera{id=" + id + ", frequency=" + frequency + ", status=" + status + ", detectedObjectsList=" + detectedObjectsList + "}";
    }

}
