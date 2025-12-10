package com.mariogame.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.mariogame.MarioGame;
import com.mariogame.core.AssetLoader;

/**
 * Écran de menu principal du jeu.
 */
public class MenuScreen implements Screen {
    private final MarioGame game;
    private final Stage stage;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final SpriteBatch batch;
    private final BitmapFont titleFont;
    private final BitmapFont buttonFont;
    private final Texture background;
    
    private final AssetLoader assetLoader;
    
    public MenuScreen(final MarioGame game) {
        this.game = game;
        this.assetLoader = AssetLoader.getInstance();
        
        // Initialisation de la caméra et de la vue
        camera = new OrthographicCamera();
        viewport = new FitViewport(800, 480, camera);
        viewport.apply();
        
        // Initialisation du stage pour l'interface utilisateur
        stage = new Stage(viewport);
        Gdx.input.setInputProcessor(stage);
        
        // Initialisation du batch pour le rendu
        batch = new SpriteBatch();
        
        // Chargement des polices
        titleFont = assetLoader.getFont("large");
        buttonFont = assetLoader.getFont("medium");
        
        // Chargement de l'arrière-plan
        background = new Texture("textures/menu/background.png");
        
        // Création de l'interface utilisateur
        createUI();
    }
    
    private void createUI() {
        Table table = new Table();
        table.setFillParent(true);
        table.center();
        
        // Titre du jeu
        // (Rendu séparément pour plus de contrôle)
        
        // Bouton Nouvelle Partie
        TextButton newGameButton = new TextButton("Nouvelle Partie", createButtonStyle());
        newGameButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Démarrer une nouvelle partie
                game.startNewGame();
            }
        });
        
        // Bouton Options
        TextButton optionsButton = new TextButton("Options", createButtonStyle());
        optionsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Afficher l'écran des options
                game.setScreen(new OptionsScreen(game));
            }
        });
        
        // Bouton Quitter
        TextButton exitButton = new TextButton("Quitter", createButtonStyle());
        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });
        
        // Ajout des éléments au tableau
        table.add(newGameButton).width(300).height(60).padBottom(20).row();
        table.add(optionsButton).width(300).height(60).padBottom(20).row();
        table.add(exitButton).width(300).height(60);
        
        stage.addActor(table);
    }
    
    private TextButton.TextButtonStyle createButtonStyle() {
        Skin skin = new Skin();
        skin.add("default", buttonFont);
        
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.font = buttonFont;
        style.up = skin.getDrawable("button-up");
        style.down = skin.getDrawable("button-down");
        style.over = skin.getDrawable("button-over");
        
        return style;
    }
    
    @Override
    public void show() {
        Gdx.app.log("MenuScreen", "show() called");
    }
    
    @Override
    public void render(float delta) {
        // Effacer l'écran
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        // Mise à jour de la scène
        stage.act(delta);
        
        // Rendu
        batch.setProjectionMatrix(camera.combined);
        
        // Dessiner l'arrière-plan
        batch.begin();
        batch.draw(background, -viewport.getWorldWidth() / 2, -viewport.getWorldHeight() / 2, 
                  viewport.getWorldWidth(), viewport.getWorldHeight());
        
        // Dessiner le titre
        String title = "SUPER MARIO";
        titleFont.draw(
            batch, 
            title, 
            -viewport.getWorldWidth() / 2, 
            viewport.getWorldHeight() / 2 - 50, 
            viewport.getWorldWidth(), 
            Align.center, 
            false
        );
        
        batch.end();
        
        // Dessiner l'interface utilisateur
        stage.draw();
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
        stage.dispose();
        batch.dispose();
        titleFont.dispose();
        buttonFont.dispose();
        background.dispose();
        Gdx.app.log("MenuScreen", "dispose() called");
    }
}
