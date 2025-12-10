package com.mariogame.core;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PlayerTest {

    @Mock private World world;
    @Mock private Body body;
    @Mock private Fixture fixture;
    @Mock private TextureAtlas atlas;
    @Mock private Sound jumpSound, coinSound, powerupSound, deathSound;
    
    private Player player;
    
    @BeforeEach
    void setUp() {
        // Configuration du mock World
        when(world.createBody(any())).thenReturn(body);
        when(body.createFixture(any())).thenReturn(fixture);
        
        // Création d'une instance de Player pour les tests
        player = new Player(world, 0, 0);
        
        // Injection des dépendances mockées
        player.setSounds(jumpSound, coinSound, powerupSound, deathSound);
    }

    @Test
    void testInitialState() {
        assertEquals(3, player.getLives());
        assertEquals(0, player.getCoins());
        assertEquals(0, player.getScore());
        assertTrue(player.isFacingRight());
        assertEquals(Player.State.IDLE, player.getState());
    }
    
    @Test
    void testCollectCoin() {
        int initialCoins = player.getCoins();
        float initialScore = player.getScore();
        
        player.collectCoin();
        
        assertEquals(initialCoins + 1, player.getCoins());
        assertTrue(player.getScore() > initialScore);
        verify(coinSound).play(0.5f);
    }
    
    @Test
    void testTakeDamage() {
        int initialLives = player.getLives();
        
        player.takeDamage();
        
        assertEquals(initialLives - 1, player.getLives());
        assertTrue(player.isInvincible());
        verify(deathSound).play(0.5f);
        
        // Vérifier l'effet de recul
        verify(body).setLinearVelocity(anyFloat(), anyFloat());
    }
    
    @Test
    void testJump() {
        // Simuler que le joueur est au sol
        when(fixture.getUserData()).thenReturn("ground");
        
        player.jump();
        
        verify(body).applyLinearImpulse(
            eq(new Vector2(0, Player.JUMP_FORCE)), 
            any(Vector2.class), 
            eq(true)
        );
        verify(jumpSound).play(0.5f);
    }
    
    @Test
    void testMoveRight() {
        player.moveRight();
        
        assertTrue(player.isFacingRight());
        verify(body).applyForce(
            eq(new Vector2(Player.SPEED, 0)), 
            any(Vector2.class), 
            eq(true)
        );
    }
    
    @Test
    void testMoveLeft() {
        player.moveLeft();
        
        assertFalse(player.isFacingRight());
        verify(body).applyForce(
            eq(new Vector2(-Player.SPEED, 0)), 
            any(Vector2.class), 
            eq(true)
        );
    }
    
    @Test
    void testUpdateInvincibility() {
        player.takeDamage(); // Rend le joueur invincible
        float initialTime = player.getInvincibilityTimer();
        
        player.update(0.5f); // Mise à jour de 0.5 secondes
        
        assertTrue(player.getInvincibilityTimer() < initialTime);
        assertTrue(player.isInvincible());
    }
    
    @Test
    void testDispose() {
        // Simuler l'initialisation de l'atlas
        player.setAtlas(atlas);
        
        player.dispose();
        
        verify(atlas).dispose();
        verify(jumpSound).dispose();
        verify(coinSound).dispose();
        verify(powerupSound).dispose();
        verify(deathSound).dispose();
    }
}
