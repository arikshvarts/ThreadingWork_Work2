package bgu.spl.mics.application.objects;

import java.util.concurrent.atomic.AtomicInteger;

public class ServiceCounter {
        private AtomicInteger numThreads;

        private ServiceCounter() {
            numThreads=new AtomicInteger(0);

        }
        
    // Singleton instance    
        private static class ServiceCounterHelper {
            private static final ServiceCounter INSTANCE = new ServiceCounter();
        }

        public static ServiceCounter getInstance() {
            return ServiceCounterHelper.INSTANCE;
        }

        public void incrementThreads() {
            this.numThreads.incrementAndGet();        }

            public void decrementThreads() {
                this.numThreads.decrementAndGet();        }
            public int  getNumThreads() {

                return numThreads.get();
            }
}
