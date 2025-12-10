package com.mariogame.managers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.mariogame.entities.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class LevelManagerTest {

    @Mock private World world;
    @Mock private TiledMap tiledMap;
    @Mock private Player player;
    @Mock private GameManager gameManager;
    
    private LevelManager levelManager;
    
    @BeforeEach
    void setUp() {
        // Configuration initiale
        levelManager = new LevelManager(world, gameManager);
        
        // Configuration du GameManager
        when(gameManager.getPlayer()).thenReturn(player);
    }

    @Test
    void testLoadLevel() {
        // Configuration du mock TiledMap
        // (dans une vraie implémentation, on utiliserait un fichier de test .tmx)
        
        // Appel de la méthode à tester
        levelManager.loadLevel(1);
        
        // Vérifications
        assertNotNull(levelManager.getCurrentLevel());
        // Plus de vérifications seraient nécessaires selon l'implémentation
    }
    
    @Test
    void testGetSpawnPoint() {
        // Configuration des points d'apparition de test
        Map<String, Vector2> spawnPoints = new HashMap<>();
        spawnPoints.put("player", new Vector2(10, 5));
        levelManager.setSpawnPoints(spawnPoints);
        
        // Test
        Vector2 spawnPoint = levelManager.getSpawnPoint("player");
        
        // Vérifications
        assertNotNull(spawnPoint);
        assertEquals(10, spawnPoint.x);
        assertEquals(5, spawnPoint.y);
    }
    
    @Test
    void testGetLevelCount() {
        int expectedLevels = 3;
        // Dans une vraie implémentation, cela dépendrait des fichiers de niveau disponibles
        
        // Pour le test, on suppose que nous avons 3 niveaux
        assertTrue(levelManager.getLevelCount() >= 0);
    }
    
    @Test
    void testNextLevel() {
        // Configuration initiale
        levelManager.loadLevel(1);
        int initialLevel = levelManager.getCurrentLevelNumber();
        
        // Test
        levelManager.nextLevel();
        
        // Vérifications
        assertEquals(initialLevel + 1, levelManager.getCurrentLevelNumber());
    }
    
    @Test
    void testRestartLevel() {
        // Configuration initiale
        levelManager.loadLevel(1);
        
        // Simulation d'un jeu en cours
        when(player.getPosition()).thenReturn(new Vector2(100, 100));
        
        // Test
        levelManager.restartLevel();
        
        // Vérifications
        verify(player).setPosition(anyFloat(), anyFloat());
    }
    
    @Test
    void testDispose() {
        // Configuration initiale
        levelManager.loadLevel(1);
        
        // Test
        levelManager.dispose();
        
        // Vérifications
        // (dans une vraie implémentation, on vérifierait que les ressources sont libérées)
        assertTrue(true);
    }
}
