package bgu.spl.mics.application.messages;
import java.util.ArrayList;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.TrackedObject;

public class TrackedObjectsEvent implements Event<Boolean> {
    private final ArrayList<TrackedObject> trackedObjects;

    public TrackedObjectsEvent(ArrayList<TrackedObject> trackedObjects) {
        this.trackedObjects = trackedObjects;
    }

    public ArrayList<TrackedObject> getTrackedObjects() {
        return trackedObjects;
    }
}