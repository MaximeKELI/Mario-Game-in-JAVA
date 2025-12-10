package com.mariogame.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;

/**
 * Gestionnaire centralisé des ressources du jeu.
 * Utilise le pattern Singleton pour un accès global aux ressources.
 */
public class GameManager implements Disposable {
    private static GameManager instance;
    private AssetManager assetManager;
    private boolean assetsLoaded = false;
    
    // Chemins des ressources
    public static final String SKIN_UI = "ui/uiskin.json";
    public static final String FONT_SMALL = "fonts/kenvector_future.fnt";
    public static final String FONT_MEDIUM = "fonts/kenvector_future_thin.fnt";
    public static final String FONT_LARGE = "fonts/kenvector_future_thin_84.fnt";
    
    // Atlas de textures
    public static final String PLAYER_ATLAS = "textures/player/player.atlas";
    public static final String ENEMIES_ATLAS = "textures/enemies/enemies.atlas";
    public static final String ITEMS_ATLAS = "textures/items/items.atlas";
    public static final String TILES_ATLAS = "textures/tiles/tiles.atlas";
    public static final String EFFECTS_ATLAS = "textures/effects/effects.atlas";
    
    // Sons
    public static final String SOUND_JUMP = "sounds/jump.wav";
    public static final String SOUND_COIN = "sounds/coin.wav";
    public static final String SOUND_POWERUP = "sounds/powerup.wav";
    public static final String SOUND_HURT = "sounds/hurt.wav";
    public static final String SOUND_BREAK = "sounds/break.wav";
    public static final String SOUND_STOMP = "sounds/stomp.wav";
    
    // Musiques
    public static final String MUSIC_MAIN_THEME = "music/main_theme.ogg";
    public static final String MUSIC_UNDERGROUND = "music/underground.ogg";
    public static final String MUSIC_CASTLE = "music/castle.ogg";
    
    // Niveaux
    public static final String LEVEL_1_1 = "levels/1-1.tmx";
    public static final String LEVEL_1_2 = "levels/1-2.tmx";
    
    private GameManager() {
        init();
    }
    
    public static synchronized GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }
    
    private void init() {
        assetManager = new AssetManager();
        
        // Configuration du chargeur de polices
        InternalFileHandleResolver filePathResolver = new InternalFileHandleResolver();
        assetManager.setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(filePathResolver));
        assetManager.setLoader(BitmapFont.class, ".ttf", new FreetypeFontLoader(filePathResolver));
        
        // Configuration du chargeur de cartes Tiled
        assetManager.setLoader(TiledMap.class, new TmxMapLoader(filePathResolver));
        
        // Chargement des polices
        loadFonts();
    }
    
    private void loadFonts() {
        // Configuration des paramètres de police
        FreetypeFontLoader.FreeTypeFontLoaderParameter fontParams = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        fontParams.fontFileName = "fonts/kenvector_future.ttf";
        fontParams.fontParameters.size = 16;
        assetManager.load("default.ttf", BitmapFont.class, fontParams);
        
        // Autres tailles de police...
    }
    
    public void loadAssets() {
        if (assetsLoaded) return;
        
        // Chargement des atlas de textures
        assetManager.load(PLAYER_ATLAS, TextureAtlas.class);
        assetManager.load(ENEMIES_ATLAS, TextureAtlas.class);
        assetManager.load(ITEMS_ATLAS, TextureAtlas.class);
        assetManager.load(TILES_ATLAS, TextureAtlas.class);
        assetManager.load(EFFECTS_ATLAS, TextureAtlas.class);
        
        // Chargement des sons
        assetManager.load(SOUND_JUMP, Sound.class);
        assetManager.load(SOUND_COIN, Sound.class);
        assetManager.load(SOUND_POWERUP, Sound.class);
        assetManager.load(SOUND_HURT, Sound.class);
        assetManager.load(SOUND_BREAK, Sound.class);
        assetManager.load(SOUND_STOMP, Sound.class);
        
        // Chargement des musiques
        assetManager.load(MUSIC_MAIN_THEME, Music.class);
        assetManager.load(MUSIC_UNDERGROUND, Music.class);
        assetManager.load(MUSIC_CASTLE, Music.class);
        
        // Chargement des niveaux
        assetManager.load(LEVEL_1_1, TiledMap.class);
        assetManager.load(LEVEL_1_2, TiledMap.class);
        
        // Chargement du skin UI
        // assetManager.load(SKIN_UI, Skin.class);
        
        assetsLoaded = true;
    }
    
    public boolean update() {
        return assetManager.update();
    }
    
    public float getProgress() {
        return assetManager.getProgress();
    }
    
    public void finishLoading() {
        assetManager.finishLoading();
    }
    
    public boolean isLoaded(String fileName) {
        return assetManager.isLoaded(fileName);
    }
    
    public TextureAtlas getAtlas(String fileName) {
        if (!assetManager.isLoaded(fileName)) {
            Gdx.app.error("GameManager", "Atlas not loaded: " + fileName);
            return null;
        }
        return assetManager.get(fileName, TextureAtlas.class);
    }
    
    public Sound getSound(String fileName) {
        if (!assetManager.isLoaded(fileName)) {
            Gdx.app.error("GameManager", "Sound not loaded: " + fileName);
            return null;
        }
        return assetManager.get(fileName, Sound.class);
    }
    
    public Music getMusic(String fileName) {
        if (!assetManager.isLoaded(fileName)) {
            Gdx.app.error("GameManager", "Music not loaded: " + fileName);
            return null;
        }
        return assetManager.get(fileName, Music.class);
    }
    
    public TiledMap getMap(String fileName) {
        if (!assetManager.isLoaded(fileName)) {
            Gdx.app.error("GameManager", "Map not loaded: " + fileName);
            return null;
        }
        return assetManager.get(fileName, TiledMap.class);
    }
    
    public BitmapFont getFont(String name) {
        if (name.equals("small")) {
            return assetManager.get(FONT_SMALL, BitmapFont.class);
        } else if (name.equals("medium")) {
            return assetManager.get(FONT_MEDIUM, BitmapFont.class);
        } else if (name.equals("large")) {
            return assetManager.get(FONT_LARGE, BitmapFont.class);
        }
        return assetManager.get("default.ttf", BitmapFont.class);
    }
    
    public void unloadAssets() {
        assetManager.unload(PLAYER_ATLAS);
        assetManager.unload(ENEMIES_ATLAS);
        assetManager.unload(ITEMS_ATLAS);
        assetManager.unload(TILES_ATLAS);
        assetManager.unload(EFFECTS_ATLAS);
        
        // Ne pas décharger les polices pour éviter de les recharger
    }
    
    @Override
    public void dispose() {
        if (assetManager != null) {
            assetManager.dispose();
            assetManager = null;
        }
        instance = null;
    }
}
