    import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;

public class CameraTest {

    @Test
    public void testHandleTick_ValidEvent() {
        // Arrange
        ArrayList<DetectedObject> detectedObjects = new ArrayList<>();
        detectedObjects.add(new DetectedObject("Rock", "Small"));
        detectedObjects.add(new DetectedObject("Tree", "Tall"));

        ArrayList<StampedDetectedObjects> camData = new ArrayList<>();
        camData.add(new StampedDetectedObjects(5, detectedObjects)); // Adding a detection at time 5

        Camera camera = new Camera(1, 5, "Cam1", camData);

        // Act
        DetectObjectsEvent event = camera.handleTick(10); // Should trigger as 10-5 = 5 matches frequency

        // Assert
        assertNotNull(event, "Event should be created.");
        assertEquals(5, event.getTime(), "Event time should match detection time.");
        assertEquals(2, event.getObjects().size(), "Event should contain 2 detected objects.");
    }

    @Test
    public void testHandleTick_EmptyEvent() {
        // Arrange
        ArrayList<StampedDetectedObjects> camData = new ArrayList<>();  // No detections
        Camera camera = new Camera(1, 5, "Cam1", camData);

        // Act
        DetectObjectsEvent event = camera.handleTick(10);  // No detection data

        // Assert
        assertNotNull(event, "Event should be created even if empty.");
        assertEquals(10, event.getTime(), "Event time should be the current tick.");
        assertTrue(event.getObjects().isEmpty(), "Event should contain no detected objects.");
    }
}

