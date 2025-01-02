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
import bgu.spl.mics.application.objects.CloudPoint;

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
    public ArrayList<lidarData> lidarData3Pts;
    public ArrayList<StampedCloudPoints> lidarData2Pts;


    public ParsingJsonFiles(String configFilePath) throws IOException {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(configFilePath)) {
            this.configuration = gson.fromJson(reader, Configuration.class);
        }
        this.configDirectory = new File(configFilePath).getParent();
        // Initialize cameras
    }

    public Map<String, ArrayList<StampedDetectedObjects>> parseCameraData() throws IOException {
        Gson gson = new Gson();
        String path = configuration.Cameras.camera_datas_path;
        try (FileReader reader = new FileReader(configDirectory+path.substring(1))) {
            Type type = new TypeToken<Map<String, ArrayList<StampedDetectedObjects>>>() {}.getType();
            return gson.fromJson(reader, type);
        }
    }
    public static int num_of_cameras(){ return Cameras.size();}


    public ArrayList<lidarData> parseLidarData() throws IOException {
        Gson gson = new Gson();
        String path = configuration.LiDarWorkers.lidars_data_path;

        try (FileReader reader = new FileReader(configDirectory+path.substring(1))) {
            Type type = new TypeToken<ArrayList<lidarData>>() {}.getType();
            return gson.fromJson(reader, type);
        }
    }

    public static int num_of_lidars(){ return Lidars.size();}

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
public ArrayList<StampedCloudPoints> two_to_three(ArrayList<lidarData> lidarData3Pts){
    String id="";
    int time=0;
    ArrayList<StampedCloudPoints> lidarData2Pts = new ArrayList<>();
    ArrayList<CloudPoint> cloudPoints = new ArrayList<>();
    for(lidarData data : lidarData3Pts){
        id=data.getId();
        time=data.getTime();
        for (ArrayList<Double> ls : data.get3pts()){
            cloudPoints.add(new CloudPoint(ls.get(0),ls.get(1)));
        }
        lidarData2Pts.add(new StampedCloudPoints(data.getId(),data.getTime(),cloudPoints));

    }
    return lidarData2Pts;   
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