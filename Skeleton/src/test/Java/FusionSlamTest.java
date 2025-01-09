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
import bgu.spl.mics.application.objects.FusionSlam;
import bgu.spl.mics.application.objects.LandMark;
import bgu.spl.mics.application.objects.CloudPoint;
import bgu.spl.mics.application.objects.TrackedObject;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;

import org.junit.Before;

public class FusionSlamtest {

    private FusionSlam fusionSlam;
    private Pose testPose;
    private TrackedObject testObject;

    @Before
    public void setUp() {
        fusionSlam = FusionSlam.getInstance();
        testPose = new Pose(5.0, 10.0, 30.0, 1); 
        fusionSlam.InsertPose(testPose);
        
        ArrayList<CloudPoint> localPoints = new ArrayList<>();
        localPoints.add(new CloudPoint(2.0, 3.0));
        testObject = new TrackedObject("obj1", "Test Object", localPoints, 1);
    }


////method under test: translateCoordinateSys

//The pre conditions, post conditions and invariant are in the FusionSlam class above translateCoordinateSys method

//this 2 invariants kept during the tests

// The `poses` and `trackedObject` data must remain unchanged after this method executes.
// The returned list of global coordinates, if not null, should match the size of the input coordinate list.
    @Test
    public void testTranslateCoordinateSys() {
        ArrayList<CloudPoint> globalPoints = fusionSlam.translateCoordinateSys(testObject);

        assertNotNull("Translation should produce a valid global coordinate list", globalPoints);
        assertEquals("The number of transformed points should match the input", 1, globalPoints.size());

        CloudPoint transformedPoint = globalPoints.get(0);
        double expectedX = 5.232;
        double expectedY = 13.598;

        assertEquals(expectedX, transformedPoint.getX(), 0.1);
        assertEquals(expectedY, transformedPoint.getY(), 0.1);
    }


//method under test: addOrUpdateLandMark

//The pre conditions, post conditions and invariant are in the FusionSlam class above translateCoordinateSys method

//this 3 invariants are kept during the tests

// The `landmarks` list must not contain duplicate landmarks with the same ID.
// The size of the `landmarks` list should only increase when a new landmark is added.
// The `waitingObjs` list should only grow if pose data is unavailable for the tracked object.
    @Test
    public void testAddOrUpdateLandMark() {
        fusionSlam.addOrUpdateLandMark(testObject);
        assertFalse("Landmark should be added successfully", fusionSlam.getLandMarks().isEmpty());

        LandMark addedLandMark = fusionSlam.getLandMarks().get(0);
        assertEquals("obj1", addedLandMark.getId(), "Landmark ID should match");
        assertEquals("Test Object", addedLandMark.getDescription(), "Landmark description should match");
    }
}
