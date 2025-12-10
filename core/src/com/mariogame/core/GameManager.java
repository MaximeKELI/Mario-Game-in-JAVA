package com.mariogame.core;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class GameManager {
    private static GameManager instance;
    private AssetManager assetManager;
    
    // Chemins des ressources
    public static final String PLAYER_ATLAS = "player/player.pack";
    public static final String ITEMS_ATLAS = "items/items.pack";
    public static final String ENEMIES_ATLAS = "enemies/enemies.pack";
    public static final String BACKGROUND = "background.png";
    public static final String COIN_SOUND = "sounds/coin.wav";
    public static final String JUMP_SOUND = "sounds/jump.wav";
    public static final String GAME_MUSIC = "music/mario_theme.ogg";
    
    private GameManager() {
        assetManager = new AssetManager();
    }
    
    public static GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }
    
    public void loadAssets() {
        // Chargement des atlas de textures
        assetManager.load(PLAYER_ATLAS, TextureAtlas.class);
        assetManager.load(ITEMS_ATLAS, TextureAtlas.class);
        assetManager.load(ENEMIES_ATLAS, TextureAtlas.class);
        
        // Chargement des textures
        assetManager.load(BACKGROUND, Texture.class);
        
        // Chargement des sons
        assetManager.load(COIN_SOUND, Sound.class);
        assetManager.load(JUMP_SOUND, Sound.class);
        
        // Chargement de la musique
        assetManager.load(GAME_MUSIC, Music.class);
    }
    
    public void dispose() {
        assetManager.dispose();
    }
    
    public boolean update() {
        return assetManager.update();
    }
    
    public float getProgress() {
        return assetManager.getProgress();
    }
    
    public TextureAtlas getAtlas(String fileName) {
        return assetManager.get(fileName, TextureAtlas.class);
    }
    
    public Texture getTexture(String fileName) {
        return assetManager.get(fileName, Texture.class);
    }
    
    public Sound getSound(String fileName) {
        return assetManager.get(fileName, Sound.class);
    }
    
    public Music getMusic(String fileName) {
        return assetManager.get(fileName, Music.class);
    }
}
