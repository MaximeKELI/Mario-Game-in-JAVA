package com.mariogame.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.mariogame.core.MarioGame;

public class DesktopLauncher {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        
        // Configuration de la fenêtre
        config.setTitle("Super Mario Clone");
        config.setWindowedMode(800, 416); // 2x la taille virtuelle pour une meilleure visibilité
        config.setResizable(false);
        
        // Création de l'application
        new Lwjgl3Application(new MarioGame(), config);
    }
}
