package bgu.spl.mics.application.objects;

import java.util.ArrayList;

/**
 * Represents an object tracked by the LiDAR.
 * This object includes information about the tracked object's ID, description, 
 * time of tracking, and coordinates in the environment.
 */
public class TrackedObject {
    private final String id;
    private final String description;
    private final ArrayList<CloudPoint>  coordinates;
    private final int time;


    public TrackedObject(String id, String description, ArrayList<CloudPoint>  coordinates,int time) {
        this.id = id;
        this.description = description;
        this.coordinates = coordinates;
        this.time=time;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public ArrayList<CloudPoint>   getCoordinates() {
        return coordinates;
    }
    public int getTime() {
        return time;
    }
}
