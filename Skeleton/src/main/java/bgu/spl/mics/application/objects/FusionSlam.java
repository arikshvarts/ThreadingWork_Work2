package bgu.spl.mics.application.objects;

/**
 * Manages the fusion of sensor data for simultaneous localization and mapping (SLAM).
 * Combines data from multiple sensors (e.g., LiDAR, camera) to build and update a global map.
 * Implements the Singleton pattern to ensure a single instance of FusionSlam exists.
 */
public class FusionSlam {
    private double x;
    private double y;
    private double yaw;
    private double robot_coord_x;
    private double robot_coord_y;
    private double robot_coord_yaw;
    private TrackedObjectsEvent trackedObjectsEvent;

    // Singleton instance holder
    private static class FusionSlamHolder {
        private static final FusionSlam instance = new FusionSlam();
    }
    public FusionSlam(LandMark lMark,Pose pose) {
        this.x=lMark;
        this.y=y;
        this.yaw=yaw;
        robot_coord_x=pose.getX();
        robot_coord_y=pose.getY();
        robot_coord_yaw=pose.getYaw();
        trackedObjectsEvent= new TrackedObjectsEvent();


    }
    public static FusionSlam getInstance() {
        return FusionSlamHolder.instance;
    }
    public Pose translateCoordinateSys() {
        double yaw_rad=yaw*Math.PI/180;
        double x_global=x*Math.cos(yaw_rad)-y*Math.sin(yaw_rad)+robot_coord_x;
        double y_global=x*Math.sin(yaw_rad)+y*Math.cos(yaw_rad)+robot_coord_y;
        return new Pose(x_global,y_global,yaw+robot_coord_yaw);
    }
}
    
