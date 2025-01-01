package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.List;

public class lidarData {
    private int time;
    private String id;
    private ArrayList<ArrayList<Double>> cloudPoints;

    // Getters and setters
    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    public ArrayList<ArrayList<Double>> get3pts() {
        return cloudPoints;
    }


    public void setCloudPoints(ArrayList<ArrayList<Double>> cloudPoints) {
        this.cloudPoints = cloudPoints;
    }
}
    

