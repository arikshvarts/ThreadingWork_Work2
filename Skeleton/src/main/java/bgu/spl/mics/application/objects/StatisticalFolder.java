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
        
    
        public synchronized void updateRuntime(int ticks) {
            this.systemRuntime = ticks;
        }
    
        public synchronized void incrementDetectedObjects() {
            this.numDetectedObjects.incrementAndGet();
        }
    
        public synchronized void incrementTrackedObjects() {
            this.numTrackedObjects.incrementAndGet();
        }
    
        public synchronized void incrementLandmarks() {
            this.numLandmarks.incrementAndGet();
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
    