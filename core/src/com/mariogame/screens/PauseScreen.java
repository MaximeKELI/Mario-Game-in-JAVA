package com.mariogame.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
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
 * Écran de pause qui s'affiche lorsque le jeu est mis en pause.
 */
public class PauseScreen implements Screen {
    private final MarioGame game;
    private final GameScreen gameScreen;
    private final Stage stage;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final BitmapFont titleFont;
    private final BitmapFont buttonFont;
    private final AssetLoader assetLoader;
    
    public PauseScreen(final MarioGame game, final GameScreen gameScreen) {
        this.game = game;
        this.gameScreen = gameScreen;
        this.assetLoader = AssetLoader.getInstance();
        
        // Initialisation de la caméra et de la vue
        camera = new OrthographicCamera();
        viewport = new FitViewport(800, 480, camera);
        viewport.apply();
        
        // Initialisation du stage pour l'interface utilisateur
        stage = new Stage(viewport);
        Gdx.input.setInputProcessor(stage);
        
        // Chargement des polices
        titleFont = assetLoader.getFont("large");
        buttonFont = assetLoader.getFont("medium");
        
        // Création de l'interface utilisateur
        createUI();
    }
    
    private void createUI() {
        Table table = new Table();
        table.setFillParent(true);
        table.center();
        
        // Titre
        Label.LabelStyle titleStyle = new Label.LabelStyle();
        titleStyle.font = titleFont;
        Label titleLabel = new Label("PAUSE", titleStyle);
        
        // Bouton Reprendre
        TextButton resumeButton = new TextButton("Reprendre", createButtonStyle());
        resumeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Reprendre le jeu
                game.setScreen(gameScreen);
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
        
        // Bouton Quitter vers le menu
        TextButton menuButton = new TextButton("Menu Principal", createButtonStyle());
        menuButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Retourner au menu principal
                game.setScreen(new MenuScreen(game));
            }
        });
        
        // Ajout des éléments au tableau
        table.add(titleLabel).padBottom(50).row();
        table.add(resumeButton).width(300).height(60).padBottom(20).row();
        table.add(optionsButton).width(300).height(60).padBottom(20).row();
        table.add(menuButton).width(300).height(60);
        
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
    public void render(float delta) {
        // Effacer l'écran avec un fond semi-transparent
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        Gdx.gl.glClearColor(0, 0, 0, 0.5f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        // Rendu du jeu en arrière-plan
        gameScreen.render(0);
        
        // Rendu de l'interface de pause
        stage.act(delta);
        stage.draw();
    }
    
    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        camera.position.set(0, 0, 0);
    }
    
    @Override
    public void show() {
        Gdx.app.log("PauseScreen", "show() called");
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
        Gdx.app.log("PauseScreen", "dispose() called");
    }
}
