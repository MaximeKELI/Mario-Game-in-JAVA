package com.mariogame.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.mariogame.MarioGame;

/**
 * Classe de base abstraite pour tous les écrans du jeu.
 * Fournit des fonctionnalités communes à tous les écrans.
 */
public abstract class AbstractScreen implements Screen {
    protected final MarioGame game;
    protected final SpriteBatch batch;
    protected Stage stage;
    protected boolean initialized = false;
    protected boolean active = false;
    protected float stateTime = 0f;
    
    public AbstractScreen(MarioGame game) {
        this.game = game;
        this.batch = game.getSpriteBatch();
    }
    
    /**
     * Initialise l'écran. Cette méthode est appelée une seule fois avant le premier rendu.
     */
    public void initialize() {
        if (initialized) {
            return;
        }
        
        // Initialisation de la scène
        stage = new Stage(getViewport(), batch);
        
        // Configuration de l'écran
        setupScreen();
        
        // Marquage comme initialisé
        initialized = true;
        Gdx.app.log(getClass().getSimpleName(), "Initialized");
    }
    
    /**
     * Méthode à implémenter par les sous-classes pour configurer l'écran.
     */
    protected abstract void setupScreen();
    
    /**
     * Obtient le viewport à utiliser pour cet écran.
     * Peut être redéfini par les sous-classes pour utiliser un type de viewport différent.
     * @return Le viewport à utiliser
     */
    protected Viewport getViewport() {
        return game.getScreenManager().getViewport();
    }
    
    @Override
    public void show() {
        // Par défaut, on initialise l'écran s'il ne l'est pas déjà
        if (!initialized) {
            initialize();
        }
        
        // Définir le processeur d'entrées
        Gdx.input.setInputProcessor(stage);
        
        // Réinitialiser le temps d'état
        stateTime = 0f;
    }
    
    @Override
    public void render(float delta) {
        // Mettre à jour le temps d'état
        stateTime += delta;
        
        // Effacer l'écran
        clearScreen();
        
        // Mettre à jour la scène
        update(delta);
        
        // Rendu de la scène
        stage.act(delta);
        stage.draw();
    }
    
    /**
     * Efface l'écran avec une couleur de fond par défaut.
     * Peut être redéfini par les sous-classes pour utiliser une couleur différente.
     */
    protected void clearScreen() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(Gdx.gl.GL_COLOR_BUFFER_BIT);
    }
    
    /**
     * Met à jour la logique de l'écran.
     * @param delta Le temps écoulé depuis la dernière mise à jour en secondes
     */
    protected void update(float delta) {
        // À implémenter par les sous-classes si nécessaire
    }
    
    /**
     * Appelé lorsque l'écran est masqué.
     * @param callback Callback à appeler une fois l'animation de masquage terminée
     */
    public void hide(Runnable callback) {
        // Par défaut, on appelle immédiatement le callback
        if (callback != null) {
            callback.run();
        }
    }
    
    @Override
    public void hide() {
        // Ne rien faire par défaut
    }
    
    @Override
    public void resize(int width, int height) {
        // Mettre à jour le viewport de la scène
        if (stage != null) {
            stage.getViewport().update(width, height, true);
        }
    }
    
    @Override
    public void pause() {
        // À implémenter par les sous-classes si nécessaire
    }
    
    @Override
    public void resume() {
        // À implémenter par les sous-classes si nécessaire
    }
    
    @Override
    public void dispose() {
        Gdx.app.log(getClass().getSimpleName(), "Disposing");
        
        // Libérer les ressources de la scène
        if (stage != null) {
            stage.dispose();
            stage = null;
        }
        
        // Réinitialiser les états
        initialized = false;
        active = false;
    }
    
    // Getters et Setters
    
    public boolean isInitialized() {
        return initialized;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    public float getStateTime() {
        return stateTime;
    }
}
