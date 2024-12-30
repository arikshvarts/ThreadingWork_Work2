package bgu.spl.mics;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import bgu.spl.mics.Configuration.Lidars;
import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import bgu.spl.mics.application.objects.Pose;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class ParsingJsonFiles {
    private Configuration configuration;

    public ParsingJsonFiles(String configFilePath) throws IOException {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(configFilePath)) {
            this.configuration = gson.fromJson(reader, Configuration.class);
        }
    }

    public Map<String, List<List<Camera>>> parseCameraData() throws IOException {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(configuration.Cameras.camera_datas_path)) {
            Type type = new TypeToken<Map<String, List<List<Camera>>>>() {}.getType();
            return gson.fromJson(reader, type);
        }
    }

    public List<LiDarWorkerTracker> parseLidarData() throws IOException {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(configuration.Lidars.lidars_data_path)) {
            Type type = new TypeToken<List<LiDarWorkerTracker>>() {}.getType();
            return gson.fromJson(reader, type);
        }
    }

    public List<Pose> parsePoseData() throws IOException {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(configuration.poseJsonFile)) {
            Type type = new TypeToken<List<Pose>>() {}.getType();
            return gson.fromJson(reader, type);
        }
    }

    // Add getters for the configuration if needed
    public Configuration getConfiguration() {
        return configuration;
    }
    public static void main(String[] args) {
        try {
            ParsingJsonFiles parsingJsonFiles = new ParsingJsonFiles("C:\\Users\\ariks\\uni\\CodingEnviroments\\Work2_Threading\\Skeleton\\example_input_2\\configuration_file.json");
            System.out.println(parsingJsonFiles.parseCameraData());
            System.out.println(parsingJsonFiles.parseLidarData());
            System.out.println(parsingJsonFiles.parsePoseData());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}