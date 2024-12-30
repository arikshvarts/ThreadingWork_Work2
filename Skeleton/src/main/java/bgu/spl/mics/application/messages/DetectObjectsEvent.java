package bgu.spl.mics.application.messages;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.DetectedObject;


public class DetectObjectsEvent implements Event<Boolean> {

    private final int time;                       // Time the objects were detected
    private final ArrayList<DetectedObject> objects;  // List of detected objects

    public DetectObjectsEvent(int time, ArrayList<DetectedObject> objects) {
        this.time = time;
        this.objects = objects;
    }

    public int getTime() {
        return time;
    }

    public List<DetectedObject> getObjects() {
        return objects;
    }

    //to string method to use in the error json file
    @Override
    public String toString() {
        Gson gson = new Gson();
        JsonObject cameraJson = new JsonObject();
        
        cameraJson.addProperty("time", time);
        
        JsonArray detectedObjectsArray = new JsonArray();
        for (DetectedObject obj : objects) {
            JsonObject objJson = new JsonObject();
            objJson.addProperty("id", obj.getId());
            objJson.addProperty("description", obj.getDescription());
            detectedObjectsArray.add(objJson);
        }
        
        cameraJson.add("detectedObjects", detectedObjectsArray);
        
        JsonObject finalJson = new JsonObject();
        finalJson.add("Camera1", cameraJson);
        
        return gson.toJson(finalJson);
    }


}
