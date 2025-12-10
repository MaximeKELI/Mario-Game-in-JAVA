package com.mariogame.utils;

/**
 * Classe contenant toutes les constantes du jeu.
 * Permet une gestion centralisée des valeurs fixes utilisées dans tout le jeu.
 */
public final class Constants {
    // Empêche l'instanciation de la classe
    private Constants() {}
    
    // Constantes de l'application
    public static final String GAME_TITLE = "Super Mario Bros.";
    public static final float GRAVITY = -20f;
    public static final float PPM = 100f; // Pixels Per Meter
    
    // Catégories de collision (bits)
    public static final class CollisionBits {
        public static final short NOTHING = 0;
        public static final short GROUND = 1;
        public static final short PLAYER = 2;
        public static final short ENEMY = 4;
        public static final short ITEM = 8;
        public static final short SENSOR = 16;
        public static final short PLAYER_HEAD = 32;
        public static final short ENEMY_HEAD = 64;
        public static final short PROJECTILE = 128;
        
        // Masques de collision
        public static final class Masks {
            public static final short PLAYER = GROUND | ENEMY | ITEM | SENSOR | ENEMY_HEAD;
            public static final short ENEMY = GROUND | PLAYER | ITEM | PLAYER_HEAD | ENEMY | PROJECTILE;
            public static final short ITEM = GROUND | PLAYER | ENEMY | PROJECTILE;
            public static final short SENSOR = PLAYER | ENEMY;
            public static final short PROJECTILE = GROUND | ENEMY | ITEM;
        }
    }
    
    // Noms des fichiers de ressources
    public static final class Assets {
        // Dossiers
        public static final String TEXTURES_DIR = "textures/";
        public static final String SOUNDS_DIR = "sounds/";
        public static final String MUSIC_DIR = "music/";
        public static final String FONTS_DIR = "fonts/";
        public static final String MAPS_DIR = "maps/";
        public static final String UI_DIR = "ui/";
        
        // Extensions
        public static final String ATLAS_EXT = ".atlas";
        public static final String TMX_EXT = ".tmx";
        public static final String PNG_EXT = ".png";
        public static final String OGG_EXT = ".ogg";
        public static final String WAV_EXT = ".wav";
        public static final String FNT_EXT = ".fnt";
        public static final String TTF_EXT = ".ttf";
        public static final String JSON_EXT = ".json";
        
        // Atlas de textures
        public static final class Atlases {
            public static final String PLAYER = TEXTURES_DIR + "player/player" + ATLAS_EXT;
            public static final String ENEMIES = TEXTURES_DIR + "enemies/enemies" + ATLAS_EXT;
            public static final String ITEMS = TEXTURES_DIR + "items/items" + ATLAS_EXT;
            public static final String TILES = TEXTURES_DIR + "tiles/tiles" + ATLAS_EXT;
            public static final String EFFECTS = TEXTURES_DIR + "effects/effects" + ATLAS_EXT;
            public static final String UI = UI_DIR + "uiskin" + ATLAS_EXT;
        }
        
        // Polices
        public static final class Fonts {
            public static final String DEFAULT = FONTS_DIR + "kenvector_future" + TTF_EXT;
            public static final String SMALL = FONTS_DIR + "kenvector_future" + FNT_EXT;
            public static final String MEDIUM = FONTS_DIR + "kenvector_future_thin" + FNT_EXT;
            public static final String LARGE = FONTS_DIR + "kenvector_future_thin_84" + FNT_EXT;
        }
        
        // Sons
        public static final class Sounds {
            public static final String JUMP = SOUNDS_DIR + "jump" + WAV_EXT;
            public static final String COIN = SOUNDS_DIR + "coin" + WAV_EXT;
            public static final String POWERUP = SOUNDS_DIR + "powerup" + WAV_EXT;
            public static final String HURT = SOUNDS_DIR + "hurt" + WAV_EXT;
            public static final String BREAK = SOUNDS_DIR + "break" + WAV_EXT;
            public static final String STOMP = SOUNDS_DIR + "stomp" + WAV_EXT;
            public static final String FIREBALL = SOUNDS_DIR + "fireball" + WAV_EXT;
        }
        
