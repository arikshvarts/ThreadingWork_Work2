package bgu.spl.mics.application.messages;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.CloudPoint;
import bgu.spl.mics.application.objects.TrackedObject;


public class TrackedObjectsEvent implements Event<Boolean> {
    private int time;
    private final ArrayList<TrackedObject> trackedObjects;

    public TrackedObjectsEvent(ArrayList<TrackedObject> trackedObjects, int time) {
        this.trackedObjects = trackedObjects;
        this.time = time;
    }

    public ArrayList<TrackedObject> getTrackedObjects() {
        return trackedObjects;
    }
    public int getTime(){
        return time;
    }

    //to string method to use in the error file

    @Override
public String toString() {
    JsonObject mainJson = new JsonObject();
    
    // Create the outer array for LiDarWorkerTracker1
    JsonArray trackedObjectsArray = new JsonArray();
    
    for (int i = 0; i < trackedObjects.size(); i++) {
        TrackedObject obj = trackedObjects.get(i);
        JsonObject trackedObjectJson = new JsonObject();
        
        trackedObjectJson.addProperty("id", obj.getId());
        trackedObjectJson.addProperty("time", obj.getTime());
        trackedObjectJson.addProperty("description", obj.getDescription());
        
        // Handle coordinates
        JsonArray coordinatesArray = new JsonArray();
        for (int j = 0; j < obj.getCoordinates().size(); j++) {
            CloudPoint coord = obj.getCoordinates().get(j);
            JsonObject coordJson = new JsonObject();
            coordJson.addProperty("x", coord.getX());
            coordJson.addProperty("y", coord.getY());
            coordinatesArray.add(coordJson);
        }
        
        trackedObjectJson.add("coordinates", coordinatesArray);
        trackedObjectsArray.add(trackedObjectJson);
    }
    
    // Add the final array to the main object under "LiDarWorkerTracker1"
    mainJson.add("LiDarWorkerTracker1", trackedObjectsArray);
    
    return new Gson().toJson(mainJson);
    }
}