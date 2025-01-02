package bgu.spl.mics.application.objects;

/**
 * Represents objects detected by the camera at a specific timestamp. Includes
 * the time of detection and a list of detected objects.
 */
import java.util.ArrayList;

public class StampedDetectedObjects {

    public final int time; // The time at which the objects were detected
    public final ArrayList<DetectedObject> detectedObjects; // List of detected objects

    public StampedDetectedObjects(int time, ArrayList<DetectedObject> detectedObjects) {
        this.time = time;
        this.detectedObjects = detectedObjects;
    }

    public int getTime() {
        return time;
    }

    //return the list of DetectedObject instances.
    public ArrayList<DetectedObject> getDetectedObjects() {
        return detectedObjects;
    }

    @Override
    public String toString() {
        return "StampedDetectedObjects{"
                + "time=" + time
                + ", detectedObjects=" + detectedObjects
                + '}';
    }

}
