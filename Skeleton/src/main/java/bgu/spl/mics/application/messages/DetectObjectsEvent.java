package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.StampedDetectedObjects;

public class DetectObjectsEvent implements Event<Boolean> {

    private final int time;                       // Time the objects were detected
    private final StampedDetectedObjects obj;  // List of detected objects


    public DetectObjectsEvent(int time, StampedDetectedObjects obj) {
        this.time = time;
        this.obj = obj;
    }

    public int getTime() {
        return time;
    }

    public StampedDetectedObjects getObjects() {
        return obj;
    }
}
