    package bgu.spl.mics.application.objects;

    /**
     * Holds statistical information about the system's operation.
     * This class aggregates metrics such as the runtime of the system,
     * the number of objects detected and tracked, and the number of landmarks identified.
     */
    public class StatisticalFolder {
        private int systemRuntime;
        private int numDetectedObjects;
        private int numTrackedObjects;
        private int numLandmarks;
    
        public synchronized void updateRuntime(int ticks) {
            this.systemRuntime = ticks;
        }
    
        public synchronized void incrementDetectedObjects() {
            this.numDetectedObjects++;
        }
    
        public synchronized void incrementTrackedObjects() {
            this.numTrackedObjects++;
        }
    
        public synchronized void incrementLandmarks() {
            this.numLandmarks++;
        }
    
        // Getters
        public int getSystemRuntime() {
            return systemRuntime;
        }
    
        public int getNumDetectedObjects() {
            return numDetectedObjects;
        }
    
        public int getNumTrackedObjects() {
            return numTrackedObjects;
        }
    
        public int getNumLandmarks() {
            return numLandmarks;
        }
    }
    