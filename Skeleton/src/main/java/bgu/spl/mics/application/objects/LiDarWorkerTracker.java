package bgu.spl.mics.application.objects;

import java.util.ArrayList;

import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;

/**
 * LiDarWorkerTracker is responsible for managing a LiDAR worker.
 * It processes DetectObjectsEvents and generates TrackedObjectsEvents by using data from the LiDarDataBase.
 * Each worker tracks objects and sends observations to the FusionSlam service.
 */
public class LiDarWorkerTracker {

    private final int id; // The ID of the LiDar
    private final int frequency; // The time interval at which the LiDar sends new events
    private STATUS status=STATUS.UP; // The status of the LiDar
    private ArrayList<TrackedObject> lastTrackedObjects=null; // The last objects the LiDar tracked

    public LiDarWorkerTracker(int id, int frequency) {
        this.id = id;
        this.frequency = frequency;
        // this.status = status;
        // this.lastTrackedObjects = lastTrackedObjects;
    }


    public int getId() {
        return id;
    }

    public int getFrequency() {
        return frequency;
    }

    public STATUS getStatus() {
        return status;
    }

    public ArrayList<TrackedObject> getLastTrackedObjects() {
        return lastTrackedObjects;
    }

    // Setter for status
    public void setStatus(STATUS status) {
        this.status = status;
    }

    // Setter for lastTrackedObjects (if needed)
    public void setLastTrackedObjects(ArrayList<TrackedObject> lastTrackedObjects) {
        this.lastTrackedObjects = lastTrackedObjects;
    }
    
    public TrackedObjectsEvent handleDetectedObjectsEvent(DetectObjectsEvent det){
        ArrayList<TrackedObject> trackedObjects = new ArrayList<>();
        ArrayList<StampedCloudPoints> objects_at_time  = LiDarDataBase.getInstance().getMapTimeHashMap().get(det.getTime());
        for(StampedCloudPoints tracked : objects_at_time){
            //need to continue and find from all tracked in this time in the hash, only yhe objects detected from camera
            for(DetectedObject obj : det.getObjects()){
                if(tracked.getId() == "ERROR"){
                    //if tracked object with id = ERROR
                    setStatus(STATUS.ERROR);
                    break;
                }
                if(obj.getId() == tracked.getId()){
                    trackedObjects.add(new TrackedObject(tracked.getId(), obj.getDescription(), tracked.getCloudPoints(), det.getTime()));
                }
            }
        }
        return(new TrackedObjectsEvent(trackedObjects, det.getTime()));
    }
    
}
