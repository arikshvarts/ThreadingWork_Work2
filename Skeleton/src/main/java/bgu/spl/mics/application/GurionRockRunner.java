package bgu.spl.mics.application;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import bgu.spl.mics.Configuration;
import bgu.spl.mics.Configuration.CameraConfiguration;
import bgu.spl.mics.Configuration.Cameras;
import bgu.spl.mics.Configuration.LidarConfiguration;
import bgu.spl.mics.ParsingJsonFiles;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;
import bgu.spl.mics.application.services.*;
import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.CloudPoint;
import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.FusionSlam;
import bgu.spl.mics.application.objects.GPSIMU;
import bgu.spl.mics.application.objects.LiDarDataBase;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.objects.StampedCloudPoints;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import bgu.spl.mics.application.objects.StatisticalFolder;
import bgu.spl.mics.application.objects.TrackedObject;
import bgu.spl.mics.application.objects.lidarData;
import java.util.concurrent.CountDownLatch;

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
    // Configuration configuration;
    // String configDirectory;
    ArrayList<lidarData> lidarData3Pts;
    ArrayList<StampedCloudPoints> lidarData2Pts;
    ArrayList<Camera> Cameras ;
    ArrayList<LiDarWorkerTracker> Lidars ;
    LiDarDataBase db;
    GPSIMU gps=null;
    ParsingJsonFiles parsingJsonFiles = null;
    CountDownLatch latch;
    Map<String, ArrayList<StampedDetectedObjects>> cameraData=null;
    ArrayList<Pose> PoseData=null;

    try {
        parsingJsonFiles = new ParsingJsonFiles("C:\\Users\\ariks\\uni\\CodingEnviroments\\Work2_Threading\\Skeleton\\example input\\configuration_file.json");
        System.err.println("Done parsing");
    
    
    
    } catch (IOException e) {
        e.printStackTrace();
    }
    Cameras = new ArrayList<>();
    Lidars = new ArrayList<>();

    try {
        cameraData = parsingJsonFiles.parseCameraData();
        lidarData3Pts = parsingJsonFiles.parseLidarData();
        lidarData2Pts=parsingJsonFiles.two_to_three(lidarData3Pts);
        db=LiDarDataBase.getInstance();
        db.initialize(lidarData2Pts);
        PoseData = parsingJsonFiles.parsePoseData();
    } catch (IOException e) {
        e.printStackTrace();
    }
    gps = new GPSIMU();
    ArrayList<String> cameras_keys = new ArrayList<>();
    for (CameraConfiguration config : parsingJsonFiles.configuration.Cameras.CamerasConfigurations) {
        int id = config.id;
        int frequency = config.frequency;
        String key = config.camera_key;

    }
    
    for (LidarConfiguration config : parsingJsonFiles.configuration.LiDarWorkers.LidarConfigurations) {
        int id = config.id;
        int frequency = config.frequency;
        Lidars.add(new LiDarWorkerTracker(id, frequency));
    }
    //sending the keys names to the ErrorInfo
    for(Camera cam : Cameras){ErrorInfo.getInstance().add_cameras_keys_match_frame(cam.getKey());}

    gps.setCurrentTick(0);
    gps.setPoseList(parsingJsonFiles.PoseData);
    
    System.out.println("Done parsing");
    ArrayList<CameraService> CameraServices = new ArrayList<>();  
    ArrayList<LiDarService> LiDarServices = new ArrayList<>();  

    ArrayList<String> cameras_keys = new ArrayList<>();
    for (CameraConfiguration config : parsingJsonFiles.configuration.Cameras.CamerasConfigurations) {
        int id = config.id;
        int frequency = config.frequency;
        String key = config.camera_key;
        Cameras.add(new Camera(id, frequency,key,cameraData.get(key)));

    }
    
    for (LidarConfiguration config : parsingJsonFiles.configuration.LiDarWorkers.LidarConfigurations) {
        int id = config.id;
        int frequency = config.frequency;
        Lidars.add(new LiDarWorkerTracker(id, frequency));
    }
    //sending the keys names to the ErrorInfo
    for(Camera cam : Cameras){ErrorInfo.getInstance().add_cameras_keys_match_frame(cam.getKey());}

    
    latch = new CountDownLatch(Cameras.size()+3+Lidars.size());
    TimeService timeservice = new TimeService(parsingJsonFiles.getConfiguration().TickTime, parsingJsonFiles.getConfiguration().Duration);
    FusionSlam fusionSlam = new FusionSlam();
    FusionSlamService slamservice = new FusionSlamService(fusionSlam);
    
    // PoseService poseservice = new PoseService(gps);
    for (int i = 0; i < Cameras.size(); i++) {
        CameraServices.add(new CameraService(Cameras.get(i)));
    }
    LiDarDataBase.getInstance().getCloudPoints();
    for (int i = 0; i < Lidars.size(); i++) {
        LiDarServices.add(new LiDarService(Lidars.get(i)));
    }
    //need to do count down latch here somewhere
    slamservice.run();
    // poseservice.run();
    for (int i = 0; i < CameraServices.size(); i++) {
        CameraServices.get(i).run();
    }
    for (int i = 0; i < LiDarServices.size(); i++) {
        LiDarServices.get(i).run();
    }
        timeservice.run();

