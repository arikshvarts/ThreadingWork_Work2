package bgu.spl.mics.application.objects;

/**
 * Represents objects detected by the camera at a specific timestamp.
 * Includes the time of detection and a list of detected objects.
 */
import java.util.List;

public class StampedDetectedObjects {
    private final int time; // The time at which the objects were detected
    private final List<DetectedObject> detectedObjects; // List of detected objects

    public StampedDetectedObjects(int time, List<DetectedObject> detectedObjects) {
        this.time = time;
        this.detectedObjects = detectedObjects;
    }


    public int getTime() {
        return time;
    }


    //return the list of DetectedObject instances.
    public List<DetectedObject> getDetectedObjects() {
        return detectedObjects;
    }

    @Override
    public String toString() {
        return "StampedDetectedObjects{" +
                "time=" + time +
                ", detectedObjects=" + detectedObjects +
                '}';
    }

}

