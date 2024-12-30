package bgu.spl.mics.application.messages;

import java.util.ArrayList;
import java.util.List;

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
}
