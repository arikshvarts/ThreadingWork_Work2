package bgu.spl.mics.application;

import java.util.ArrayList;

import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;
import bgu.spl.mics.application.objects.CloudPoint;
import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.objects.StatisticalFolder;
import bgu.spl.mics.application.objects.TrackedObject;

/**
 * The main entry point for the GurionRock Pro Max Ultra Over 9000 simulation.
 * <p>
 * This class initializes the system and starts the simulation by setting up
 * services, objects, and configurations.
 * </p>
 */
public class GurionRockRunner {

    /**
     * The main method of the simulation. This method sets up the necessary
     * components, parses configuration files, initializes services, and starts
     * the simulation.
     *
     * @param args Command-line arguments. The first argument is expected to be
     * the path to the configuration file.
     */
    public static void main(String[] args) {
//implement countdownlatch so the tick will start only after initiallize of everyone

// public class ErrorInfo {
//     private ArrayList<DetectObjectsEvent> cameras_last_frames; //the last frame of all the cameras
//     private ArrayList<TrackedObjectsEvent> lidars_last_frames; //the last frame of all the Lidars
//     private ArrayList<Pose> poses; //All the robot poses up to the tick where the error occurred.
//     private StatisticalFolder stat;

//TESTING the error output file
        DetectedObject obj1 = new DetectedObject("obj1", "descr1");
        DetectedObject obj2 = new DetectedObject("obj2", "descr2");
        ArrayList<DetectedObject> objects = new ArrayList<DetectedObject>();
        objects.add(obj1); objects.add(obj2);
        DetectObjectsEvent det_eve_1 = new DetectObjectsEvent(2, objects);
        ArrayList<String> cameras_keys = new ArrayList<>();
        cameras_keys.add("Camera1"); cameras_keys.add("Camera2");
        DetectedObject obj3 = new DetectedObject("obj3", "descr3");
        ArrayList<DetectedObject> objects2 = new ArrayList<DetectedObject>();
        objects2.add(obj3);
        DetectObjectsEvent det_eve_2 = new DetectObjectsEvent(2, objects2);
        final ArrayList<DetectObjectsEvent> cameras_last_frames = new ArrayList<>();
        cameras_last_frames.add(det_eve_1); cameras_last_frames.add(det_eve_2); //camera last frames

                ArrayList<CloudPoint> coordinates1 = new ArrayList<>();
        coordinates1.add(new CloudPoint(1.0, 2.0));
        coordinates1.add(new CloudPoint(4.0, 5.0));

        ArrayList<CloudPoint> coordinates2 = new ArrayList<>();
        coordinates2.add(new CloudPoint(7.0, 8.0));
        coordinates2.add(new CloudPoint(10.0, 11.0));

        // Create instances of TrackedObject
        TrackedObject trackedObject1 = new TrackedObject("Object1", "first tracked object", coordinates1, 2);
        TrackedObject trackedObject2 = new TrackedObject("Object2", "second tracked object", coordinates2, 2);
        ArrayList<TrackedObject> tr_eve= new ArrayList<>();
        final ArrayList<TrackedObjectsEvent> lidars_last_frames = new ArrayList<>();
        lidars_last_frames.add(new TrackedObjectsEvent(tr_eve, 2));
        ArrayList<Pose> poses = new ArrayList<>();
        Pose pose1 = new Pose(10.5, 20.3, 1.57, 100);
        Pose pose2 = new Pose(15.0, 25.5, 0.78, 100);
        poses.add(pose1); poses.add(pose2); //poses
        StatisticalFolder stat = StatisticalFolder.getInstance();
        stat.incrementDetectedObjects(2);
        stat.incrementDetectedObjects(4);
        stat.incrementLandmarks(5);

        //now create the ErrorInfo instance
        ErrorInfo err_info = new ErrorInfo();
        err_info.AddCamerasLastFrames(det_eve_1); err_info.AddCamerasLastFrames(det_eve_2);
        err_info.AddLidarsLastFrames(new TrackedObjectsEvent(tr_eve, 2));
        err_info.setPoses(poses);
        err_info.set_crashed_brod(new CrashedBroadcast("faulty service", "err message"));
        err_info.add_cameras_keys_match_frame("Camera1");
        err_info.add_cameras_keys_match_frame("Camera2");
        err_info.createOutput();
    }
}
