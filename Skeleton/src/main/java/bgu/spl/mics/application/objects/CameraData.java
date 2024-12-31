package bgu.spl.mics.application.objects;

import java.util.List;

public class CameraData {
    public int time;
    public List<DetectedObject> detectedObjects;

    // Getters
    public int getTime() {
        return time;
    }

    public List<DetectedObject> getDetectedObjects() {
        return detectedObjects;
    }

    @Override
    public String toString() {
        return "CameraData{" +
                "time=" + time +
                ", detectedObjects=" + detectedObjects +
                '}';
    }
}
