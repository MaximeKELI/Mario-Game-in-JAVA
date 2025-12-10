package com.mariogame.entities.items;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.mariogame.entities.Entity;
import com.mariogame.entities.Player;
import com.mariogame.utils.Constants.CollisionBits;

/**
 * Fleur qui donne au joueur la capacité de lancer des boules de feu.
 */
public class Flower extends Entity {
    private static final float BOUNCE_HEIGHT = 0.5f;
    private static final float BOUNCE_DURATION = 1f;
    
    private Animation<TextureRegion> animation;
    private boolean isActive = false;
    private boolean isConsumed = false;
    private float spawnTimer = 0.5f;
    private float bounceTimer = 0f;
    private float initialY;
    
    public Flower(World world, float x, float y) {
        super(world, x, y, 0.8f, 0.8f);
        this.initialY = y;
        
        // Charger l'animation (à implémenter avec l'AssetManager)
        // animation = assets.getAnimation("flower");
    }
    
    @Override
    protected void createBody() {
        // Créer la définition du corps
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.KinematicBody;
        bodyDef.position.set(position);
        bodyDef.fixedRotation = true;
        
        // Créer le corps dans le monde
        body = world.createBody(bodyDef);
        body.setUserData(this);
        
        // Créer la forme de collision
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width / 2, height / 2);
        
        // Définir les propriétés physiques
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.isSensor = true;
        
        // Définir les filtres de collision
        fixtureDef.filter.categoryBits = CollisionBits.ITEM;
        fixtureDef.filter.maskBits = CollisionBits.PLAYER;
        
        // Créer la fixture
        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData("flower");
        
        // Désactiver les collisions jusqu'à ce que la fleur soit active
        setActive(false);
        
        // Libérer la forme
        shape.dispose();
    }
    
    @Override
    public void update(float deltaTime) {
        if (isConsumed) return;
        
        // Mettre à jour le timer d'activation
        if (!isActive) {
            spawnTimer -= deltaTime;
            if (spawnTimer <= 0) {
                activate();
            }
            return;
        }
        
        // Mettre à jour l'animation
        stateTime += deltaTime;
        
        // Animation de flottement
        if (isActive) {
            bounceTimer += deltaTime;
            float bounceOffset = (float) Math.sin((bounceTimer * Math.PI * 2) / BOUNCE_DURATION) * BOUNCE_HEIGHT;
            
            if (body != null) {
                body.setTransform(
                    position.x,
                    initialY + bounceOffset,
                    0
                );
            }
        }
    }
    
    private void setActive(boolean active) {
        if (body != null) {
            body.setActive(active);
        }
    }
    
    private void activate() {
        if (isActive) return;
        
        isActive = true;
        setActive(true);
        
        // Jouer un son d'apparition
        // soundManager.playSound("powerup_appear", 0.5f);
    }
    
    @Override
    public void render(SpriteBatch batch) {
        if (isConsumed || animation == null) return;
        
        // Obtenir la frame d'animation actuelle
        TextureRegion currentFrame = animation.getKeyFrame(stateTime, true);
        
        // Dessiner la fleur
        float x = position.x - width / 2;
        float y = position.y - height / 2;
        
        batch.draw(currentFrame, x, y, width, height);
    }
    
    /**
     * Appelé lorsque le joueur entre en collision avec la fleur.
     * @param player Le joueur qui a collecté la fleur
     * @return true si la fleur a été consommée, false sinon
     */
    public boolean collect(Player player) {
        if (isConsumed || !isActive) return false;
        
        isConsumed = true;
        
        // Donner l'effet au joueur
        player.collectFireFlower();
        
        // Jouer un son de collection
        // soundManager.playSound("powerup", 0.7f);
        
        // Créer un effet visuel
        // particleManager.createEffect("powerup_collect", position.x, position.y);
        
        // Marquer pour suppression
        remove();
        
        return true;
    }
    
    @Override
    public void dispose() {
        super.dispose();
        // Libérer les ressources si nécessaire
    }
}
