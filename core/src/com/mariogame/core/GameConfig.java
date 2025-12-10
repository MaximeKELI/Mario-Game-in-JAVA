package com.mariogame.core;

import com.badlogic.gdx.Input;

public class GameConfig {
    // Configuration des touches
    public static class Keys {
        public static int MOVE_LEFT = Input.Keys.LEFT;
        public static int MOVE_RIGHT = Input.Keys.RIGHT;
        public static int JUMP = Input.Keys.SPACE;
        public static int JUMP_ALT = Input.Keys.UP;
        public static int JUMP_ALT2 = Input.Keys.Z;
        public static int CROUCH = Input.Keys.DOWN;
        public static int RUN = Input.Keys.SHIFT_LEFT;
        public static int RUN_ALT = Input.Keys.SHIFT_RIGHT;
        public static int DEBUG = Input.Keys.F1;
        public static int PAUSE = Input.Keys.ESCAPE;
    }
    
    // Configuration du joueur
    public static class Player {
        public static final float WIDTH = 0.8f; // Largeur en mètres
        public static final float HEIGHT = 1.8f; // Hauteur en mètres
        public static final float DENSITY = 1f;
        public static final float FRICTION = 0.6f;
        public static final float RESTITUTION = 0.1f;
        public static final float MAX_SPEED = 4f;
        public static final float JUMP_FORCE = 12f;
        public static final float ACCELERATION = 20f;
        public static final float DECELERATION = 0.9f;
        public static final float GRAVITY_SCALE = 1f;
        public static final int MAX_JUMPS = 2;
        public static final float COYOTE_TIME = 0.1f;
        public static final float JUMP_BUFFER_TIME = 0.1f;
    }
    
    // Configuration du monde
    public static class World {
        public static final float GRAVITY = -30f;
        public static final float PPM = 32f; // Pixels Per Meter
        public static final float V_WIDTH = 16f; // Largeur virtuelle en mètres
        public static final float V_HEIGHT = 9f; // Hauteur virtuelle en mètres
        public static final int VELOCITY_ITERATIONS = 6;
        public static final int POSITION_ITERATIONS = 2;
        public static final float TIME_STEP = 1/60f;
    }
    
    // Configuration de la caméra
    public static class Camera {
        public static final float FOLLOW_SPEED = 0.1f;
        public static final float ZOOM = 1f;
        public static final float MIN_ZOOM = 0.5f;
        public static final float MAX_ZOOM = 2f;
        public static final float ZOOM_SPEED = 0.1f;
    }
    
    // Configuration des calques de collision
    public static class CollisionLayers {
        public static final short NOTHING_BIT = 0;
        public static final short GROUND_BIT = 1;
        public static final short PLAYER_BIT = 2;
        public static final short BRICK_BIT = 4;
        public static final short COIN_BIT = 8;
        public static final short DESTROYED_BIT = 16;
        public static final short OBJECT_BIT = 32;
        public static final short ENEMY_BIT = 64;
        public static final short ENEMY_HEAD_BIT = 128;
        public static final short ITEM_BIT = 256;
        public static final short PLAYER_HEAD_BIT = 512;
        
        // Masques de collision
        public static final short PLAYER_MASK = GROUND_BIT | BRICK_BIT | COIN_BIT | ENEMY_BIT | ITEM_BIT | OBJECT_BIT;
        public static final short ENEMY_MASK = GROUND_BIT | PLAYER_BIT | BRICK_BIT | OBJECT_BIT | ENEMY_BIT;
        public static final short ITEM_MASK = PLAYER_BIT | GROUND_BIT;
        public static final short GROUND_MASK = PLAYER_BIT | ENEMY_BIT | ITEM_BIT | OBJECT_BIT;
    }
    
    // Configuration des effets
    public static class Effects {
        public static final float INVINCIBILITY_DURATION = 3f;
        public static final float DAMAGE_KNOCKBACK_X = 5f;
        public static final float DAMAGE_KNOCKBACK_Y = 10f;
        public static final float STAR_MAN_DURATION = 10f;
    }
    
    // Configuration du HUD
    public static class HUD {
        public static final float PADDING = 10f;
        public static final float FONT_SCALE = 0.5f;
        public static final String FONT = "fonts/mario-font.fnt";
    }
    
    // Configuration des chemins des ressources
    public static class AssetPaths {
        public static final String TEXTURES = "textures/textures.atlas";
        public static final String UI_SKIN = "ui/uiskin.json";
        public static final String SOUNDS = "sounds/sounds.pack";
        public static final String MUSIC = "music/music.pack";
        public static final String PARTICLES = "particles/particles.pack";
    }
    
    // Configuration des niveaux
    public static class Level {
        public static final String PATH = "levels/";
        public static final String EXTENSION = ".tmx";
        public static final String[] LEVELS = {"1-1", "1-2", "1-3", "1-4"};
    }
    
    // Configuration des scores
    public static class Score {
        public static final int COIN = 100;
        public static final int BRICK = 50;
        public static final int ENEMY = 200;
        public static final int POWERUP = 1000;
        public static final int TIME_BONUS = 50;
        public static final int ONE_UP = 1;
    }
}
