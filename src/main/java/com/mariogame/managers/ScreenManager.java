package com.mariogame.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;
import com.mariogame.MarioGame;
import com.mariogame.screens.*;
import com.mariogame.screens.ScreenType;
import com.mariogame.utils.Constants;

/**
 * Gestionnaire d'écrans pour le jeu.
 * Permet de gérer la pile d'écrans et les transitions entre eux.
 */
public class ScreenManager implements Disposable {
    private final MarioGame game;
    private final ObjectMap<ScreenType, AbstractScreen> screens;
    private final Array<Screen> activeScreens;
    private ScreenType currentScreenType;
    private ScreenType nextScreenType;
    private boolean transitionInProgress;
    
    public ScreenManager(MarioGame game) {
        this.game = game;
        this.screens = new ObjectMap<>();
        this.activeScreens = new Array<>();
        this.transitionInProgress = false;
        
        // Initialisation des écrans
        initScreens();
    }
    
    private void initScreens() {
        // Création des écrans (ils seront initialisés au premier accès)
        screens.put(ScreenType.LOADING, new LoadingScreen(game));
        screens.put(ScreenType.MENU, new MenuScreen(game));
        screens.put(ScreenType.GAME, new GameScreen(game));
        screens.put(ScreenType.PAUSE, new PauseScreen(game));
        screens.put(ScreenType.GAME_OVER, new GameOverScreen(game));
        screens.put(ScreenType.OPTIONS, new OptionsScreen(game));
        screens.put(ScreenType.LEVEL_SELECT, new LevelSelectScreen(game));
        screens.put(ScreenType.CREDITS, new CreditsScreen(game));
    }
    
    /**
     * Définit l'écran actuel
     * @param screenType Le type d'écran à afficher
     */
    public void setScreen(ScreenType screenType) {
        if (transitionInProgress) {
            return; // Une transition est déjà en cours
        }
        
        // Si l'écran demandé est déjà affiché, on ne fait rien
        if (screenType == currentScreenType) {
            return;
        }
        
        // Planifier le prochain écran
        nextScreenType = screenType;
        
        // Démarrer la transition
        startTransition();
    }
    
    /**
     * Démarre une transition entre les écrans
     */
    private void startTransition() {
        if (transitionInProgress) {
            return;
        }
        
        transitionInProgress = true;
        
        // Si un écran est déjà affiché, on le fait disparaître progressivement
        if (currentScreenType != null) {
            AbstractScreen currentScreen = screens.get(currentScreenType);
            currentScreen.hide(new Runnable() {
                @Override
                public void run() {
                    // Une fois l'écran actuel masqué, on affiche le suivant
                    completeTransition();
                }
            });
        } else {
            // Pas d'écran actuel, on passe directement à l'affichage du suivant
            completeTransition();
        }
    }
    
    /**
     * Termine la transition et affiche le nouvel écran
     */
    private void completeTransition() {
        // Cacher l'écran précédent s'il existe
        if (currentScreenType != null) {
            AbstractScreen previousScreen = screens.get(currentScreenType);
            previousScreen.setActive(false);
        }
        
        // Mettre à jour l'écran courant
        currentScreenType = nextScreenType;
        AbstractScreen nextScreen = screens.get(currentScreenType);
        
        // Initialiser l'écran s'il ne l'est pas déjà
        if (!nextScreen.isInitialized()) {
            nextScreen.initialize();
        }
        
        // Activer l'écran
        nextScreen.setActive(true);
        nextScreen.show();
        
        // Mettre à jour le viewport
        nextScreen.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        
        // Réinitialiser le flag de transition
        transitionInProgress = false;
        
        Gdx.app.log("ScreenManager", "Screen changed to: " + currentScreenType);
    }
    
    /**
     * Obtient l'écran actuel
     * @return L'écran actuel ou null si aucun écran n'est affiché
     */
    public AbstractScreen getCurrentScreen() {
        return currentScreenType != null ? screens.get(currentScreenType) : null;
    }
    
    /**
     * Obtient un écran par son type
     * @param screenType Le type d'écran
     * @return L'écran correspondant
     */
    public AbstractScreen getScreen(ScreenType screenType) {
        return screens.get(screenType);
    }
    
    /**
     * Effectue le rendu de l'écran actuel
     * @param delta Le temps écoulé depuis le dernier rendu
     */
    public void render(float delta) {
        if (currentScreenType != null) {
            AbstractScreen screen = screens.get(currentScreenType);
            if (screen != null) {
                screen.render(delta);
            }
        }
    }
    
    /**
     * Redimensionne tous les écrans
     * @param width La nouvelle largeur
     * @param height La nouvelle hauteur
     */
    public void resize(int width, int height) {
        for (AbstractScreen screen : screens.values()) {
            if (screen != null) {
                screen.resize(width, height);
            }
        }
    }
    
    /**
     * Met en pause l'écran actuel
     */
    public void pause() {
        if (currentScreenType != null) {
            AbstractScreen screen = screens.get(currentScreenType);
            if (screen != null) {
                screen.pause();
            }
        }
    }
    
    /**
     * Reprend l'écran actuel
     */
    public void resume() {
        if (currentScreenType != null) {
            AbstractScreen screen = screens.get(currentScreenType);
            if (screen != null) {
                screen.resume();
            }
        }
    }
    
    /**
     * Libère les ressources utilisées par le gestionnaire d'écrans
     */
    @Override
    public void dispose() {
        Gdx.app.log("ScreenManager", "Disposing all screens");
        
        // Libérer tous les écrans
        for (AbstractScreen screen : screens.values()) {
            if (screen != null) {
                screen.dispose();
            }
        }
        screens.clear();
        activeScreens.clear();
    }
    
    /**
     * Vérifie si une transition est en cours
     * @return true si une transition est en cours, false sinon
     */
    public boolean isTransitionInProgress() {
        return transitionInProgress;
    }
}
