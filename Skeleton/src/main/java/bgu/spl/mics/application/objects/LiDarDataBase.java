package bgu.spl.mics.application.objects;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * LiDarDataBase is a singleton class responsible for managing LiDAR data.
 * It provides access to cloud point data and other relevant information for tracked objects.
 */
public class LiDarDataBase {
    private static LiDarDataBase instance;
    private  ArrayList<StampedCloudPoints> cloudPoints;
    //each key in this map is time and value is list of all StampedCloudPoints with the key time
    private static ConcurrentHashMap<Integer, ArrayList<StampedCloudPoints>> map_time_cloudP = new ConcurrentHashMap<>();
    

    public void initialize(ArrayList<StampedCloudPoints> data) {
        cloudPoints = data;
                for (StampedCloudPoints point : cloudPoints) {
            //updating all the values to our map
            map_time_cloudP.computeIfAbsent(point.getTime(), k -> new ArrayList<>()).add(point);
        }    }
    
        public LiDarDataBase() {
cloudPoints=null;

    }
    private static class LidarDataBaseHelper {
        private static final LiDarDataBase INSTANCE = new LiDarDataBase();
    }

    public static LiDarDataBase getInstance() {
        return LidarDataBaseHelper.INSTANCE;
    }
    // private synchronized void  loadData(String filePath) {
    //     Gson gson = new Gson();
    //     try (FileReader reader = new FileReader(filePath)) {
    //         Type listType = new TypeToken<ArrayList<StampedCloudPoints>>() {}.getType();
    //         ArrayList<StampedCloudPoints> parsedData = gson.fromJson(reader, listType);
    //         cloudPoints.addAll(parsedData);
    //     } catch (IOException e) {
    //         System.err.println("Failed to load LiDAR data: " + e.getMessage());
    //     }
    //     for (StampedCloudPoints point : cloudPoints) {
    //         //updating all the values to our map
    //         map_time_cloudP.computeIfAbsent(point.getTime(), k -> new ArrayList<>()).add(point);
    //     }
    // }

    public static ConcurrentHashMap<Integer, ArrayList<StampedCloudPoints>> getMapTimeHashMap() {
        return map_time_cloudP;
    }

      public ArrayList<StampedCloudPoints> getCloudPoints() {
        return cloudPoints;
    }
    /**
     * Returns the singleton instance of LiDarDataBase.
     *
     * @param filePath The path to the LiDAR data file.
     * @return The singleton instance of LiDarDataBase.
     */
}
