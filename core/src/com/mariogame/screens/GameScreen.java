package com.mariogame.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.mariogame.MarioGame;
import com.mariogame.core.AssetLoader;
import com.mariogame.core.GameConfig;
import com.mariogame.entities.Player;
import com.mariogame.utils.WorldUtils;

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
    
    // Monde physique
    private World world;
    private Box2DDebugRenderer debugRenderer;
    
    // Entités
    private Player player;
    
    // Contrôles
    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private boolean jumpPressed = false;
    private boolean runPressed = false;
    
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
            GameConfig.World.V_WIDTH * GameConfig.World.PPM, 
            GameConfig.World.V_HEIGHT * GameConfig.World.PPM, 
            gameCamera
        );
        
        // Initialisation du monde physique
        world = new World(new Vector2(0, -GameConfig.World.GRAVITY), true);
        debugRenderer = new Box2DDebugRenderer();
        
        // Initialisation du rendu
        batch = new SpriteBatch();
        
        // Initialisation du joueur
        player = new Player(world, 2, 10);
        
        // Configuration des entrées
        Gdx.input.setInputProcessor(this);
        
        // Chargement de la carte
        loadMap("level1");
    }
    
    private void loadMap(String mapName) {
        // Implémentation du chargement de la carte
        // mapRenderer = new OrthogonalTiledMapRenderer(assetLoader.getMap(mapName), 1 / GameConfig.World.PPM);
    }
    
    private void handleInput(float delta) {
        // Gestion des entrées utilisateur
        if (leftPressed) {
            player.moveLeft();
            if (runPressed) {
                player.runLeft();
            }
        } else if (rightPressed) {
            player.moveRight();
            if (runPressed) {
                player.runRight();
            }
        } else {
            player.stop();
        }
        
        if (jumpPressed) {
            player.jump();
        }
    }
    
    private void update(float delta) {
        // Mise à jour du monde physique
        world.step(
            GameConfig.World.TIME_STEP, 
            GameConfig.World.VELOCITY_ITERATIONS, 
            GameConfig.World.POSITION_ITERATIONS
        );
        
        // Mise à jour du joueur
        player.update(delta);
        
        // Mise à jour de la caméra
        updateCamera(delta);
    }
    
    private void updateCamera(float delta) {
        // Suivre le joueur avec la caméra
        if (player != null) {
            Vector2 position = player.getPosition();
            gameCamera.position.x = position.x * GameConfig.World.PPM;
            gameCamera.position.y = position.y * GameConfig.World.PPM;
            
            // Limiter la caméra aux bords de la carte
            float viewportWidth = gameViewport.getWorldWidth();
            float viewportHeight = gameViewport.getWorldHeight();
            
            // Ici, vous devriez utiliser la taille réelle de votre carte
            float mapWidth = 100 * GameConfig.World.PPM; // Exemple
            float mapHeight = 20 * GameConfig.World.PPM; // Exemple
            
            gameCamera.position.x = Math.max(viewportWidth / 2, Math.min(mapWidth - viewportWidth / 2, gameCamera.position.x));
            gameCamera.position.y = Math.max(viewportHeight / 2, Math.min(mapHeight - viewportHeight / 2, gameCamera.position.y));
            
            gameCamera.update();
        }
    }
    
    @Override
    public void render(float delta) {
        // Mise à jour
        handleInput(delta);
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
        
        // Rendu du joueur
        player.render(batch);
        
        batch.end();
        
        // Rendu du debug si activé
        if (debugMode) {
            debugRenderer.render(world, 
                gameCamera.combined.scl(GameConfig.World.PPM));
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
        // Mettre le jeu en pause
    }
    
    @Override
    public void resume() {
        // Reprendre le jeu
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
        if (world != null) {
            world.dispose();
        }
        if (debugRenderer != null) {
            debugRenderer.dispose();
        }
        if (batch != null) {
            batch.dispose();
        }
        if (stage != null) {
            stage.dispose();
        }
        Gdx.app.log("GameScreen", "dispose() called");
    }
    
    // Gestion des entrées tactiles/au clavier
    public boolean keyDown(int keycode) {
        switch (keycode) {
            case com.badlogic.gdx.Input.Keys.LEFT:
                leftPressed = true;
                break;
            case com.badlogic.gdx.Input.Keys.RIGHT:
                rightPressed = true;
                break;
            case com.badlogic.gdx.Input.Keys.UP:
            case com.badlogic.gdx.Input.Keys.SPACE:
            case com.badlogic.gdx.Input.Keys.Z:
                jumpPressed = true;
                break;
            case com.badlogic.gdx.Input.Keys.SHIFT_LEFT:
            case com.badlogic.gdx.Input.Keys.SHIFT_RIGHT:
                runPressed = true;
                break;
            case com.badlogic.gdx.Input.Keys.ESCAPE:
                game.setScreen(new PauseScreen(game, this));
                break;
            case com.badlogic.gdx.Input.Keys.F1:
                debugMode = !debugMode;
                break;
        }
        return true;
    }
    
    public boolean keyUp(int keycode) {
        switch (keycode) {
            case com.badlogic.gdx.Input.Keys.LEFT:
                leftPressed = false;
                break;
            case com.badlogic.gdx.Input.Keys.RIGHT:
                rightPressed = false;
                break;
            case com.badlogic.gdx.Input.Keys.UP:
            case com.badlogic.gdx.Input.Keys.SPACE:
            case com.badlogic.gdx.Input.Keys.Z:
                jumpPressed = false;
                break;
            case com.badlogic.gdx.Input.Keys.SHIFT_LEFT:
            case com.badlogic.gdx.Input.Keys.SHIFT_RIGHT:
                runPressed = false;
                break;
        }
        return true;
    }
}
