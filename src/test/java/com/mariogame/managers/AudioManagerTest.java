package com.mariogame.managers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.ObjectMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AudioManagerTest {

    @Mock private Music backgroundMusic;
    @Mock private Sound jumpSound, coinSound, powerupSound, deathSound;
    
    private AudioManager audioManager;
    private ObjectMap<String, Sound> soundEffects;
    
    @BeforeEach
    void setUp() {
        // Initialisation des mocks
        soundEffects = new ObjectMap<>();
        soundEffects.put("jump", jumpSound);
        soundEffects.put("coin", coinSound);
        soundEffects.put("powerup", powerupSound);
        soundEffects.put("death", deathSound);
        
        // Initialisation de l'AudioManager avec les mocks
        audioManager = new AudioManager();
        audioManager.setBackgroundMusic(backgroundMusic);
        audioManager.setSoundEffects(soundEffects);
    }

    @Test
    void testPlayBackgroundMusic() {
        // Test de la lecture de la musique de fond
        audioManager.playBackgroundMusic();
        
        verify(backgroundMusic).setLooping(true);
        verify(backgroundMusic).setVolume(0.5f);
        verify(backgroundMusic).play();
    }
    
    @Test
    void testPauseBackgroundMusic() {
        // Test de la mise en pause de la musique
        audioManager.pauseBackgroundMusic();
        
        verify(backgroundMusic).pause();
    }
    
    @Test
    void testStopBackgroundMusic() {
        // Test de l'arrêt de la musique
        audioManager.stopBackgroundMusic();
        
        verify(backgroundMusic).stop();
    }
    
    @Test
    void testSetMusicVolume() {
        // Test du réglage du volume de la musique
        float volume = 0.7f;
        audioManager.setMusicVolume(volume);
        
        verify(backgroundMusic).setVolume(volume);
    }
    
    @Test
    void testPlaySound() {
        // Test de la lecture d'un effet sonore
        audioManager.playSound("jump");
        
        verify(jumpSound).play(0.5f);
    }
    
    @Test
    void testPlaySoundWithVolume() {
        // Test de la lecture d'un effet sonore avec un volume personnalisé
        float volume = 0.8f;
        audioManager.playSound("coin", volume);
        
        verify(coinSound).play(volume);
    }
    
    @Test
    void testPlaySound_InvalidSound() {
        // Test avec un identifiant de son invalide
        assertDoesNotThrow(() -> audioManager.playSound("invalid_sound"));
    }
    
    @Test
    void testSetSoundVolume() {
        // Test du réglage du volume des effets sonores
        float volume = 0.6f;
        audioManager.setSoundVolume(volume);
        
        // Vérifier que le volume a été mis à jour pour la lecture future
        audioManager.playSound("jump");
        verify(jumpSound).play(volume);
    }
    
    @Test
    void testDispose() {
        // Test de la libération des ressources
        audioManager.dispose();
        
        // Vérifier que toutes les ressources ont été libérées
        verify(backgroundMusic).dispose();
        soundEffects.values().forEach(sound -> verify(sound).dispose());
    }
    
    @Test
    void testIsMusicPlaying() {
        // Test de la vérification de la lecture de la musique
        when(backgroundMusic.isPlaying()).thenReturn(true);
        assertTrue(audioManager.isMusicPlaying());
        
        when(backgroundMusic.isPlaying()).thenReturn(false);
        assertFalse(audioManager.isMusicPlaying());
    }
    
    @Test
    void testToggleMute() {
        // Test du mode muet
        audioManager.toggleMute();
        verify(backgroundMusic).setVolume(0f);
        
        audioManager.toggleMute();
        verify(backgroundMusic).setVolume(0.5f);
    }
}