        // Musiques
        public static final class Music {
            public static final String MAIN_THEME = MUSIC_DIR + "main_theme" + OGG_EXT;
            public static final String UNDERGROUND = MUSIC_DIR + "underground" + OGG_EXT;
            public static final String CASTLE = MUSIC_DIR + "castle" + OGG_EXT;
            public static final String GAME_OVER = MUSIC_DIR + "game_over" + OGG_EXT;
        }
        
        // Niveaux
        public static final class Levels {
            public static final String WORLD_1_1 = MAPS_DIR + "1-1" + TMX_EXT;
            public static final String WORLD_1_2 = MAPS_DIR + "1-2" + TMX_EXT;
            public static final String WORLD_1_3 = MAPS_DIR + "1-3" + TMX_EXT;
            public static final String WORLD_1_4 = MAPS_DIR + "1-4" + TMX_EXT;
        }
        
        // UI
        public static final class UI {
            public static final String SKIN = UI_DIR + "uiskin" + JSON_EXT;
        }
    }
    
    // Préférences
    public static final class Preferences {
        public static final String NAME = "mario_game_prefs";
        
        // Clés des préférences
        public static final String VSYNC = "vsync";
        public static final String FULLSCREEN = "fullscreen";
        public static final String RESOLUTION_WIDTH = "resolution_width";
        public static final String RESOLUTION_HEIGHT = "resolution_height";
        public static final String MUSIC_VOLUME = "music_volume";
        public static final String SOUND_VOLUME = "sound_volume";
        public static final String DEBUG_MODE = "debug_mode";
        public static final String LANGUAGE = "language";
        
        // Valeurs par défaut
        public static final class Defaults {
            public static final boolean VSYNC = true;
            public static final boolean FULLSCREEN = false;
            public static final int WIDTH = 1280;
            public static final int HEIGHT = 720;
            public static final float MUSIC_VOLUME = 0.7f;
            public static final float SOUND_VOLUME = 0.8f;
            public static final boolean DEBUG_MODE = false;
            public static final String LANGUAGE = "en";
        }
    }
    
    // Configuration du joueur
    public static final class PlayerConfig {
        // Vitesses
        public static final float WALK_SPEED = 4f;
        public static final float RUN_SPEED = 6f;
        public static final float JUMP_FORCE = 12f;
        public static final float DAMPING = 0.9f;
        
        // Taille de la hitbox (en mètres)
        public static final float WIDTH = 0.8f;
        public static final float HEIGHT = 1.8f;
        
        // Propriétés physiques
        public static final float DENSITY = 1f;
        public static final float FRICTION = 0.6f;
        public static final float RESTITUTION = 0.1f;
        
        // Autres propriétés
        public static final float INVINCIBILITY_DURATION = 3f;
        public static final int MAX_LIVES = 5;
        public static final int STARTING_LIVES = 3;
    }
    
    // Configuration de la caméra
    public static final class CameraConfig {
        public static final float FOLLOW_SPEED = 0.1f;
        public static final float ZOOM = 1f;
        public static final float MIN_ZOOM = 0.5f;
        public static final float MAX_ZOOM = 2f;
        public static final float ZOOM_SPEED = 0.1f;
    }
    
    // Configuration du monde
    public static final class WorldConfig {
        public static final float GRAVITY = -30f;
        public static final float PPM = 100f; // Pixels Per Meter
        public static final float V_WIDTH = 16f; // Largeur virtuelle en mètres
        public static final float V_HEIGHT = 9f; // Hauteur virtuelle en mètres
        public static final int VELOCITY_ITERATIONS = 6;
        public static final int POSITION_ITERATIONS = 2;
        public static final float TIME_STEP = 1/60f;
    }
}
