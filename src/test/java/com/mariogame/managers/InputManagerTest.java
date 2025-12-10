package com.mariogame.managers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;
import com.mariogame.core.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InputManagerTest {

    @Mock private Player player;
    @Mock private Input input;
    
    private InputManager inputManager;
    
    @BeforeEach
    void setUp() {
        inputManager = new InputManager(player);
        inputManager.setInputProcessor(input);
    }

    @Test
    void testKeyDown_MoveRight() {
        // Test de la touche droite
        boolean processed = inputManager.keyDown(Input.Keys.RIGHT);
        
        assertTrue(processed);
        verify(player).setMovingRight(true);
    }
    
    @Test
    void testKeyDown_MoveLeft() {
        // Test de la touche gauche
        boolean processed = inputManager.keyDown(Input.Keys.LEFT);
        
        assertTrue(processed);
        verify(player).setMovingLeft(true);
    }
    
    @Test
    void testKeyDown_Jump() {
        // Test de la touche espace pour sauter
        boolean processed = inputManager.keyDown(Input.Keys.SPACE);
        
        assertTrue(processed);
        verify(player).jump();
    }
    
    @Test
    void testKeyUp_StopMovingRight() {
        // Test du relâchement de la touche droite
        boolean processed = inputManager.keyUp(Input.Keys.RIGHT);
        
        assertTrue(processed);
        verify(player).setMovingRight(false);
    }
    
    @Test
    void testKeyUp_StopMovingLeft() {
        // Test du relâchement de la touche gauche
        boolean processed = inputManager.keyUp(Input.Keys.LEFT);
        
        assertTrue(processed);
        verify(player).setMovingLeft(false);
    }
    
    @Test
    void testTouchDown() {
        // Test d'un appui sur l'écran (pour mobile)
        boolean processed = inputManager.touchDown(100, 200, 0, 0);
        
        assertTrue(processed);
        // Vérifier que la position du saut a été enregistrée
        assertEquals(100, inputManager.getJumpTouchX());
        assertEquals(200, inputManager.getJumpTouchY());
    }
    
    @Test
    void testTouchUp_JumpZone() {
        // Configuration initiale
        inputManager.touchDown(100, 200, 0, 0); // Appui initial
        
        // Relâchement dans la zone de saut (côté droit de l'écran)
        boolean processed = inputManager.touchUp(900, 200, 0, 0);
        
        assertTrue(processed);
        verify(player).jump();
    }
    
    @Test
    void testUpdate() {
        // Configuration des touches actives
        when(input.isKeyPressed(Input.Keys.RIGHT)).thenReturn(true);
        when(input.isKeyPressed(Input.Keys.LEFT)).thenReturn(false);
        when(input.isKeyPressed(Input.Keys.SPACE)).thenReturn(false);
        
        // Mise à jour de l'InputManager
        inputManager.update(0.1f);
        
        // Vérification que le joueur se déplace vers la droite
        verify(player).setMovingRight(true);
        verify(player).setMovingLeft(false);
    }
    
    @Test
    void testSetInputProcessor() {
        // Vérification que le processeur d'entrée est bien défini
        InputProcessor processor = mock(InputProcessor.class);
        inputManager.setInputProcessor(processor);
        
        // Vérification via les appels aux méthodes de test
        inputManager.keyDown(Input.Keys.SPACE);
        verify(player).jump();
    }
    
    @Test
    void testClear() {
        // Configuration initiale
        inputManager.keyDown(Input.Keys.RIGHT);
        
        // Appel de la méthode clear
        inputManager.clear();
        
        // Vérification que les mouvements sont réinitialisés
        verify(player).setMovingRight(false);
        verify(player).setMovingLeft(false);
    }
}
