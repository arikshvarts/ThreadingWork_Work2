package bgu.spl.mics.application.messages;
import java.util.ArrayList;

import bgu.spl.mics.Event;
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
}