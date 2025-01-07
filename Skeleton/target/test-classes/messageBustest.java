package bgu.spl.mics.application.tests;

import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;

// import static org.junit.jupiter.api.Assertions.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import org.junit.Test;


import bgu.spl.mics.application.objects.Camera;

import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import bgu.spl.mics.application.services.CameraService;
import bgu.spl.mics.application.services.LiDarService;

import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;

import org.junit.Before;

public class messageBustest {
    public MessageBusImpl messageBus;
    public MicroService microService1;
    public MicroService microService2;
    public CountDownLatch countDownLatch;
    public Camera camera1;
    public LiDarWorkerTracker lidar1;
    public CameraService cameraSer1;
    public LiDarService lidarSer1;

    @Before
    void setUp() {
        messageBus = MessageBusImpl.getInstance();
        countDownLatch = new CountDownLatch(5);
        camera1 = new Camera(1,0,"camera1",new ArrayList<StampedDetectedObjects>());
        lidar1 = new LiDarWorkerTracker(1,0);

        cameraSer1= new CameraService(camera1,countDownLatch);
        messageBus = MessageBusImpl.getInstance();
        // cameraSer2 = new CameraService(camera1,countDownLatch);
        lidarSer1 = new LiDarService(lidar1, countDownLatch);
        // messageBus.register(microService1);
        // messageBus.register(microService2);
    }

    
    @Test
    void testnRegister() {
        assertFalse(messageBus.isRegistered(cameraSer1));
        messageBus.register(cameraSer1);
        assertTrue(messageBus.isRegistered(cameraSer1));

    }

    @Test
    void testunRegister() {
        messageBus.register(lidarSer1);
        assertTrue(messageBus.isRegistered(lidarSer1));
        messageBus.register(lidarSer1);
        assertFalse(messageBus.isRegistered(lidarSer1));

    }

    @Test
    void testSendBroadcast() {

        messageBus.register(cameraSer1);
        messageBus.register(lidarSer1);

        // Assert no microservices subscribed yet
        assertEquals("No microservices subscribed yet.",0, messageBus.getnumListeners(TickBroadcast.class));

        // Subscribe microService1 to TickBroadcast
        messageBus.subscribeBroadcast(TickBroadcast.class, cameraSer1);
        // Subscribe microService2 to TerminateBroadcast
        messageBus.subscribeBroadcast(TerminatedBroadcast.class, lidarSer1);

        // Assert only microService1 is subscribed to TickBroadcast
        assertEquals("Only cameraSer1 subscribes to TickBroadcast.", 1, messageBus.getnumListeners(TickBroadcast.class));

        // Send a TickBroadcast
        messageBus.sendBroadcast(new TickBroadcast(1));

        // Verify that microService1 received the TickBroadcast
        assertEquals("cameraSer1 got a TickBroadcast.",1, messageBus.getMicroServices_Queues(cameraSer1).size());
        assertEquals( "lidarSer1 got no messages yet.",0, messageBus.getMicroServices_Queues(lidarSer1).size());

        // Send two TerminateBroadcasts
        messageBus.sendBroadcast(new TerminatedBroadcast("cameraSer1"));
        messageBus.sendBroadcast(new TerminatedBroadcast("lidarSer1"));

        // Verify that microService2 received both TerminateBroadcasts
        assertEquals("cameraSer1 got no other messages except first tick.",1, messageBus.getMicroServices_Queues(cameraSer1).size());
        assertEquals("lidarSer1 got 2 TerminateBroadcasts.",2, messageBus.getMicroServices_Queues(lidarSer1).size());
    }
}

