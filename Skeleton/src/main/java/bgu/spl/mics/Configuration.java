package bgu.spl.mics;
import java.util.List;

public class Configuration {
    public Cameras Cameras;
    public Lidars Lidars;
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

    public static class Lidars {
        public List<LidarConfiguration> LidarConfigurations;
        public String lidars_data_path;
    }

    public static class LidarConfiguration {
        public int id;
        public int frequency;
    }
}