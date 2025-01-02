package bgu.spl.mics.application.objects;

import java.security.Key;
import java.util.ArrayList;


import bgu.spl.mics.application.messages.DetectObjectsEvent;

public class Camera {

    private int id;
    private int frequency;
    private STATUS status=STATUS.UP;
    private  ArrayList<StampedDetectedObjects> CameraData;
    private  int last_detected_time;
    private String cameraKey;
    // private ParsingJsonFiles parsed;

    // {
    //     try {
    //         parsed = new ParsingJsonFiles("C:\\Users\\ariks\\uni\\CodingEnviroments\\Work2_Threading\\Skeleton\\example_input_2\\configuration_file.json");
    //     } catch (IOException e) {
    //         e.printStackTrace();
    //     }
    // }



    public Camera(int id, int frequency,String CameraKey,ArrayList<StampedDetectedObjects> camDat) {
        this.id = id;
        this.frequency = frequency;
        this.cameraKey= CameraKey;
        this.CameraData = camDat;
        // this.last_detected_time = CameraData.getLast().getTime();
        this.last_detected_time = CameraData.get(CameraData.size()-1).getTime();
    }

    public String getkey(){
        return cameraKey;
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
    public String getKey(){
        return cameraKey;
    }

    public void setKey(String key) {
        this.cameraKey = key;
    }

    public STATUS getStatus() {
        return status;
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }

    public ArrayList<StampedDetectedObjects> getDetectedObjectsList() {
        return CameraData;
    }

    public void addDetectedObjects(int time, ArrayList<DetectedObject> detectedObjects) {
        CameraData.add(new StampedDetectedObjects(time, detectedObjects));
    }

    public int get_last_detected_time(){
        return last_detected_time;
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


    @Override
    public String toString() {
        return "Camera{id=" + id + ", frequency=" + frequency + ", status=" + status + ", detectedObjectsList=" + this.getDetectedObjectsList() + "}";
    }

}
