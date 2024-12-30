package bgu.spl.mics.application.objects;

import java.util.concurrent.atomic.AtomicInteger;

/**
     * Holds statistical information about the system's operation.
     * This class aggregates metrics such as the runtime of the system,
     * the number of objects detected and tracked, and the number of landmarks identified.
     */
    public class StatisticalFolder {
        private int systemRuntime;
        private AtomicInteger numDetectedObjects;
        private AtomicInteger numTrackedObjects;
        private AtomicInteger numLandmarks;
        
    // Singleton instance    
        private static class StatisticalFolderHelper {
            private static final StatisticalFolder INSTANCE = new StatisticalFolder();
        }

        public static StatisticalFolder getInstance() {
            return StatisticalFolderHelper.INSTANCE;
        }

        public void updateRuntime(int ticks) {//synchronized maybe?
            this.systemRuntime = ticks;
        }
    
        public void incrementDetectedObjects(int add) {
            this.numDetectedObjects.addAndGet(add);
        }
    
        public void incrementTrackedObjects(int add) {
            this.numTrackedObjects.addAndGet(add);
        }
    
        public void incrementLandmarks(int add) {
            this.numLandmarks.addAndGet(add);
        }
    
        // Getters
        public int getSystemRuntime() {
            return systemRuntime++;
        }
    
        public AtomicInteger getNumDetectedObjects() {
            return numDetectedObjects;
        }
    
        public AtomicInteger getNumTrackedObjects() {
            return numTrackedObjects;
        }
    
        public AtomicInteger getNumLandmarks() {
            return numLandmarks;
        }
    }
    