package com.mariogame.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.mariogame.MarioGame;
import com.mariogame.utils.Constants;

/**
 * Gestionnaire centralisé des entrées utilisateur.
 * Gère toutes les interactions clavier/souris/tactile de manière professionnelle.
 */
public class InputManager implements InputProcessor {
    private final MarioGame game;
    
    // États des touches
    private boolean moveLeft = false;
    private boolean moveRight = false;
    private boolean jumpPressed = false;
    private boolean jumpJustPressed = false;
    private boolean runPressed = false;
    private boolean crouchPressed = false;
    private boolean pausePressed = false;
    private boolean debugPressed = false;
    
    // États de la souris/tactile
    private boolean touchDown = false;
    private float touchX = 0;
    private float touchY = 0;
    
    public InputManager(MarioGame game) {
        this.game = game;
    }
    
    /**
     * Met à jour l'état des entrées (appelé chaque frame).
     */
    public void update() {
        // Réinitialiser les états "just pressed" après une frame
        jumpJustPressed = false;
        pausePressed = false;
        debugPressed = false;
    }
    
    // Getters pour les états
    
    public boolean isMoveLeft() {
        return moveLeft;
    }
    
    public boolean isMoveRight() {
        return moveRight;
    }
    
    public boolean isJumpPressed() {
        return jumpPressed;
    }
    
    public boolean isJumpJustPressed() {
        return jumpJustPressed;
    }
    
    public boolean isRunPressed() {
        return runPressed;
    }
    
    public boolean isCrouchPressed() {
        return crouchPressed;
    }
    
    public boolean isPausePressed() {
        return pausePressed;
    }
    
    public boolean isDebugPressed() {
        return debugPressed;
    }
    
    public boolean isTouchDown() {
        return touchDown;
    }
    
    public float getTouchX() {
        return touchX;
    }
    
    public float getTouchY() {
        return touchY;
    }
    
    // Méthodes utilitaires
    
    /**
     * Retourne la direction horizontale (-1 pour gauche, 1 pour droite, 0 pour aucun).
     */
    public float getHorizontalAxis() {
        if (moveLeft) return -1f;
        if (moveRight) return 1f;
        return 0f;
    }
    
    /**
     * Vérifie si le joueur essaie de bouger.
     */
    public boolean isMoving() {
        return moveLeft || moveRight;
    }
    
    // Implémentation de InputProcessor
    
    @Override
    public boolean keyDown(int keycode) {
        switch (keycode) {
            case Constants.Keys.MOVE_LEFT:
            case Input.Keys.A:
                moveLeft = true;
                return true;
                
            case Constants.Keys.MOVE_RIGHT:
            case Input.Keys.D:
                moveRight = true;
                return true;
                
            case Constants.Keys.JUMP:
            case Constants.Keys.JUMP_ALT:
            case Constants.Keys.JUMP_ALT2:
                if (!jumpPressed) {
                    jumpJustPressed = true;
                }
                jumpPressed = true;
                return true;
                
            case Constants.Keys.RUN:
            case Constants.Keys.RUN_ALT:
                runPressed = true;
                return true;
                
            case Constants.Keys.CROUCH:
            case Input.Keys.S:
                crouchPressed = true;
                return true;
                
            case Constants.Keys.PAUSE:
                pausePressed = true;
                return true;
                
            case Constants.Keys.DEBUG:
                debugPressed = true;
                return true;
        }
        return false;
    }
    
    @Override
    public boolean keyUp(int keycode) {
        switch (keycode) {
            case Constants.Keys.MOVE_LEFT:
            case Input.Keys.A:
                moveLeft = false;
                return true;
                
            case Constants.Keys.MOVE_RIGHT:
            case Input.Keys.D:
                moveRight = false;
                return true;
                
            case Constants.Keys.JUMP:
            case Constants.Keys.JUMP_ALT:
            case Constants.Keys.JUMP_ALT2:
                jumpPressed = false;
                return true;
                
            case Constants.Keys.RUN:
            case Constants.Keys.RUN_ALT:
                runPressed = false;
                return true;
                
            case Constants.Keys.CROUCH:
            case Input.Keys.S:
                crouchPressed = false;
                return true;
        }
        return false;
    }
    
    @Override
    public boolean keyTyped(char character) {
        return false;
    }
    
    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (pointer == 0) {
            touchDown = true;
            touchX = screenX;
            touchY = screenY;
            return true;
        }
        return false;
    }
    
    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (pointer == 0) {
            touchDown = false;
            return true;
        }
        return false;
    }
    
    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (pointer == 0) {
            touchX = screenX;
            touchY = screenY;
            return true;
        }
        return false;
    }
    
    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }
    
    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }
}

