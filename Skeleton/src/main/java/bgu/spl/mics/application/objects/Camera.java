package bgu.spl.mics.application.objects;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Camera {
    private int id;
    private int frequency;
    private STATUS status;
    private ArrayList<StampedDetectedObjects> detectedObjectsList;
    private final String dataFilePath; //the path to this camera data we have as a string in the Configuration JSON File

    public Camera(int id, int frequency, STATUS status, String dataFilePath) {
        this.id = id;
        this.frequency = frequency;
        this.status = status;
        this.detectedObjectsList = new ArrayList<>();
        this.dataFilePath = dataFilePath;
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
    public DetectObjectsEvent handleTick(int currTime) {
        List<CameraData> cameraDataList = parseCameraData();
        for (CameraData data : cameraDataList) {
            if (data.getTime() == currTime) {
                return new DetectObjectsEvent(data.getDetectedObjects());
            }
        }
        // Return an empty List if there are no objects that detected at this time
        return new DetectObjectsEvent(new ArrayList<>());
    }
        //check how to relate to the frequency
    }

    @Override
    public String toString() {
        return "Camera{id=" + id + ", frequency=" + frequency + ", status=" + status + ", detectedObjectsList=" + detectedObjectsList + "}";
    }

}