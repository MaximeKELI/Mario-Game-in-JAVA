package com.mariogame.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;
import com.mariogame.entities.Player;
import com.mariogame.utils.Constants;

/**
 * Gère les effets sonores et la musique du jeu avec support 3D.
 */
public class SoundManager implements Disposable {
    private final ObjectMap<String, Sound> sounds = new ObjectMap<>();
    private final ObjectMap<String, Music> musicTracks = new ObjectMap<>();
    private final Array<Long> activeSounds = new Array<>();
    private final Player player;
    private Music currentMusic;
    private float soundVolume = 0.8f;
    private float musicVolume = 0.7f;
    private boolean soundEnabled = true;
    private boolean musicEnabled = true;
    private float globalPitch = 1.0f;
    
    // Paramètres audio 3D
    private static final float MAX_DISTANCE = 20f; // Distance maximale pour entendre un son
    private static final float REFERENCE_DISTANCE = 5f; // Distance de référence pour l'atténuation
    private static final float ROLLOFF_FACTOR = 1f; // Facteur d'atténuation
    
    public SoundManager(Player player) {
        this.player = player;
    }
    
    /**
     * Charge un effet sonore.
     */
    public void loadSound(String name, String path) {
        if (!sounds.containsKey(name)) {
            Sound sound = Gdx.audio.newSound(Gdx.files.internal(path));
            sounds.put(name, sound);
        }
    }
    
    /**
     * Charge une piste musicale.
     */
    public void loadMusic(String name, String path) {
        if (!musicTracks.containsKey(name)) {
            Music music = Gdx.audio.newMusic(Gdx.files.internal(path));
            musicTracks.put(name, music);
        }
    }
    
    /**
     * Joue un effet sonore à une position donnée (son 3D).
     */
    public long playSound3D(String name, Vector2 position, float volume) {
        if (!soundEnabled || !sounds.containsKey(name)) return -1;
        
        Sound sound = sounds.get(name);
        float distance = position.dst(player.getPosition());
        
        // Calcul du volume en fonction de la distance (atténuation en fonction de la distance)
        float distanceVolume = calculateVolume(distance, volume);
        
        // Calcul du panoramique (pan) en fonction de la position relative
        float pan = calculatePan(position);
        
        // Lecture du son avec les paramètres 3D
        long soundId = sound.play(distanceVolume, globalPitch, pan);
        activeSounds.add(soundId);
        
        return soundId;
    }
    
    /**
     * Joue un effet sonore sans position (son 2D).
     */
    public long playSound(String name, float volume) {
        if (!soundEnabled || !sounds.containsKey(name)) return -1;
        
        Sound sound = sounds.get(name);
        long soundId = sound.play(volume * soundVolume, globalPitch, 0);
        activeSounds.add(soundId);
        
        return soundId;
    }
    
    /**
     * Joue une musique en boucle.
     */
    public void playMusic(String name, boolean loop) {
        if (!musicEnabled || !musicTracks.containsKey(name)) return;
        
        // Arrêter la musique actuelle si elle est en cours de lecture
        if (currentMusic != null) {
            currentMusic.stop();
        }
        
        currentMusic = musicTracks.get(name);
        currentMusic.setLooping(loop);
        currentMusic.setVolume(musicVolume);
        currentMusic.play();
    }
    
    /**
     * Met à jour les paramètres audio en fonction de la position du joueur.
     * Doit être appelée à chaque frame.
     */
    public void update() {
        // Mettre à jour le volume de la musique en fonction de la distance
        // et d'autres facteurs (comme l'environnement)
        
        // Mettre à jour les effets sonores 3D actifs
        update3DSounds();
        
        // Nettoyer les sons terminés
        cleanUpFinishedSounds();
    }
    
    private void update3DSounds() {
        // Mettre à jour les paramètres 3D des sons en cours de lecture
        // (volume, panoramique, etc.)
    }
    
    private void cleanUpFinishedSounds() {
        // Supprimer les IDs de sons terminés de la liste des sons actifs
        for (int i = activeSounds.size - 1; i >= 0; i--) {
            long soundId = activeSounds.get(i);
            boolean isPlaying = false;
            
            // Vérifier si le son est toujours en cours de lecture
            for (Sound sound : sounds.values()) {
                if (sound.loop(soundId) || sound.playing(soundId)) {
                    isPlaying = true;
                    break;
                }
            }
            
            if (!isPlaying) {
                activeSounds.removeIndex(i);
            }
        }
    }
    
