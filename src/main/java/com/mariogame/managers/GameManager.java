package com.mariogame.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.mariogame.utils.Constants;

/**
 * Gère l'état global du jeu, y compris le score, les vies et la progression.
 */
public class GameManager implements Disposable {
    private static final String PREFS_NAME = "mario_game";
    private static final String HIGH_SCORE_KEY = "high_score";
    private static final String COINS_KEY = "total_coins";
    private static final String UNLOCKED_LEVELS_KEY = "unlocked_levels";
    
    private static GameManager instance;
    
    // Données de jeu
    private int score;
    private int coins;
    private int lives;
    private int world;
    private int level;
    private int highScore;
    private int totalCoins;
    private Array<Integer> unlockedLevels;
    
    // Références
    private final Preferences prefs;
    
    private GameManager() {
        prefs = Gdx.app.getPreferences(PREFS_NAME);
        load();
    }
    
    public static synchronized GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }
    
    /**
     * Charge les données sauvegardées.
     */
    private void load() {
        highScore = prefs.getInteger(HIGH_SCORE_KEY, 0);
        totalCoins = prefs.getInteger(COINS_KEY, 0);
        
        // Charger les niveaux débloqués
        String[] unlockedLevelsStr = prefs.getString(UNLOCKED_LEVELS_KEY, "1-1").split(",");
        unlockedLevels = new Array<>();
        for (String level : unlockedLevelsStr) {
            try {
                unlockedLevels.add(Integer.parseInt(level.replace("-", "")));
            } catch (NumberFormatException e) {
                Gdx.app.error("GameManager", "Erreur lors du chargement des niveaux débloqués", e);
            }
        }
        
        // S'assurer que le premier niveau est toujours débloqué
        if (!unlockedLevels.contains(11, false)) { // 1-1 = 11
            unlockedLevels.add(11);
            saveUnlockedLevels();
        }
    }
    
    /**
     * Sauvegarde les données du jeu.
     */
    public void save() {
        // Mettre à jour le meilleur score si nécessaire
        if (score > highScore) {
            highScore = score;
            prefs.putInteger(HIGH_SCORE_KEY, highScore);
        }
        
        // Mettre à jour le total de pièces
        totalCoins += coins;
        prefs.putInteger(COINS_KEY, totalCoins);
        
        // Sauvegarder les préférences
        prefs.flush();
    }
    
    /**
     * Sauvegarde la liste des niveaux débloqués.
     */
    private void saveUnlockedLevels() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < unlockedLevels.size; i++) {
            if (i > 0) sb.append(",");
            int level = unlockedLevels.get(i);
            sb.append(level / 10).append("-").append(level % 10);
        }
        prefs.putString(UNLOCKED_LEVELS_KEY, sb.toString());
        prefs.flush();
    }
    
    /**
     * Réinitialise les données de la partie en cours.
     */
    public void resetGame() {
        score = 0;
        coins = 0;
        lives = Constants.PlayerConfig.STARTING_LIVES;
        world = 1;
        level = 1;
    }
    
    /**
     * Passe au niveau suivant.
     * @return true si un nouveau niveau a été chargé, false si c'était le dernier niveau
     */
    public boolean nextLevel() {
        level++;
        
        // Vérifier si on doit passer au monde suivant
        if (level > Constants.LevelConfig.LEVELS_PER_WORLD) {
            level = 1;
            world++;
            
            // Vérifier si c'est la fin du jeu
            if (world > Constants.LevelConfig.TOTAL_WORLDS) {
                return false;
            }
        }
        
        // Débloquer le niveau suivant s'il ne l'était pas déjà
        int nextLevelId = world * 10 + level;
        if (!unlockedLevels.contains(nextLevelId, false)) {
            unlockedLevels.add(nextLevelId);
            saveUnlockedLevels();
        }
        
        return true;
    }
    
    /**
     * Vérifie si un niveau est débloqué.
     */
    public boolean isLevelUnlocked(int world, int level) {
        int levelId = world * 10 + level;
        return unlockedLevels.contains(levelId, false);
    }
    
    // Méthodes pour gérer le score et les vies
    
    public void addScore(int points) {
        score += points;
        
        // Vérifier si le joueur gagne une vie supplémentaire
        if (score / 10000 > (score - points) / 10000) {
            addLife();
        }
    }
    
    public void addCoin() {
        coins++;
        totalCoins++;
        
        // Toutes les 100 pièces, gagner une vie
        if (coins % 100 == 0) {
            addLife();
            coins = 0; // Réinitialiser le compteur de pièces
        }
    }
    
    public void addLife() {
        lives++;
    }
    
    public boolean removeLife() {
        lives--;
        return lives <= 0;
    }
    
    public void setLevel(int world, int level) {
        this.world = world;
        this.level = level;
    }
    
    // Getters et setters
    
    public int getScore() {
        return score;
    }
    
    public int getCoins() {
        return coins;
    }
    
    public int getLives() {
        return lives;
    }
    
    public int getWorld() {
        return world;
    }
    
    public int getLevel() {
        return level;
    }
    
    public int getHighScore() {
        return highScore;
    }
    
    public int getTotalCoins() {
        return totalCoins;
    }
    
    public Array<Integer> getUnlockedLevels() {
        return unlockedLevels;
    }
    
    @Override
    public void dispose() {
        save();
    }
    
    /**
     * Réinitialise toutes les données du jeu (pour les paramètres ou en cas de réinitialisation complète).
     */
    public static void resetAllData() {
        Gdx.app.getPreferences(PREFS_NAME).clear().flush();
        if (instance != null) {
            instance.load();
        }
    }
}
