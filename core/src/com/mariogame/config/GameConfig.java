package com.mariogame.config;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.mariogame.utils.Constants;

/**
 * Classe de configuration du jeu.
 * Gère les paramètres du jeu et leur persistance.
 */
public class GameConfig {
    // Paramètres graphiques
    private boolean vSyncEnabled;
    private boolean fullscreen;
    private int resolutionWidth;
    private int resolutionHeight;
    private float musicVolume;
    private float soundVolume;
    
    // Paramètres de jeu
    private boolean debugMode;
    private String language;
    
    // Référence aux préférences
    private final Preferences prefs;
    
    public GameConfig() {
        // Charger les préférences
        prefs = Gdx.app.getPreferences(Constants.Preferences.NAME);
        
        // Charger les valeurs par défaut
        load();
    }
    
    /**
     * Charge la configuration depuis les préférences
     */
    public void load() {
        // Paramètres graphiques
        vSyncEnabled = prefs.getBoolean(Constants.Preferences.VSYNC, true);
        fullscreen = prefs.getBoolean(Constants.Preferences.FULLSCREEN, false);
        resolutionWidth = prefs.getInteger(Constants.Preferences.RESOLUTION_WIDTH, 1280);
        resolutionHeight = prefs.getInteger(Constants.Preferences.RESOLUTION_HEIGHT, 720);
        
        // Paramètres audio
        musicVolume = prefs.getFloat(Constants.Preferences.MUSIC_VOLUME, 0.7f);
        soundVolume = prefs.getFloat(Constants.Preferences.SOUND_VOLUME, 0.8f);
        
        // Paramètres de jeu
        debugMode = prefs.getBoolean(Constants.Preferences.DEBUG_MODE, false);
        language = prefs.getString(Constants.Preferences.LANGUAGE, "en");
    }
    
    /**
     * Sauvegarde la configuration dans les préférences
     */
    public void save() {
        // Paramètres graphiques
        prefs.putBoolean(Constants.Preferences.VSYNC, vSyncEnabled);
        prefs.putBoolean(Constants.Preferences.FULLSCREEN, fullscreen);
        prefs.putInteger(Constants.Preferences.RESOLUTION_WIDTH, resolutionWidth);
        prefs.putInteger(Constants.Preferences.RESOLUTION_HEIGHT, resolutionHeight);
        
        // Paramètres audio
        prefs.putFloat(Constants.Preferences.MUSIC_VOLUME, musicVolume);
        prefs.putFloat(Constants.Preferences.SOUND_VOLUME, soundVolume);
        
        // Paramètres de jeu
        prefs.putBoolean(Constants.Preferences.DEBUG_MODE, debugMode);
        prefs.putString(Constants.Preferences.LANGUAGE, language);
        
        // Sauvegarder les modifications
        prefs.flush();
    }
    
    // Getters et Setters
    
    public boolean isVSyncEnabled() {
        return vSyncEnabled;
    }
    
    public void setVSyncEnabled(boolean vSyncEnabled) {
        this.vSyncEnabled = vSyncEnabled;
        Gdx.graphics.setVSync(vSyncEnabled);
    }
    
    public boolean isFullscreen() {
        return fullscreen;
    }
    
    public void setFullscreen(boolean fullscreen) {
        this.fullscreen = fullscreen;
        if (fullscreen) {
            Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
        } else {
            Gdx.graphics.setWindowedMode(resolutionWidth, resolutionHeight);
        }
    }
    
    public int getResolutionWidth() {
        return resolutionWidth;
    }
    
    public int getResolutionHeight() {
        return resolutionHeight;
    }
    
    public void setResolution(int width, int height) {
        this.resolutionWidth = width;
        this.resolutionHeight = height;
        if (!fullscreen) {
            Gdx.graphics.setWindowedMode(width, height);
        }
    }
    
    public float getMusicVolume() {
        return musicVolume;
    }
    
    public void setMusicVolume(float volume) {
        this.musicVolume = Math.max(0, Math.min(1, volume));
    }
    
    public float getSoundVolume() {
        return soundVolume;
    }
    
    public void setSoundVolume(float volume) {
        this.soundVolume = Math.max(0, Math.min(1, volume));
    }
    
    public boolean isDebugMode() {
        return debugMode;
    }
    
    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }
    
    public String getLanguage() {
        return language;
    }
    
    public void setLanguage(String language) {
        this.language = language;
    }
    
    /**
     * Réinitialise la configuration aux valeurs par défaut
     */
    public void resetToDefaults() {
        // Réinitialisation des valeurs par défaut
        vSyncEnabled = true;
        fullscreen = false;
        resolutionWidth = 1280;
        resolutionHeight = 720;
        musicVolume = 0.7f;
        soundVolume = 0.8f;
        debugMode = false;
        language = "en";
        
        // Sauvegarder les modifications
        save();
    }
}
