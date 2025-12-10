package com.mariogame.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;

/**
 * Classe de base pour toutes les entités du jeu.
 * Fournit des fonctionnalités communes comme la position, la vitesse, etc.
 */
public abstract class Entity implements Disposable {
    protected final World world;
    protected Body body;
    protected final Vector2 position;
    protected float width;
    protected float height;
    protected boolean isRemoved = false;
    protected float stateTime = 0f;
    
    public Entity(World world, float x, float y, float width, float height) {
        this.world = world;
        this.position = new Vector2(x, y);
        this.width = width;
        this.height = height;
        createBody();
    }
    
    /**
     * Crée le corps physique de l'entité.
     * Doit être implémenté par les sous-classes.
     */
    protected abstract void createBody();
    
    /**
     * Met à jour l'état de l'entité.
     * @param deltaTime Temps écoulé depuis la dernière mise à jour en secondes
     */
    public void update(float deltaTime) {
        if (body != null) {
            position.set(body.getPosition());
        }
        stateTime += deltaTime;
    }
    
    /**
     * Dessine l'entité.
     * @param batch Le SpriteBatch à utiliser pour le rendu
     */
    public abstract void render(SpriteBatch batch);
    
    /**
     * Marque l'entité pour suppression.
     */
    public void remove() {
        isRemoved = true;
    }
    
    /**
     * Vérifie si l'entité est marquée pour suppression.
     */
    public boolean isRemoved() {
        return isRemoved;
    }
    
    /**
     * Libère les ressources utilisées par l'entité.
     */
    @Override
    public void dispose() {
        if (body != null) {
            world.destroyBody(body);
            body = null;
        }
    }
    
    // Getters et setters
    
    public Vector2 getPosition() {
        return position;
    }
    
    public float getX() {
        return position.x;
    }
    
    public float getY() {
        return position.y;
    }
    
    public float getWidth() {
        return width;
    }
    
    public float getHeight() {
        return height;
    }
    
    public Body getBody() {
        return body;
    }
    
    /**
     * Interface pour les objets qui doivent libérer des ressources.
     */
    public interface Disposable {
        void dispose();
    }
}
