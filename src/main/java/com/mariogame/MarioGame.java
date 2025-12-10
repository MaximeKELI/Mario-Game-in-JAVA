package com.mariogame;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.mariogame.managers.*;
import com.mariogame.screens.ScreenType;
import com.mariogame.screens.ScreenManager;
import com.mariogame.utils.Constants;

/**
 * Classe principale du jeu Mario.
 * Gère l'initialisation du jeu et les composants principaux.
 */
public class MarioGame extends Game {
    // Gestionnaires
    private AssetManager assetManager;
    private ScreenManager screenManager;
    private InputManager inputManager;
    private AudioManager audioManager;
    private SaveManager saveManager;
    
    // Composants de rendu
    private SpriteBatch spriteBatch;
    private ShapeRenderer shapeRenderer;
    
    // Configuration
    private GameConfig config;
    
    @Override
    public void create() {
        // Initialisation des gestionnaires
        initManagers();
        
        // Chargement des ressources
        loadAssets();
        
        // Configuration initiale
        setupGame();
    }
    
    private void initManagers() {
        // Initialisation du gestionnaire d'assets
        assetManager = new AssetManager();
        
        // Initialisation du gestionnaire d'écrans
        screenManager = new ScreenManager(this);
        
        // Initialisation du gestionnaire d'entrées
        inputManager = new InputManager(this);
        Gdx.input.setInputProcessor(inputManager);
        
        // Initialisation du gestionnaire audio
        audioManager = new AudioManager(assetManager);
        
        // Initialisation du gestionnaire de sauvegardes
        saveManager = new SaveManager();
        
        // Initialisation des composants de rendu
        spriteBatch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        
        // Configuration du jeu
        config = new GameConfig();
    }
    
    private void loadAssets() {
        // Chargement des ressources de base nécessaires pour l'écran de chargement
        // Les autres ressources seront chargées de manière asynchrone
        AssetLoader.loadEssentialAssets(assetManager);
    }
    
    private void setupGame() {
        // Configuration des paramètres graphiques
        Gdx.graphics.setVSync(config.isVSyncEnabled());
        
        // Démarrage avec l'écran de chargement
        screenManager.setScreen(ScreenType.LOADING);
    }
    
    @Override
    public void render() {
        // Délégation du rendu au gestionnaire d'écrans
        screenManager.render(Gdx.graphics.getDeltaTime());
    }
    
    @Override
    public void resize(int width, int height) {
        screenManager.resize(width, height);
    }
    
    @Override
    public void pause() {
        screenManager.pause();
    }
    
    @Override
    public void resume() {
        screenManager.resume();
    }
    
    @Override
    public void dispose() {
        // Libération des ressources
        screenManager.dispose();
        assetManager.dispose();
        spriteBatch.dispose();
        shapeRenderer.dispose();
        audioManager.dispose();
    }
    
    // Getters
    public AssetManager getAssetManager() {
        return assetManager;
    }
    
    public ScreenManager getScreenManager() {
        return screenManager;
    }
    
    public InputManager getInputManager() {
        return inputManager;
    }
    
    public AudioManager getAudioManager() {
        return audioManager;
    }
    
    public SaveManager getSaveManager() {
        return saveManager;
    }
    
    public SpriteBatch getSpriteBatch() {
        return spriteBatch;
    }
    
    public ShapeRenderer getShapeRenderer() {
        return shapeRenderer;
    }
    
    public GameConfig getConfig() {
        return config;
    }
}
