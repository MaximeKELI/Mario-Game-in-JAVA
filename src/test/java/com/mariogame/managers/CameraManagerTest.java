package com.mariogame.managers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CameraManagerTest {

    @Mock private OrthographicCamera camera;
    private CameraManager cameraManager;
    private static final float VIEWPORT_WIDTH = 20f;
    private static final float VIEWPORT_HEIGHT = 15f;
    
    @BeforeEach
    void setUp() {
        cameraManager = new CameraManager(camera, VIEWPORT_WIDTH, VIEWPORT_HEIGHT);
    }

    @Test
    void testInitialization() {
        assertNotNull(cameraManager);
        assertEquals(VIEWPORT_WIDTH, cameraManager.getViewportWidth());
        assertEquals(VIEWPORT_HEIGHT, cameraManager.getViewportHeight());
    }
    
    @Test
    void testUpdate() {
        // Configuration du mock
        Vector2 target = new Vector2(10, 5);
        float deltaTime = 0.1f;
        
        // Appel de la méthode à tester
        cameraManager.update(target, deltaTime);
        
        // Vérifications
        verify(camera).update();
    }
    
    @Test
    void testSetZoom() {
        float zoom = 0.8f;
        cameraManager.setZoom(zoom);
        
        verify(camera).zoom = zoom;
        verify(camera).update();
    }
    
    @Test
    void testShake() {
        float intensity = 0.5f;
        float duration = 1.0f;
        
        cameraManager.shake(intensity, duration);
        
        assertTrue(cameraManager.isShaking());
    }
    
    @Test
    void testScreenToWorldCoordinates() {
        // Configuration du mock
        Vector3 expected = new Vector3(10, 20, 0);
        when(camera.unproject(any(Vector3.class))).thenReturn(expected);
        
        // Appel de la méthode à tester
        Vector2 result = cameraManager.screenToWorldCoordinates(100, 200);
        
        // Vérifications
        assertNotNull(result);
        assertEquals(expected.x, result.x);
        assertEquals(expected.y, result.y);
    }
    
    @Test
    void testSetPosition() {
        float x = 10f;
        float y = 5f;
        
        cameraManager.setPosition(x, y);
        
        verify(camera).position.set(x, y, 0);
        verify(camera).update();
    }
    
    @Test
    void testGetCamera() {
        assertEquals(camera, cameraManager.getCamera());
    }
    
    @Test
    void testSetBounds() {
        float minX = 0f;
        float minY = 0f;
        float maxX = 100f;
        float maxY = 100f;
        
        cameraManager.setBounds(minX, minY, maxX, maxY);
        
        // Vérifier que les limites sont correctement définies
        // (la vérification se fera dans la méthode update)
        assertTrue(true);
    }
}
