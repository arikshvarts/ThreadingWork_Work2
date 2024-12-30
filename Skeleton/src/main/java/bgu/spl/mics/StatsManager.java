package bgu.spl.mics;

import java.util.HashSet;

import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.StatisticalFolder;

//we are making a singelton instance of StatisticalFolder to mannage 
public class StatsManager {

    private static StatisticalFolder statsFolder;
    private static HashSet<DetectedObject> all_objects_detected = new HashSet<DetectedObject>(); //a collection of all the object were detected till now (no duplicates, implement of set)

    public static void initialize() {
        if (statsFolder == null) {
            statsFolder = new StatisticalFolder();
        }
    }

    public static StatisticalFolder getStatsFolder() {
        return statsFolder;
    }

    public static HashSet<DetectedObject> getAllObjects() {
        return all_objects_detected;
    }
}
