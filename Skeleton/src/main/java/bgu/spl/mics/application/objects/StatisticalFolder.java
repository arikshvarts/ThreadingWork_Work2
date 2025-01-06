package bgu.spl.mics.application.objects;

import java.util.concurrent.atomic.AtomicInteger;

/**
     * Holds statistical information about the system's operation.
     * This class aggregates metrics such as the runtime of the system,
     * the number of objects detected and tracked, and the number of landmarks identified.
     */
    public class StatisticalFolder {
        public AtomicInteger systemRuntime;
        public AtomicInteger numDetectedObjects;
        public AtomicInteger numTrackedObjects;
        public AtomicInteger numLandmarks;
        public AtomicInteger numSensors;



        private StatisticalFolder() {
            systemRuntime=new AtomicInteger(0);
            numDetectedObjects=new AtomicInteger(0);
            numTrackedObjects=new AtomicInteger(0);
            numLandmarks=new AtomicInteger(0);
            numSensors=new AtomicInteger(0);
            
        }
        
    // Singleton instance    
        private static class StatisticalFolderHelper {
            private static final StatisticalFolder INSTANCE = new StatisticalFolder();
        }

        public static StatisticalFolder getInstance() {
            return StatisticalFolderHelper.INSTANCE;
        }

        public void updateRuntime(int add) {//synchronized maybe?
            this.systemRuntime.addAndGet(add);        }
        //     public void updateRuntime(int add) {//synchronized maybe?
        //     int oldVal;
        //     int newVal;
        //     do{
        //         oldVal = systemRuntime.get();
        //         newVal = oldVal + 1;
        //     }
        //     while(!systemRuntime.compareAndSet(oldVal, newVal));
        // }
    
        public void incrementDetectedObjects(int add) {
            this.numDetectedObjects.addAndGet(add);
        }
    
        public void incrementTrackedObjects(int add) {
            this.numTrackedObjects.addAndGet(add);
        }
    
        public void incrementLandmarks(int add) {
            this.numLandmarks.addAndGet(add);
        }

        public void incrementNumSensors() {
            numSensors.addAndGet(1);
            System.out.println("num of sensorsssssssssssssssssssssssssssssssssssssssssssssss  "+ numSensors);
        }
        // public void incrementNumSensors() {
        //     int oldVal;
        //     int newVal;
        //     do{
        //         oldVal = numSensors.get();
        //         newVal = oldVal + 1;
        //     }
        //     while(!numSensors.compareAndSet(oldVal, newVal));
        // }
    
        // public void decrementNumSensors() {
        //     this.numLandmarks.addAndGet(-1);
        // }
        public void decrementNumSensors() {
            // int oldVal;
            // int newVal;
            // do{
            //     oldVal = numSensors.get();
            //     newVal = oldVal -1;
            //     System.out.println("num of sensorsssssssssssssssssssssssssssssssssssssssssssssss  "+ newVal );

            // }
            // while(!numSensors.compareAndSet(oldVal, newVal));
            numSensors.decrementAndGet();
            System.out.println("num of sensorsssssssssssssssssssssssssssssssssssssssssssssss  "+ numSensors);

        }
    
        // Getters
        public AtomicInteger getNumSensors() {
            return numSensors;
        }

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
        public void setSystemRuntime(AtomicInteger systemRuntime) {
            this.systemRuntime = systemRuntime;
        }
    }
    