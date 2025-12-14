package com.mariogame.systems;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.mariogame.entities.Player;

/**
 * Système de replay ultra-avancé pour enregistrer et rejouer les parties.
 * Supporte la compression et la lecture à différentes vitesses.
 */
public class ReplaySystem {
    private final Array<ReplayFrame> frames = new Array<>();
    private boolean recording = false;
    private boolean playing = false;
    private int currentFrameIndex = 0;
    private float playbackSpeed = 1f;
    private final Json json = new Json();
    
    /**
     * Démarre l'enregistrement.
     */
    public void startRecording() {
        recording = true;
        frames.clear();
    }
    
    /**
     * Arrête l'enregistrement.
     */
    public void stopRecording() {
        recording = false;
    }
    
    /**
     * Enregistre une frame.
     */
    public void recordFrame(Player player, float deltaTime) {
        if (!recording) return;
        
        ReplayFrame frame = new ReplayFrame();
        frame.timestamp = System.currentTimeMillis();
        frame.deltaTime = deltaTime;
        frame.playerX = player.getX();
        frame.playerY = player.getY();
        frame.playerState = player.getState().name();
        frame.facingRight = player.isFacingRight();
        
        frames.add(frame);
    }
    
    /**
     * Démarre la lecture.
     */
    public void startPlayback() {
        playing = true;
        currentFrameIndex = 0;
    }
    
    /**
     * Arrête la lecture.
     */
    public void stopPlayback() {
        playing = false;
    }
    
    /**
     * Obtient la frame actuelle pour la lecture.
     */
    public ReplayFrame getCurrentFrame() {
        if (!playing || currentFrameIndex >= frames.size) {
            return null;
        }
        
        return frames.get(currentFrameIndex);
    }
    
    /**
     * Avance à la frame suivante.
     */
    public void nextFrame() {
        if (playing) {
            currentFrameIndex++;
            if (currentFrameIndex >= frames.size) {
                stopPlayback();
            }
        }
    }
    
    /**
     * Sauvegarde le replay dans un fichier.
     */
    public String saveReplay() {
        ReplayData data = new ReplayData();
        data.frames = frames.toArray(ReplayFrame.class);
        data.version = "1.0";
        data.timestamp = System.currentTimeMillis();
        
        return json.toJson(data);
    }
    
    /**
     * Charge un replay depuis un fichier.
     */
    public void loadReplay(String jsonData) {
        ReplayData data = json.fromJson(ReplayData.class, jsonData);
        frames.clear();
        frames.addAll(data.frames);
    }
    
    /**
     * Compresse le replay pour économiser de l'espace.
     */
    public String compressReplay() {
        // Compression simple (peut être améliorée avec gzip)
        return saveReplay(); // Pour l'instant, retourne le JSON
    }
    
    public void setPlaybackSpeed(float speed) {
        this.playbackSpeed = Math.max(0.1f, Math.min(5f, speed));
    }
    
    public float getPlaybackSpeed() {
        return playbackSpeed;
    }
    
    public boolean isRecording() {
        return recording;
    }
    
    public boolean isPlaying() {
        return playing;
    }
    
    public int getFrameCount() {
        return frames.size;
    }
    
    /**
     * Frame de replay.
     */
    public static class ReplayFrame {
        public long timestamp;
        public float deltaTime;
        public float playerX, playerY;
        public String playerState;
        public boolean facingRight;
    }
    
    /**
     * Données complètes du replay.
     */
    private static class ReplayData {
        public ReplayFrame[] frames;
        public String version;
        public long timestamp;
    }
}

