package bgu.spl.mics.application.objects;
import java.util.ArrayList;
/**
 * Represents a landmark in the environment map.
 * Landmarks are identified and updated by the FusionSlam service.
 */
class Landmark {
    private  String id;
    private  String description;
    private  ArrayList<CloudPoint> coordinates;

    public Landmark(String id, String description, ArrayList<CloudPoint> coordinates) {
        this.id = id;
        this.description = description;
        this.coordinates = coordinates;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public ArrayList<CloudPoint> getCoordinates() {
        return coordinates;
    }
}
