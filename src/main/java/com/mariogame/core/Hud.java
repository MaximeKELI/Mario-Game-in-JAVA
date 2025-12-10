package com.mariogame.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class Hud implements Disposable {
    // Scène2D.ui Stage et ses propres Viewport pour le HUD
    public Stage stage;
    private Viewport viewport;
    
    // Informations à suivre
    private Integer score;
    private Integer coins;
    private Integer lives;
    private float timeCount;
    
    // Éléments d'interface utilisateur
    private Label scoreLabel;
    private Label coinsLabel;
    private Label livesLabel;
    private Label timeLabel;
    private Label countdownLabel;
    private Label levelLabel;
    private Label worldLabel;
    private Label marioLabel;
    
    public Hud(SpriteBatch sb) {
        // Définir le score à 0
        score = 0;
        coins = 0;
        lives = 3;
        timeCount = 300; // 5 minutes en secondes
        
        // Configurer le viewport du HUD
        viewport = new FitViewport(MarioGame.V_WIDTH * 10, MarioGame.V_HEIGHT * 10, new OrthographicCamera());
        stage = new Stage(viewport, sb);
        
        // Table pour organiser les éléments du HUD
        Table table = new Table();
        // Placer la table en haut de l'écran
        table.top();
        // Rendre la table de la taille de l'écran
        table.setFillParent(true);
        
        // Créer les labels avec le style par défaut
        BitmapFont font = new BitmapFont();
        font.getData().setScale(0.5f);
        
        // Créer les labels
        countdownLabel = new Label(String.format("%03d", (int)timeCount), 
            new Label.LabelStyle(font, Color.WHITE));
        scoreLabel = new Label(String.format("%06d", score), 
            new Label.LabelStyle(font, Color.WHITE));
        coinsLabel = new Label(String.format("%02d", coins), 
            new Label.LabelStyle(font, Color.WHITE));
        livesLabel = new Label(String.format("%01d", lives), 
            new Label.LabelStyle(font, Color.WHITE));
        
        // Créer les labels de texte statique
        Label timeTextLabel = new Label("TIME", new Label.LabelStyle(font, Color.WHITE));
        Label levelTextLabel = new Label("1-1", new Label.LabelStyle(font, Color.WHITE));
        Label worldTextLabel = new Label("WORLD", new Label.LabelStyle(font, Color.WHITE));
        Label marioTextLabel = new Label("MARIO", new Label.LabelStyle(font, Color.WHITE));
        
        // Ajouter les labels à la table
        // Première rangée
        table.add(marioTextLabel).expandX().padTop(10);
        table.add(worldTextLabel).expandX().padTop(10);
        table.add(timeTextLabel).expandX().padTop(10);
        table.row();
        
        // Deuxième rangée (valeurs)
        table.add(scoreLabel).expandX();
        table.add(levelTextLabel).expandX();
        table.add(countdownLabel).expandX();
        
        // Ajouter la table au stage
        stage.addActor(table);
    }
    
    public void update(float dt) {
        // Mettre à jour le chronomètre
        timeCount -= dt;
        if (timeCount < 0) timeCount = 0;
        countdownLabel.setText(String.format("%03d", (int)timeCount));
        
        // Mettre à jour les autres informations
        scoreLabel.setText(String.format("%06d", score));
        coinsLabel.setText(String.format("%02d", coins));
        livesLabel.setText(String.format("%01d", lives));
    }
    
    public void addScore(int value) {
        score += value;
    }
    
    public void addCoin() {
        coins++;
        // Pour chaque 100 pièces, gagner une vie
        if (coins >= 100) {
            coins = 0;
            lives++;
        }
    }
    
    public void loseLife() {
        lives--;
    }
    
    public int getLives() {
        return lives;
    }
    
    public void render() {
        stage.draw();
    }
    
    @Override
    public void dispose() {
        stage.dispose();
    }
}
