package com.mariogame.managers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.mariogame.core.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GameManagerTest {

    @Mock private AssetManager assetManager;
    @Mock private TextureAtlas atlas;
    @Mock private Music backgroundMusic;
    @Mock private Sound jumpSound, coinSound, powerupSound, deathSound;
    
    private GameManager gameManager;
    
    @BeforeEach
    void setUp() {
        // Configuration des mocks
        when(assetManager.get("textures/player.pack", TextureAtlas.class)).thenReturn(atlas);
        when(assetManager.get("audio/music/background.ogg", Music.class)).thenReturn(backgroundMusic);
        when(assetManager.get("audio/sounds/jump.wav", Sound.class)).thenReturn(jumpSound);
        when(assetManager.get("audio/sounds/coin.wav", Sound.class)).thenReturn(coinSound);
        when(assetManager.get("audio/sounds/powerup.wav", Sound.class)).thenReturn(powerupSound);
        when(assetManager.get("audio/sounds/death.wav", Sound.class)).thenReturn(deathSound);
        
        // Initialisation du GameManager
        gameManager = GameManager.getInstance();
        gameManager.setAssetManager(assetManager);
    }

    @Test
    void testLoadAssets() {
        gameManager.loadAssets();
        
        // Vérifier que les assets sont chargés
        verify(assetManager).load("textures/player.pack", TextureAtlas.class);
        verify(assetManager).load("audio/music/background.ogg", Music.class);
        verify(assetManager).load("audio/sounds/jump.wav", Sound.class);
        verify(assetManager).load("audio/sounds/coin.wav", Sound.class);
        verify(assetManager).load("audio/sounds/powerup.wav", Sound.class);
        verify(assetManager).load("audio/sounds/death.wav", Sound.class);
    }
    
    @Test
    void testGetPlayer() {
        // Créer un joueur de test
        Player player = gameManager.createPlayer(null, 0, 0);
        
        // Vérifier que le joueur est correctement configuré
        assertNotNull(player);
        assertEquals(3, player.getLives());
    }
    
    @Test
    void testPlayBackgroundMusic() {
        // Activer la musique de fond
        gameManager.playBackgroundMusic();
        
        // Vérifier que la musique est jouée en boucle
        verify(backgroundMusic).setLooping(true);
        verify(backgroundMusic).setVolume(0.5f);
        verify(backgroundMusic).play();
    }
    
    @Test
    void testPauseBackgroundMusic() {
        // Mettre en pause la musique de fond
        gameManager.pauseBackgroundMusic();
        
        // Vérifier que la musique est mise en pause
        verify(backgroundMusic).pause();
    }
    
    @Test
    void testDispose() {
        // Libérer les ressources
        gameManager.dispose();
        
        // Vérifier que tous les assets sont libérés
        verify(assetManager).dispose();
        
        // Vérifier que l'instance est réinitialisée
        assertNull(GameManager.getInstance().getAssetManager());
    }
    
    @Test
    void testUpdateScore() {
        int initialScore = gameManager.getScore();
        int points = 100;
        
        gameManager.addScore(points);
        
        assertEquals(initialScore + points, gameManager.getScore());
    }
    
    @Test
    void testPlayerLives() {
        gameManager.setLives(5);
        assertEquals(5, gameManager.getLives());
        
        gameManager.loseLife();
        assertEquals(4, gameManager.getLives());
    }
    
    @Test
    void testGameOver() {
        // Simuler la fin de partie
        gameManager.setLives(1);
        gameManager.loseLife();
        
        assertTrue(gameManager.isGameOver());
    }
    
    @Test
    void testResetGame() {
        // Modifier l'état du jeu
        gameManager.setLives(1);
        gameManager.addScore(100);
        
        // Réinitialiser le jeu
        gameManager.resetGame();
        
        // Vérifier que l'état est réinitialisé
        assertEquals(3, gameManager.getLives());
        assertEquals(0, gameManager.getScore());
        assertFalse(gameManager.isGameOver());
    }
}
