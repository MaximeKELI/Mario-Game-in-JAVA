package com.mariogame.core;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetErrorListener;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.SkinLoader;
import com.badlogic.gdx.assets.loaders.SoundLoader;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Logger;

/**
 * Gestionnaire de chargement des ressources du jeu.
 * Cette classe étend AssetManager pour fournir des méthodes spécifiques au jeu.
 */
public class AssetLoader extends AssetManager implements Disposable {
    private static final Logger log = new Logger(AssetLoader.class.getName(), Logger.DEBUG);
    
    // Chemins des dossiers
    public static final String TEXTURES_DIR = "textures/";
    public static final String SOUNDS_DIR = "sounds/";
    public static final String MUSIC_DIR = "music/";
    public static final String FONTS_DIR = "fonts/";
    public static final String MAPS_DIR = "maps/";
    public static final String UI_DIR = "ui/";
    
    // Préfixes des ressources
    public static final String ATLAS_EXTENSION = ".atlas";
    public static final String TMX_EXTENSION = ".tmx";
    public static final String PNG_EXTENSION = ".png";
    public static final String OGG_EXTENSION = ".ogg";
    public static final String WAV_EXTENSION = ".wav";
    public static final String FNT_EXTENSION = ".fnt";
    public static final String TTF_EXTENSION = ".ttf";
    public static final String JSON_EXTENSION = ".json";
    
    // Fichiers de configuration
    public static final String GAME_SETTINGS = "data/settings.json";
    public static final String UI_SKIN = UI_DIR + "uiskin.json";
    
    // Polices
    public static final String FONT_DEFAULT = FONTS_DIR + "kenvector_future" + TTF_EXTENSION;
    public static final String FONT_SMALL = FONTS_DIR + "kenvector_future" + FNT_EXTENSION;
    public static final String FONT_MEDIUM = FONTS_DIR + "kenvector_future_thin" + FNT_EXTENSION;
    public static final String FONT_LARGE = FONTS_DIR + "kenvector_future_thin_84" + FNT_EXTENSION;
    
    // Atlas de textures
    public static final String ATLAS_PLAYER = TEXTURES_DIR + "player/player" + ATLAS_EXTENSION;
    public static final String ATLAS_ENEMIES = TEXTURES_DIR + "enemies/enemies" + ATLAS_EXTENSION;
    public static final String ATLAS_ITEMS = TEXTURES_DIR + "items/items" + ATLAS_EXTENSION;
    public static final String ATLAS_TILES = TEXTURES_DIR + "tiles/tiles" + ATLAS_EXTENSION;
    public static final String ATLAS_EFFECTS = TEXTURES_DIR + "effects/effects" + ATLAS_EXTENSION;
    
    // Sons
    public static final String SOUND_JUMP = SOUNDS_DIR + "jump" + WAV_EXTENSION;
    public static final String SOUND_COIN = SOUNDS_DIR + "coin" + WAV_EXTENSION;
    public static final String SOUND_POWERUP = SOUNDS_DIR + "powerup" + WAV_EXTENSION;
    public static final String SOUND_HURT = SOUNDS_DIR + "hurt" + WAV_EXTENSION;
    public static final String SOUND_BREAK = SOUNDS_DIR + "break" + WAV_EXTENSION;
    public static final String SOUND_STOMP = SOUNDS_DIR + "stomp" + WAV_EXTENSION;
    
    // Musiques
    public static final String MUSIC_MAIN_THEME = MUSIC_DIR + "main_theme" + OGG_EXTENSION;
    public static final String MUSIC_UNDERGROUND = MUSIC_DIR + "underground" + OGG_EXTENSION;
    public static final String MUSIC_CASTLE = MUSIC_DIR + "castle" + OGG_EXTENSION;
    
    // Niveaux
    public static final String LEVEL_1_1 = MAPS_DIR + "1-1" + TMX_EXTENSION;
    public static final String LEVEL_1_2 = MAPS_DIR + "1-2" + TMX_EXTENSION;
    
    // Polices chargées
    private BitmapFont defaultFont;
    private BitmapFont smallFont;
    private BitmapFont mediumFont;
    private BitmapFont largeFont;
    
    private static AssetLoader instance;
    
    private AssetLoader() {
        super();
        setupLoaders();
        setErrorListener(new AssetErrorReporter());
    }
    
    public static synchronized AssetLoader getInstance() {
        if (instance == null) {
            instance = new AssetLoader();
        }
        return instance;
    }
    
    private void setupLoaders() {
        // Configuration du chargeur de polices
        FileHandleResolver resolver = new InternalFileHandleResolver();
        setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(resolver));
        setLoader(BitmapFont.class, ".ttf", new FreetypeFontLoader(resolver));
        
