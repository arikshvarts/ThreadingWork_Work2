package bgu.spl.mics;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import bgu.spl.mics.Configuration;
import bgu.spl.mics.Configuration.CameraConfiguration;
import bgu.spl.mics.Configuration.LidarConfiguration;
import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.CameraData;
import bgu.spl.mics.application.objects.GPSIMU;
import bgu.spl.mics.application.objects.LiDarDataBase;
import bgu.spl.mics.application.objects.lidarData;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.objects.STATUS;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import bgu.spl.mics.application.objects.TrackedObject;
import bgu.spl.mics.application.objects.StampedCloudPoints;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ParsingJsonFiles {
    public Configuration configuration;
    public String configDirectory;
    public Map<String, ArrayList<StampedDetectedObjects>> cameraData;
    public ArrayList<StampedCloudPoints> lidarData;
    public ArrayList<Pose> PoseData;
    List<Camera> Cameras ;
    List<LiDarWorkerTracker> Lidars ;
    LiDarDataBase db;
    GPSIMU gps;

    public ParsingJsonFiles(String configFilePath) throws IOException {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(configFilePath)) {
            this.configuration = gson.fromJson(reader, Configuration.class);
        }
        this.configDirectory = new File(configFilePath).getParent();
        // Initialize cameras
        Cameras = new ArrayList<>();
        Lidars = new ArrayList<>();
        cameraData = parseCameraData();
        lidarData = parseLidarData();
        db=new LiDarDataBase();
        db.initialize(lidarData);
        PoseData = parsePoseData();
        for (CameraConfiguration config : configuration.Cameras.CamerasConfigurations) {
            int id = config.id;
            int frequency = config.frequency;
            String key = config.camera_key;

            Cameras.add(new Camera(id, frequency,key,cameraData.get(key)));
        }
        for (LidarConfiguration config : configuration.LiDarWorkers.LidarConfigurations) {
            int id = config.id;
            int frequency = config.frequency;
            Lidars.add(new LiDarWorkerTracker(id, frequency));
        }
        gps.setCurrentTick(0);
        gps.setPoseList(PoseData);
        System.out.println("Done parsing");


    }

    public Map<String, ArrayList<StampedDetectedObjects>> parseCameraData() throws IOException {
        Gson gson = new Gson();
        String path = configuration.Cameras.camera_datas_path;
        try (FileReader reader = new FileReader(configDirectory+path.substring(1))) {
            Type type = new TypeToken<Map<String, ArrayList<StampedDetectedObjects>>>() {}.getType();
            return gson.fromJson(reader, type);
        }
    }

    public ArrayList<StampedCloudPoints> parseLidarData() throws IOException {
        Gson gson = new Gson();
        String path = configuration.LiDarWorkers.lidars_data_path;

        try (FileReader reader = new FileReader(configDirectory+path.substring(1))) {
            Type type = new TypeToken<ArrayList<StampedCloudPoints>>() {}.getType();
            return gson.fromJson(reader, type);
        }
    }

    public ArrayList<Pose> parsePoseData() throws IOException {
        Gson gson = new Gson();
        String path = configuration.poseJsonFile;
        try (FileReader reader = new FileReader(configDirectory+path.substring(1))) {
            Type type = new TypeToken<ArrayList<Pose>>() {}.getType();
            return gson.fromJson(reader, type);
        }
    }

    // Add getters for the configuration if needed
    public Configuration getConfiguration() {
        return configuration;
    }
    public static void main(String[] args) {
        try {
            ParsingJsonFiles parsingJsonFiles = new ParsingJsonFiles("C:\\Users\\ariks\\uni\\CodingEnviroments\\Work2_Threading\\Skeleton\\example input\\configuration_file.json");
            System.err.println("Done parsing");
       
       
       
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        System.out.println("Done parsing");
    }
}