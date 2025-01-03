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
    private  ArrayList<StampedCloudPoints> cloudPoints;
    //each key in this map is time and value is list of all StampedCloudPoints with the key time
    private static ConcurrentHashMap<Integer, ArrayList<StampedCloudPoints>> map_time_cloudP = new ConcurrentHashMap<>();
    

    public void initialize(ArrayList<StampedCloudPoints> data) {
        cloudPoints = data;
        for (StampedCloudPoints obj : cloudPoints) {
            //updating all the values to our map
            // map_time_cloudP.computeIfAbsent(point.getTime(), k -> new ArrayList<>()).add(point);
            if(map_time_cloudP.get(obj.getTime()) == null){
                map_time_cloudP.put(obj.getTime(), new ArrayList<StampedCloudPoints>());
            }
            map_time_cloudP.get(obj.getTime()).add(obj);
        }    
    }
    
        public LiDarDataBase() {
cloudPoints=null;

    }
    private static class LidarDataBaseHelper {
        private static final LiDarDataBase INSTANCE = new LiDarDataBase();
    }

    public static LiDarDataBase getInstance() {
        return LidarDataBaseHelper.INSTANCE;
    }


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
