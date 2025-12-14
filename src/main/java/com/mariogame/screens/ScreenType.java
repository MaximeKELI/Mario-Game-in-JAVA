package com.mariogame.screens;

/**
 * Énumération des différents types d'écrans du jeu.
 */
public enum ScreenType {
    /** Écran de chargement */
    LOADING,
    
    /** Menu principal */
    MENU,
    
    /** Écran de jeu principal */
    GAME,
    
    /** Écran de pause */
    PAUSE,
    
    /** Transition entre niveaux */
    LEVEL_TRANSITION,
    
    /** Écran de fin de partie */
    GAME_OVER,
    
    /** Écran des options */
    OPTIONS,
    
    /** Sélection de niveau */
    LEVEL_SELECT,
    
    /** Crédits */
    CREDITS
}
