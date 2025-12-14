package com.mariogame.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Disposable;
import com.mariogame.config.GameConfig;
import com.mariogame.utils.Constants;

import java.util.HashMap;
import java.util.Map;

/**
 * Gestionnaire audio professionnel pour la musique et les effets sonores.
 * Gère le volume, la mise en cache et la lecture optimisée des sons.
 */
public class AudioManager implements Disposable {
    private final AssetManager assetManager;
    private final GameConfig config;
    
    // Musiques actives
    private Music currentMusic;
    private String currentMusicName;
    private boolean musicEnabled = true;
    
    // Cache des sons pour performance
    private final Map<String, Sound> soundCache = new HashMap<>();
    
    // IDs des sons en cours de lecture (pour pouvoir les arrêter)
    private final Map<String, Long> playingSounds = new HashMap<>();
    
    public AudioManager(AssetManager assetManager) {
        this.assetManager = assetManager;
        this.config = new GameConfig();
    }
    
    /**
     * Joue une musique en boucle.
     */
    public void playMusic(String musicPath) {
        if (!musicEnabled) return;
        
        // Arrêter la musique actuelle si différente
        if (currentMusic != null && !musicPath.equals(currentMusicName)) {
            currentMusic.stop();
            currentMusic.dispose();
        }
        
        // Charger et jouer la nouvelle musique
        if (assetManager.isLoaded(musicPath, Music.class)) {
            currentMusic = assetManager.get(musicPath, Music.class);
            currentMusicName = musicPath;
            currentMusic.setLooping(true);
            currentMusic.setVolume(config.getMusicVolume());
            currentMusic.play();
        } else {
            Gdx.app.error("AudioManager", "Music not loaded: " + musicPath);
        }
    }
    
    /**
     * Arrête la musique actuelle.
     */
    public void stopMusic() {
        if (currentMusic != null) {
            currentMusic.stop();
        }
    }
    
    /**
     * Met en pause/reprend la musique.
     */
    public void pauseMusic() {
        if (currentMusic != null && currentMusic.isPlaying()) {
            currentMusic.pause();
        }
    }
    
    public void resumeMusic() {
        if (currentMusic != null && !currentMusic.isPlaying()) {
            currentMusic.play();
        }
    }
    
    /**
     * Joue un effet sonore.
     */
    public void playSound(String soundPath) {
        playSound(soundPath, 1.0f);
    }
    
    /**
     * Joue un effet sonore avec un volume spécifique.
     */
    public void playSound(String soundPath, float volume) {
        if (!config.isSoundEnabled()) return;
        
        if (assetManager.isLoaded(soundPath, Sound.class)) {
            Sound sound = assetManager.get(soundPath, Sound.class);
            long soundId = sound.play(volume * config.getSoundVolume());
            playingSounds.put(soundPath, soundId);
        } else {
            Gdx.app.error("AudioManager", "Sound not loaded: " + soundPath);
        }
    }
    
    /**
     * Arrête un son en cours de lecture.
     */
    public void stopSound(String soundPath) {
        if (playingSounds.containsKey(soundPath)) {
            if (assetManager.isLoaded(soundPath, Sound.class)) {
                Sound sound = assetManager.get(soundPath, Sound.class);
                sound.stop(playingSounds.get(soundPath));
            }
            playingSounds.remove(soundPath);
        }
    }
    
    /**
     * Arrête tous les sons en cours.
     */
    public void stopAllSounds() {
        for (Map.Entry<String, Long> entry : playingSounds.entrySet()) {
            if (assetManager.isLoaded(entry.getKey(), Sound.class)) {
                Sound sound = assetManager.get(entry.getKey(), Sound.class);
                sound.stop(entry.getValue());
            }
        }
        playingSounds.clear();
    }
    
    /**
     * Met à jour le volume de la musique.
     */
    public void setMusicVolume(float volume) {
        config.setMusicVolume(volume);
        if (currentMusic != null) {
            currentMusic.setVolume(volume);
        }
    }
    
    /**
     * Met à jour le volume des effets sonores.
     */
    public void setSoundVolume(float volume) {
        config.setSoundVolume(volume);
    }
    
    /**
     * Active/désactive la musique.
     */
    public void setMusicEnabled(boolean enabled) {
        this.musicEnabled = enabled;
        if (!enabled && currentMusic != null) {
            currentMusic.stop();
        } else if (enabled && currentMusic != null && !currentMusic.isPlaying()) {
            currentMusic.play();
        }
    }
    
    /**
     * Vérifie si la musique est activée.
     */
    public boolean isMusicEnabled() {
        return musicEnabled;
    }
    
    @Override
    public void dispose() {
        stopMusic();
        stopAllSounds();
        if (currentMusic != null) {
            currentMusic.dispose();
        }
        soundCache.clear();
        playingSounds.clear();
    }
}

