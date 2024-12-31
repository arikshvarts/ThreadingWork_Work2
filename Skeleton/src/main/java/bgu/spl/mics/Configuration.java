package bgu.spl.mics;
import java.util.List;

public class Configuration {
    public Cameras Cameras;
    public LiDarWorkers LiDarWorkers;
    public String poseJsonFile;
    public int TickTime;
    public int Duration;

    public static class Cameras {
        public List<CameraConfiguration> CamerasConfigurations;
        public String camera_datas_path;
    }

    public static class CameraConfiguration {
        public int id;
        public int frequency;
        public String camera_key;
    }

    public static class LiDarWorkers {
        public List<LidarConfiguration> LidarConfigurations;
        public String lidars_data_path;
    }

    public static class LidarConfiguration {
        public int id;
        public int frequency;
    }
}