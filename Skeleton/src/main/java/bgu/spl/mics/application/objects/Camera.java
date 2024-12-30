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
    private STATUS status=STATUS.UP;
    private  ArrayList<StampedDetectedObjects> detectedObjectsList=new ArrayList<>();
    private  String dataFilePath; //the path to this camera data we have as a string in the Configuration JSON File
    private  ArrayList<StampedDetectedObjects> CameraData=null;//change to relevent from pirsoor
    private  int last_detected_time=-99999;//change to relevent from pirsoor
    String cameraKey;



    public Camera(int id, int frequency,String cameraKey) {
        this.id = id;
        this.frequency = frequency;
        this.cameraKey= cameraKey;
        // this.detectedObjectsList = new ArrayList<>();
        // this.CameraData = parseCameraData();
        //leshanot barega shehapirsor over mitoch hacemra lemakom aher. laadcen how last detected time  gets its value
        // this.last_detected_time = getCameraData().get(getCameraData().size() - 1).getTime();


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

    public int get_last_detected_time(){
        return last_detected_time;
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
            //send only if frequency delay passed
            if (data.getTime() == currTime - frequency) {
                    
                return new DetectObjectsEvent(currTime, data.getDetectedObjects());
            }
            // Return an empty List if there are no objects that detected at this time
        }
        return new DetectObjectsEvent(currTime, new ArrayList<>());
    }

    public ArrayList<StampedDetectedObjects> getCameraData(){
        return CameraData;
    }

    @Override
    public String toString() {
        return "Camera{id=" + id + ", frequency=" + frequency + ", status=" + status + ", detectedObjectsList=" + detectedObjectsList + "}";
    }

}
