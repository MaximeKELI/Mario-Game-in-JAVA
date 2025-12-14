package com.mariogame.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.Disposable;
import com.mariogame.utils.Constants;

/**
 * Gestionnaire professionnel de sauvegarde et chargement des données de jeu.
 * Gère la persistance des scores, niveaux débloqués, et préférences.
 */
public class SaveManager implements Disposable {
    private static final String PREFS_NAME = "mario_game_save";
    private final Preferences prefs;
    
    // Clés de sauvegarde
    private static final String KEY_HIGH_SCORE = "high_score";
    private static final String KEY_TOTAL_COINS = "total_coins";
    private static final String KEY_UNLOCKED_LEVELS = "unlocked_levels";
    private static final String KEY_CURRENT_WORLD = "current_world";
    private static final String KEY_CURRENT_LEVEL = "current_level";
    private static final String KEY_PLAYER_LIVES = "player_lives";
    private static final String KEY_PLAYER_SCORE = "player_score";
    
    public SaveManager() {
        this.prefs = Gdx.app.getPreferences(PREFS_NAME);
    }
    
    /**
     * Sauvegarde le meilleur score.
     */
    public void saveHighScore(int score) {
        int currentHighScore = getHighScore();
        if (score > currentHighScore) {
            prefs.putInteger(KEY_HIGH_SCORE, score);
            prefs.flush();
        }
    }
    
    /**
     * Récupère le meilleur score.
     */
    public int getHighScore() {
        return prefs.getInteger(KEY_HIGH_SCORE, 0);
    }
    
    /**
     * Sauvegarde le total de pièces collectées.
     */
    public void saveTotalCoins(int coins) {
        prefs.putInteger(KEY_TOTAL_COINS, coins);
        prefs.flush();
    }
    
    /**
     * Récupère le total de pièces.
     */
    public int getTotalCoins() {
        return prefs.getInteger(KEY_TOTAL_COINS, 0);
    }
    
    /**
     * Ajoute un niveau débloqué.
     */
    public void unlockLevel(int world, int level) {
        String levelKey = world + "-" + level;
        String unlocked = prefs.getString(KEY_UNLOCKED_LEVELS, "1-1");
        
        if (!unlocked.contains(levelKey)) {
            unlocked += "," + levelKey;
            prefs.putString(KEY_UNLOCKED_LEVELS, unlocked);
            prefs.flush();
        }
    }
    
    /**
     * Vérifie si un niveau est débloqué.
     */
    public boolean isLevelUnlocked(int world, int level) {
        String levelKey = world + "-" + level;
        String unlocked = prefs.getString(KEY_UNLOCKED_LEVELS, "1-1");
        return unlocked.contains(levelKey);
    }
    
    /**
     * Récupère tous les niveaux débloqués.
     */
    public String getUnlockedLevels() {
        return prefs.getString(KEY_UNLOCKED_LEVELS, "1-1");
    }
    
    /**
     * Sauvegarde la progression actuelle du joueur.
     */
    public void saveProgress(int world, int level, int lives, int score) {
        prefs.putInteger(KEY_CURRENT_WORLD, world);
        prefs.putInteger(KEY_CURRENT_LEVEL, level);
        prefs.putInteger(KEY_PLAYER_LIVES, lives);
        prefs.putInteger(KEY_PLAYER_SCORE, score);
        prefs.flush();
    }
    
    /**
     * Récupère le monde actuel sauvegardé.
     */
    public int getCurrentWorld() {
        return prefs.getInteger(KEY_CURRENT_WORLD, 1);
    }
    
    /**
     * Récupère le niveau actuel sauvegardé.
     */
    public int getCurrentLevel() {
        return prefs.getInteger(KEY_CURRENT_LEVEL, 1);
    }
    
    /**
     * Récupère les vies sauvegardées.
     */
    public int getPlayerLives() {
        return prefs.getInteger(KEY_PLAYER_LIVES, Constants.PlayerConfig.STARTING_LIVES);
    }
    
    /**
     * Récupère le score sauvegardé.
     */
    public int getPlayerScore() {
        return prefs.getInteger(KEY_PLAYER_SCORE, 0);
    }
    
    /**
     * Efface toutes les sauvegardes.
     */
    public void clearAll() {
        prefs.clear();
        prefs.flush();
    }
    
    /**
     * Efface uniquement la progression (garde les scores).
     */
    public void clearProgress() {
        prefs.remove(KEY_CURRENT_WORLD);
        prefs.remove(KEY_CURRENT_LEVEL);
        prefs.remove(KEY_PLAYER_LIVES);
        prefs.remove(KEY_PLAYER_SCORE);
        prefs.flush();
    }
    
    @Override
    public void dispose() {
        // Les préférences sont automatiquement sauvegardées
        // Pas besoin de libération de ressources
    }
}

