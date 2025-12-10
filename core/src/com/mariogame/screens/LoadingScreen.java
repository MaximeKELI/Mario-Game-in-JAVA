package com.mariogame.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.mariogame.MarioGame;
import com.mariogame.core.AssetLoader;
import com.mariogame.screens.MenuScreen;

/**
 * Écran de chargement qui gère le chargement asynchrone des ressources.
 */
public class LoadingScreen implements Screen {
    private final MarioGame game;
    private final SpriteBatch batch;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final BitmapFont font;
    
    private final AssetLoader assetLoader;
    private boolean assetsLoaded = false;
    
    public LoadingScreen(MarioGame game) {
        this.game = game;
        this.assetLoader = AssetLoader.getInstance();
        
        // Initialisation du batch et de la caméra
        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        viewport = new FitViewport(800, 480, camera);
        
        // Chargement de la police (utilise une police par défaut pour l'écran de chargement)
        font = new BitmapFont();
        font.getData().setScale(2);
        
        // Démarrer le chargement des ressources
        assetLoader.loadAll();
    }
    
    @Override
    public void show() {
        // Appelé quand cet écran devient l'écran actuel
        Gdx.app.log("LoadingScreen", "show() called");
    }
    
    @Override
    public void render(float delta) {
        // Effacer l'écran
        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        // Mise à jour du chargement
        if (!assetsLoaded) {
            // Mettre à jour le chargement
            if (assetLoader.update()) {
                // Le chargement est terminé
                assetsLoaded = true;
                // Passer à l'écran du menu
                game.setScreen(new MenuScreen(game));
                return;
            }
        }
        
        // Rendu de l'écran de chargement
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        
        batch.begin();
        
        // Afficher la progression
        float progress = assetLoader.getProgress();
        String text = String.format("Chargement... %.0f%%", progress);
        
        font.draw(
            batch, 
            text, 
            -viewport.getWorldWidth() / 2, 
            0, 
            viewport.getWorldWidth(), 
            Align.center, 
            true
        );
        
        batch.end();
    }
    
    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        camera.position.set(0, 0, 0);
    }
    
    @Override
    public void pause() {
        // Ne rien faire
    }
    
    @Override
    public void resume() {
        // Ne rien faire
    }
    
    @Override
    public void hide() {
        // Ne rien faire
    }
    
    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        Gdx.app.log("LoadingScreen", "dispose() called");
    }
}
