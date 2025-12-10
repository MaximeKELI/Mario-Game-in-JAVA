package com.mariogame.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.mariogame.utils.Constants;

/**
 * Classe gérant l'interface utilisateur du jeu (score, vies, pièces, etc.)
 */
public class HUD implements Disposable {
    // Référence vers le stage pour l'interface utilisateur
    public Stage stage;
    private Viewport viewport;
    
    // Données du jeu
    private Integer score;
    private Integer coins;
    private Integer lives;
    private Integer world;
    private Integer level;
    private Float timeLeft;
    
    // Éléments d'interface
    private Label scoreLabel;
    private Label coinsLabel;
    private Label livesLabel;
    private Label worldLabel;
    private Label levelLabel;
    private Label timeLabel;
    private Label scoreTextLabel;
    private Label coinsTextLabel;
    private Label livesTextLabel;
    private Label worldTextLabel;
    private Label timeTextLabel;
    
    // Police de caractères
    private BitmapFont font;
    
    public HUD(SpriteBatch sb) {
        // Initialiser les données du jeu
        score = 0;
        coins = 0;
        lives = 3;
        world = 1;
        level = 1;
        timeLeft = 300f; // 5 minutes en secondes
        
        // Créer la vue pour le HUD (taille fixe)
        viewport = new FitViewport(Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT, new OrthographicCamera());
        stage = new Stage(viewport, sb);
        
        // Créer une table pour organiser les éléments du HUD
        Table table = new Table();
        table.top(); // Aligner en haut de l'écran
        table.setFillParent(true); // Remplir tout l'écran
        
        // Charger la police (à remplacer par une police personnalisée plus tard)
        font = new BitmapFont(Gdx.files.internal("fonts/mario.fnt"));
        font.getData().setScale(0.5f); // Ajuster la taille de la police
        
        // Créer les étiquettes
        scoreTextLabel = new Label("SCORE", new Label.LabelStyle(font, Color.WHITE));
        scoreLabel = new Label(String.format("%06d", score), new Label.LabelStyle(font, Color.WHITE));
        
        coinsTextLabel = new Label("COINS", new Label.LabelStyle(font, Color.WHITE));
        coinsLabel = new Label(String.format("%02d", coins), new Label.LabelStyle(font, Color.WHITE));
        
        livesTextLabel = new Label("LIVES", new Label.LabelStyle(font, Color.WHITE));
        livesLabel = new Label(String.format("%02d", lives), new Label.LabelStyle(font, Color.WHITE));
        
        worldTextLabel = new Label("WORLD", new Label.LabelStyle(font, Color.WHITE));
        worldLabel = new Label(String.format("%d-%d", world, level), new Label.LabelStyle(font, Color.WHITE));
        
        timeTextLabel = new Label("TIME", new Label.LabelStyle(font, Color.WHITE));
        timeLabel = new Label(String.format("%03d", timeLeft.intValue()), new Label.LabelStyle(font, Color.WHITE));
        
        // Ajouter les éléments à la table
        // Première ligne : SCORE et COINS
        table.add(scoreTextLabel).expandX().padTop(10);
        table.add(coinsTextLabel).expandX().padTop(10);
        table.add(worldTextLabel).expandX().padTop(10);
        table.add(timeTextLabel).expandX().padTop(10);
        table.row();
        
        // Deuxième ligne : valeurs
        table.add(scoreLabel).expandX();
        table.add(coinsLabel).expandX();
        table.add(worldLabel).expandX();
        table.add(timeLabel).expandX();
        
        // Ajouter la table au stage
        stage.addActor(table);
    }
    
    /**
     * Met à jour le HUD à chaque frame.
     * @param deltaTime Temps écoulé depuis la dernière frame
     */
    public void update(float deltaTime) {
        // Mettre à jour le temps restant
        if (timeLeft > 0) {
            timeLeft -= deltaTime;
            timeLabel.setText(String.format("%03d", Math.max(0, timeLeft.intValue())));
        }
    }
    
    /**
     * Ajoute des points au score.
     * @param value Nombre de points à ajouter
     */
    public void addScore(int value) {
        score += value;
        scoreLabel.setText(String.format("%06d", score));
    }
    
    /**
     * Ajoute une pièce au compteur.
     */
    public void addCoin() {
        coins++;
        coinsLabel.setText(String.format("%02d", coins));
        
        // Toutes les 100 pièces, gagner une vie
        if (coins % 100 == 0) {
            addLife();
        }
    }
    
    /**
     * Ajoute une vie au joueur.
     */
    public void addLife() {
        lives++;
        livesLabel.setText(String.format("%02d", lives));
    }
    
    /**
     * Retire une vie au joueur.
     * @return true si le joueur n'a plus de vies
     */
    public boolean removeLife() {
        lives--;
        livesLabel.setText(String.format("%02d", lives));
        return lives <= 0;
    }
    
    /**
     * Définit le monde et le niveau actuels.
     */
    public void setWorldLevel(int world, int level) {
        this.world = world;
        this.level = level;
        worldLabel.setText(String.format("%d-%d", world, level));
    }
    
    /**
     * Affiche un message à l'écran.
     * @param message Le message à afficher
     * @param duration Durée d'affichage en secondes
     */
    public void showMessage(String message, float duration) {
        // À implémenter : afficher un message temporaire à l'écran
    }
    
    /**
     * Redimensionne le HUD lorsque la fenêtre est redimensionnée.
     * @param width Nouvelle largeur
     * @param height Nouvelle hauteur
     */
    public void resize(int width, int height) {
        viewport.update(width, height);
    }
    
    @Override
    public void dispose() {
        stage.dispose();
        font.dispose();
    }
    
    // Getters
    
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
    
    public float getTimeLeft() {
        return timeLeft;
    }
}
