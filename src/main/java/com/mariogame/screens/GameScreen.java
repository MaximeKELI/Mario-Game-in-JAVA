package com.mariogame.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.mariogame.MarioGame;
import com.mariogame.core.AssetLoader;
import com.mariogame.utils.Constants;
import com.mariogame.world.GameWorld;

/**
 * Écran principal du jeu qui gère le rendu et la logique du gameplay.
 */
public class GameScreen implements Screen {
    private final MarioGame game;
    private final AssetLoader assetLoader;
    
    // Caméra et vue
    private OrthographicCamera gameCamera;
    private Viewport gameViewport;
    private OrthogonalTiledMapRenderer mapRenderer;
    
    // Monde de jeu
    private GameWorld gameWorld;
    private Box2DDebugRenderer debugRenderer;
    
    // Rendu
    private SpriteBatch batch;
    private Stage stage;
    private boolean debugMode = false;
    
    public GameScreen(MarioGame game) {
        this.game = game;
        this.assetLoader = AssetLoader.getInstance();
        
        // Initialisation de la caméra et de la vue
        gameCamera = new OrthographicCamera();
        gameViewport = new FitViewport(
            Constants.WorldConfig.V_WIDTH * Constants.WorldConfig.PPM, 
            Constants.WorldConfig.V_HEIGHT * Constants.WorldConfig.PPM, 
            gameCamera
        );
        
        // Initialisation du debug renderer
        debugRenderer = new Box2DDebugRenderer();
        
        // Initialisation du rendu
        batch = game.getSpriteBatch();
        
        // Initialisation du monde de jeu
        gameWorld = new GameWorld();
        gameWorld.setGameCamera(gameCamera);
        
        // Initialiser le joueur avec les managers
        if (gameWorld.getPlayer() != null) {
            gameWorld.initializePlayer(
                game.getInputManager(),
                game.getAudioManager(),
                game.getAssetManager()
            );
        }
        
        // Chargement de la carte
        loadMap("1-1");
    }
    
    private void loadMap(String mapName) {
        // Charger le niveau
        gameWorld.loadLevel(mapName);
        
        // Initialiser le joueur après le chargement
        if (gameWorld.getPlayer() != null) {
            gameWorld.initializePlayer(
                game.getInputManager(),
                game.getAudioManager(),
                game.getAssetManager()
            );
        }
        
        // Implémentation du chargement de la carte
        // mapRenderer = new OrthogonalTiledMapRenderer(assetLoader.getMap(mapName), 1 / Constants.WorldConfig.PPM);
    }
    
    private void update(float delta) {
        // Mise à jour du monde de jeu
        gameWorld.update(delta);
    }
    
    @Override
    public void render(float delta) {
        // Mise à jour
        update(delta);
        
        // Effacer l'écran
        Gdx.gl.glClearColor(0.53f, 0.81f, 0.92f, 1); // Ciel bleu clair
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        // Mise à jour du rendu de la carte
        if (mapRenderer != null) {
            mapRenderer.setView(gameCamera);
            mapRenderer.render();
        }
        
        // Rendu du jeu
        batch.setProjectionMatrix(gameCamera.combined);
        batch.begin();
        
        // Rendu du monde (inclut le joueur)
        gameWorld.render(batch);
        
        batch.end();
        
        // Rendu du debug si activé
        if (debugMode && gameWorld.getPhysicsWorld() != null) {
            debugRenderer.render(gameWorld.getPhysicsWorld(), 
                gameCamera.combined.scl(Constants.WorldConfig.PPM));
        }
        
        // Rendu de l'interface utilisateur
        if (stage != null) {
            stage.act(delta);
            stage.draw();
        }
    }
    
    @Override
    public void resize(int width, int height) {
        gameViewport.update(width, height, true);
        if (stage != null) {
            stage.getViewport().update(width, height, true);
        }
    }
    
    @Override
    public void show() {
        Gdx.app.log("GameScreen", "show() called");
    }
    
    @Override
    public void pause() {
        if (gameWorld != null) {
            gameWorld.setPaused(true);
        }
    }
    
    @Override
    public void resume() {
        if (gameWorld != null) {
            gameWorld.setPaused(false);
        }
    }
    
    @Override
    public void hide() {
        // Cacher l'écran
    }
    
    @Override
    public void dispose() {
        // Libérer les ressources
        if (mapRenderer != null) {
            mapRenderer.dispose();
        }
        if (gameWorld != null) {
            gameWorld.dispose();
        }
        if (debugRenderer != null) {
            debugRenderer.dispose();
        }
        if (stage != null) {
            stage.dispose();
        }
        Gdx.app.log("GameScreen", "dispose() called");
    }
}
