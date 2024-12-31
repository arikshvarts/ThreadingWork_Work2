package bgu.spl.mics.application.objects;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Represents the robot's pose (position and orientation) in the environment.
 * Includes x, y coordinates and the yaw angle relative to a global coordinate system.
 */
public class Pose {
    private final double x;
    private final double y;
    private final double yaw;
    private final int time;

    public Pose(double x, double y, double yaw, int time) {
        this.x = x;
        this.y = y;
        this.yaw = yaw;
        this.time = time;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getYaw() {
        return yaw;
    }

    public int getTime() {
        return time;
    }

    @Override
    public String toString() {
    JsonObject poseJson = new JsonObject();
    poseJson.addProperty("time", time);
    poseJson.addProperty("x", x);
    poseJson.addProperty("y", y);
    poseJson.addProperty("yaw", yaw);
    
    return new Gson().toJson(poseJson);
    }
}