System.err.println("Done running");

    }























//implement countdownlatch so the tick will start only after initiallize of everyone

// public class ErrorInfo {
//     private ArrayList<DetectObjectsEvent> cameras_last_frames; //the last frame of all the cameras
//     private ArrayList<TrackedObjectsEvent> lidars_last_frames; //the last frame of all the Lidars
//     private ArrayList<Pose> poses; //All the robot poses up to the tick where the error occurred.
//     private StatisticalFolder stat;

//TESTING the error output file
        // DetectedObject obj1 = new DetectedObject("obj1", "descr1");
        // DetectedObject obj2 = new DetectedObject("obj2", "descr2");
        // ArrayList<DetectedObject> objects = new ArrayList<DetectedObject>();
        // objects.add(obj1); objects.add(obj2);
        // DetectObjectsEvent det_eve_1 = new DetectObjectsEvent(2, objects);
        // ArrayList<String> cameras_keys = new ArrayList<>();
        // cameras_keys.add("Camera1"); cameras_keys.add("Camera2");
        // DetectedObject obj3 = new DetectedObject("obj3", "descr3");
        // ArrayList<DetectedObject> objects2 = new ArrayList<DetectedObject>();
        // objects2.add(obj3);
        // DetectObjectsEvent det_eve_2 = new DetectObjectsEvent(2, objects2);
        // final ArrayList<DetectObjectsEvent> cameras_last_frames = new ArrayList<>();
        // cameras_last_frames.add(det_eve_1); cameras_last_frames.add(det_eve_2); //camera last frames

        //         ArrayList<CloudPoint> coordinates1 = new ArrayList<>();
        // coordinates1.add(new CloudPoint(1.0, 2.0));
        // coordinates1.add(new CloudPoint(4.0, 5.0));

        // ArrayList<CloudPoint> coordinates2 = new ArrayList<>();
        // coordinates2.add(new CloudPoint(7.0, 8.0));
        // coordinates2.add(new CloudPoint(10.0, 11.0));

        // // Create instances of TrackedObject
        // TrackedObject trackedObject1 = new TrackedObject("Object1", "first tracked object", coordinates1, 2);
        // TrackedObject trackedObject2 = new TrackedObject("Object2", "second tracked object", coordinates2, 2);
        // ArrayList<TrackedObject> tr_eve= new ArrayList<>();
        // final ArrayList<TrackedObjectsEvent> lidars_last_frames = new ArrayList<>();
        // lidars_last_frames.add(new TrackedObjectsEvent(tr_eve, 2));
        // ArrayList<Pose> poses = new ArrayList<>();
        // Pose pose1 = new Pose(10.5, 20.3, 1.57, 100);
        // Pose pose2 = new Pose(15.0, 25.5, 0.78, 100);
        // poses.add(pose1); poses.add(pose2); //poses
        // StatisticalFolder stat = StatisticalFolder.getInstance();
        // stat.incrementDetectedObjects(2);
        // stat.incrementDetectedObjects(4);
        // stat.incrementLandmarks(5);

        // //now create the ErrorInfo instance
        // ErrorInfo err_info = new ErrorInfo();
        // err_info.AddCamerasLastFrames(det_eve_1); err_info.AddCamerasLastFrames(det_eve_2);
        // err_info.AddLidarsLastFrames(new TrackedObjectsEvent(tr_eve, 2));
        // err_info.setPoses(poses);
        // err_info.set_crashed_brod(new CrashedBroadcast("faulty service", "err message"));
        // err_info.add_cameras_keys_match_frame("Camera1");
        // err_info.add_cameras_keys_match_frame("Camera2");
        // err_info.createOutput();
    }

