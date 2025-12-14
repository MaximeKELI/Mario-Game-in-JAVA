package com.mariogame.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Disposable;
import com.mariogame.world.GameWorld;

/**
 * Classe de base professionnelle pour toutes les entités du jeu.
 * Fournit des fonctionnalités communes comme la position, la vitesse, etc.
 * Architecture inspirée des meilleures pratiques de l'industrie du jeu vidéo.
 */
public abstract class Entity implements Disposable {
    protected final World world;
    protected Body body;
    protected final Vector2 position;
    protected final Vector2 velocity;
    protected float width;
    protected float height;
    protected boolean isRemoved = false;
    protected float stateTime = 0f;
    protected GameWorld gameWorld;
    
    public Entity(World world, float x, float y, float width, float height) {
        if (world == null) {
            throw new IllegalArgumentException("World cannot be null");
        }
        this.world = world;
        this.position = new Vector2(x, y);
        this.velocity = new Vector2();
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
        if (isRemoved || body == null) return;
        
        // Mettre à jour la position depuis le corps physique
        position.set(body.getPosition());
        
        // Mettre à jour la vélocité
        if (body != null) {
            velocity.set(body.getLinearVelocity());
        }
        
        // Incrémenter le temps d'état
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
     * Définit la référence au monde de jeu.
     */
    public void setWorld(GameWorld gameWorld) {
        this.gameWorld = gameWorld;
    }
    
    /**
     * Récupère la référence au monde de jeu.
     */
    public GameWorld getGameWorld() {
        return gameWorld;
    }
    
    /**
     * Libère les ressources utilisées par l'entité.
     */
    @Override
    public void dispose() {
        if (body != null && world != null) {
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
    
    public Vector2 getVelocity() {
        return velocity;
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
    
    public float getStateTime() {
        return stateTime;
    }
    
    /**
     * Vérifie si l'entité est active (non supprimée et avec un corps valide).
     */
    public boolean isActive() {
        return !isRemoved && body != null && body.isActive();
    }
}
