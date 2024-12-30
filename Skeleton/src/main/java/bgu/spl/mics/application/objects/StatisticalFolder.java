package bgu.spl.mics.application.objects;

import java.util.concurrent.atomic.AtomicInteger;

/**
     * Holds statistical information about the system's operation.
     * This class aggregates metrics such as the runtime of the system,
     * the number of objects detected and tracked, and the number of landmarks identified.
     */
    public class StatisticalFolder {
        private AtomicInteger systemRuntime=new AtomicInteger(0);
        private AtomicInteger numDetectedObjects=new AtomicInteger(0);
        private AtomicInteger numTrackedObjects=new AtomicInteger(0);
        private AtomicInteger numLandmarks=new AtomicInteger(0);
        
    // Singleton instance    
        private static class StatisticalFolderHelper {
            private static final StatisticalFolder INSTANCE = new StatisticalFolder();
        }

        public static StatisticalFolder getInstance() {
            return StatisticalFolderHelper.INSTANCE;
        }

        public void updateRuntime(int add) {//synchronized maybe?
            this.systemRuntime.addAndGet(add);        }
    
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
        public AtomicInteger getSystemRuntime() {
            return systemRuntime;
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
    