    /**
     * Calcule le volume en fonction de la distance (atténuation).
     */
    private float calculateVolume(float distance, float baseVolume) {
        // Atténuation en fonction de la distance (loi en carré inverse)
        float distanceFactor = Math.min(1.0f, REFERENCE_DISTANCE / 
            (REFERENCE_DISTANCE + ROLLOFF_FACTOR * Math.max(0, distance - REFERENCE_DISTANCE)));
        
        return baseVolume * soundVolume * distanceFactor * distanceFactor;
    }
    
    /**
     * Calcule le panoramique (pan) en fonction de la position relative.
     */
    private float calculatePan(Vector2 position) {
        // Calculer la position relative par rapport au joueur
        float relativeX = position.x - player.getPosition().x;
        
        // Normaliser entre -1 (gauche) et 1 (droite)
        float pan = MathUtils.clamp(relativeX / MAX_DISTANCE, -1f, 1f);
        
        // Appliquer une courbe plus douce
        return (float) Math.sin(pan * Math.PI / 2);
    }
    
    /**
     * Joue un son avec un décalage aléatoire de hauteur.
     */
    public long playSoundWithPitchVariation(String name, float volume, float variation) {
        float pitch = MathUtils.random(1.0f - variation, 1.0f + variation);
        return playSound(name, volume, pitch);
    }
    
    private long playSound(String name, float volume, float pitch) {
        if (!soundEnabled || !sounds.containsKey(name)) return -1;
        
        Sound sound = sounds.get(name);
        long soundId = sound.play(volume * soundVolume, pitch, 0);
        activeSounds.add(soundId);
        
        return soundId;
    }
    
    /**
     * Arrête tous les sons en cours de lecture.
     */
    public void stopAllSounds() {
        for (Sound sound : sounds.values()) {
            sound.stop();
        }
        activeSounds.clear();
    }
    
    /**
     * Arrête la musique en cours de lecture.
     */
    public void stopMusic() {
        if (currentMusic != null) {
            currentMusic.stop();
            currentMusic = null;
        }
    }
    
    /**
     * Définit le volume des effets sonores.
     */
    public void setSoundVolume(float volume) {
        this.soundVolume = MathUtils.clamp(volume, 0f, 1f);
        
        // Mettre à jour le volume des sons en cours de lecture
        for (long soundId : activeSounds) {
            for (Sound sound : sounds.values()) {
                if (sound.playing(soundId)) {
                    sound.setVolume(soundId, this.soundVolume);
                }
            }
        }
    }
    
    /**
     * Définit le volume de la musique.
     */
    public void setMusicVolume(float volume) {
        this.musicVolume = MathUtils.clamp(volume, 0f, 1f);
        
        // Mettre à jour le volume de la musique en cours de lecture
        if (currentMusic != null) {
            currentMusic.setVolume(musicVolume);
        }
    }
    
    /**
     * Active ou désactive les effets sonores.
     */
    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
        
        if (!enabled) {
            stopAllSounds();
        }
    }
    
    /**
     * Active ou désactive la musique.
     */
    public void setMusicEnabled(boolean enabled) {
        this.musicEnabled = enabled;
        
        if (enabled) {
            if (currentMusic != null) {
                currentMusic.play();
            }
        } else {
            stopMusic();
        }
    }
    
    /**
     * Définit le pitch global des effets sonores.
     */
    public void setGlobalPitch(float pitch) {
        this.globalPitch = MathUtils.clamp(pitch, 0.5f, 2.0f);
    }
    
    /**
     * Libère les ressources audio.
     */
    @Override
    public void dispose() {
        // Arrêter tous les sons
        stopAllSounds();
        stopMusic();
        
        // Libérer les ressources des sons
        for (Sound sound : sounds.values()) {
            sound.dispose();
        }
        sounds.clear();
        
        // Libérer les ressources des musiques
        for (Music music : musicTracks.values()) {
            music.dispose();
        }
        musicTracks.clear();
        
        activeSounds.clear();
    }
    
    // Getters
    
    public float getSoundVolume() {
        return soundVolume;
    }
    
    public float getMusicVolume() {
        return musicVolume;
    }
    
    public boolean isSoundEnabled() {
        return soundEnabled;
    }
    
    public boolean isMusicEnabled() {
        return musicEnabled;
    }
    
    public float getGlobalPitch() {
        return globalPitch;
    }
}
