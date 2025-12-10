package com.mariogame.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.mariogame.MarioGame;
import com.mariogame.managers.GameManager;
import com.mariogame.utils.Constants;

/**
 * Écran de transition entre les niveaux avec des effets visuels.
 */
public class LevelTransitionScreen implements Screen {
    private final MarioGame game;
    private final GameManager gameManager;
    
    // Caméra et vue
    private OrthographicCamera camera;
    private Viewport viewport;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    
    // Paramètres de transition
    private final String nextLevel;
    private final Vector2 spawnPosition;
    private final boolean isWarpTransition;
    private float transitionTimer = 0f;
    private final float TRANSITION_DURATION = 1.5f;
    private boolean transitionIn = true;
    private boolean transitionComplete = false;
    
    // Effets visuels
    private Color transitionColor = new Color(0f, 0f, 0f, 0f);
    private float zoomEffect = 1f;
    private float rotationEffect = 0f;
    
    public LevelTransitionScreen(MarioGame game, String nextLevel, Vector2 spawnPosition, boolean isWarpTransition) {
        this.game = game;
        this.gameManager = GameManager.getInstance();
        this.nextLevel = nextLevel;
        this.spawnPosition = spawnPosition;
        this.isWarpTransition = isWarpTransition;
        
        init();
    }
    
    private void init() {
        // Initialiser la caméra et la vue
        camera = new OrthographicCamera();
        viewport = new FitViewport(Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT, camera);
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        
        // Configurer la caméra
        camera.position.set(Constants.VIRTUAL_WIDTH / 2f, Constants.VIRTUAL_HEIGHT / 2f, 0);
        camera.update();
        
        // Initialiser les effets en fonction du type de transition
        if (isWarpTransition) {
            // Effet de zoom pour la téléportation
            zoomEffect = 0.5f;
            rotationEffect = 0f;
        } else {
            // Effet de fondu pour les transitions normales
            zoomEffect = 1f;
            rotationEffect = 0f;
        }
    }
    
    @Override
    public void show() {
        // Jouer un son de transition
        // game.getSoundManager().playSound("pipe", 0.5f);
    }
    
    @Override
    public void render(float delta) {
        // Mettre à jour la logique de transition
        update(delta);
        
        // Effacer l'écran
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        // Rendu de la transition
        renderTransition();
    }
    
    private void update(float delta) {
        // Mettre à jour le timer de transition
        transitionTimer += delta;
        
        // Calculer la progression de la transition (0.0 à 1.0)
        float progress = Math.min(transitionTimer / (TRANSITION_DURATION / 2), 1.0f);
        
        if (transitionIn) {
            // Phase d'entrée (fondu au noir)
            transitionColor.a = progress;
            
            // Effets spéciaux pour la téléportation
            if (isWarpTransition) {
                zoomEffect = MathUtils.lerp(1f, 0.1f, progress);
                rotationEffect = progress * 360f;
            }
            
            // Passer à la phase de sortie si la moitié du temps est écoulée
            if (transitionTimer >= TRANSITION_DURATION / 2 && !transitionComplete) {
                transitionComplete = true;
                
                // Charger le prochain niveau
                gameManager.changeLevel(nextLevel, spawnPosition);
                
                // Réinitialiser pour la sortie
                transitionTimer = 0f;
                transitionIn = false;
            }
        } else {
            // Phase de sortie (retour à la normale)
            transitionColor.a = 1.0f - progress;
            
            // Effets spéciaux pour la téléportation
            if (isWarpTransition) {
                zoomEffect = MathUtils.lerp(0.1f, 1f, progress);
                rotationEffect = 360f - (progress * 360f);
            }
            
            // Terminer la transition
            if (transitionTimer >= TRANSITION_DURATION / 2) {
                // Revenir à l'écran de jeu
                game.setScreen(game.getGameScreen());
            }
        }
    }
    
    private void renderTransition() {
        // Définir la matrice de projection
        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);
        
        // Rendu de l'effet de transition
        if (isWarpTransition) {
            // Effet de spirale pour la téléportation
            renderSpiralEffect();
        } else {
            // Simple fondu au noir pour les transitions normales
            renderFadeEffect();
        }
    }
    
    private void renderFadeEffect() {
        // Rendu d'un simple fondu
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(transitionColor);
        shapeRenderer.rect(0, 0, Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT);
        shapeRenderer.end();
        
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }
    
    private void renderSpiralEffect() {
        // Rendu d'un effet de spirale pour la téléportation
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        
        // Sauvegarder la matrice de projection
        batch.begin();
        batch.setColor(0, 0, 0, transitionColor.a);
        batch.draw(
            null, 
            0, 0, 
            Constants.VIRTUAL_WIDTH / 2f, Constants.VIRTUAL_HEIGHT / 2f, 
            Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT, 
            zoomEffect, zoomEffect, 
            rotationEffect
        );
        batch.end();
        
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }
    
    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
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
        // Nettoyer les ressources si nécessaire
    }
    
    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
    }
    
    // Méthodes utilitaires
    
    /**
     * Crée une transition de niveau standard.
     */
    public static void createTransition(MarioGame game, String nextLevel, Vector2 spawnPosition) {
        game.setScreen(new LevelTransitionScreen(game, nextLevel, spawnPosition, false));
    }
    
    /**
     * Crée une transition de téléportation (avec des effets spéciaux).
     */
    public static void createWarpTransition(MarioGame game, String nextLevel, Vector2 spawnPosition) {
        game.setScreen(new LevelTransitionScreen(game, nextLevel, spawnPosition, true));
    }
}