        // Configuration du chargeur de cartes Tiled
        setLoader(TiledMap.class, new TmxMapLoader(resolver));
    }
    
    /**
     * Charge uniquement les ressources essentielles pour l'écran de chargement.
     * Ces ressources doivent être disponibles immédiatement.
     */
    public static void loadEssentialAssets(AssetManager assetManager) {
        // Charger uniquement les ressources minimales nécessaires pour l'écran de chargement
        // Par exemple, une police de base et peut-être un logo
        try {
            // Charger une police par défaut si nécessaire
            // Les autres ressources seront chargées de manière asynchrone
        } catch (Exception e) {
            log.error("Erreur lors du chargement des ressources essentielles", e);
        }
    }
    
    public void loadAll() {
        log.info("Début du chargement des ressources...");
        
        // Chargement des polices
        loadFonts();
        
        // Chargement des atlas de textures
        loadAtlas(ATLAS_PLAYER);
        loadAtlas(ATLAS_ENEMIES);
        loadAtlas(ATLAS_ITEMS);
        loadAtlas(ATLAS_TILES);
        loadAtlas(ATLAS_EFFECTS);
        
        // Chargement des sons
        loadSound(SOUND_JUMP);
        loadSound(SOUND_COIN);
        loadSound(SOUND_POWERUP);
        loadSound(SOUND_HURT);
        loadSound(SOUND_BREAK);
        loadSound(SOUND_STOMP);
        
        // Chargement des musiques
        loadMusic(MUSIC_MAIN_THEME);
        loadMusic(MUSIC_UNDERGROUND);
        loadMusic(MUSIC_CASTLE);
        
        // Chargement des niveaux
        loadMap(LEVEL_1_1);
        loadMap(LEVEL_1_2);
        
        // Chargement du skin UI
        loadSkin(UI_SKIN);
        
        log.info("Toutes les ressources ont été mises en file d'attente de chargement.");
    }
    
    private void loadFonts() {
        // Chargement de la police par défaut
        FreetypeFontLoader.FreeTypeFontLoaderParameter fontParams = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        fontParams.fontFileName = FONT_DEFAULT;
        fontParams.fontParameters.size = 16;
        load("default" + FNT_EXTENSION, BitmapFont.class, fontParams);
        
        // Chargement des autres tailles de police
        load(FONT_SMALL, BitmapFont.class);
        load(FONT_MEDIUM, BitmapFont.class);
        load(FONT_LARGE, BitmapFont.class);
    }
    
    public void loadAtlas(String path) {
        load(path, TextureAtlas.class);
    }
    
    public void loadSound(String path) {
        load(path, Sound.class);
    }
    
    public void loadMusic(String path) {
        load(path, Music.class);
    }
    
    public void loadMap(String path) {
        load(path, TiledMap.class);
    }
    
    public void loadSkin(String path) {
        SkinLoader.SkinParameter params = new SkinLoader.SkinParameter("ui/uiskin.atlas");
        load(path, Skin.class, params);
    }
    
    // Getters pour les ressources
    
    public TextureAtlas getAtlas(String path) {
        return get(path, TextureAtlas.class);
    }
    
    public Sound getSound(String path) {
        return get(path, Sound.class);
    }
    
    public Music getMusic(String path) {
        return get(path, Music.class);
    }
    
    public TiledMap getMap(String path) {
        return get(path, TiledMap.class);
    }
    
    public Skin getSkin() {
        return get(UI_SKIN, Skin.class);
    }
    
    public BitmapFont getFont(String name) {
        switch (name.toLowerCase()) {
            case "small": return get(FONT_SMALL, BitmapFont.class);
            case "medium": return get(FONT_MEDIUM, BitmapFont.class);
            case "large": return get(FONT_LARGE, BitmapFont.class);
            default: return get("default" + FNT_EXTENSION, BitmapFont.class);
        }
    }
    
    // Méthodes utilitaires
    
    /**
     * Retourne le pourcentage de progression du chargement (0-100).
     */
    public float getProgress() {
        return super.getProgress() * 100f;
    }
    
    /**
     * Vérifie si une ressource est chargée.
     */
    public boolean isResourceLoaded(String fileName) {
        return super.isLoaded(fileName);
    }
    
    public void unloadAll() {
        clear();
    }
    
    @Override
    public void dispose() {
        log.info("Libération des ressources...");
        super.dispose();
        instance = null;
    }
    
    // Classe interne pour la gestion des erreurs de chargement
    private static class AssetErrorReporter implements AssetErrorListener {
        @Override
        public void error(AssetDescriptor asset, Throwable throwable) {
            log.error("Erreur lors du chargement de la ressource: " + asset.fileName, throwable);
        }
    }
}
