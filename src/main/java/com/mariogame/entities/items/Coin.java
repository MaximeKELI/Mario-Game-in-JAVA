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
 * Pièce qui peut être collectée par le joueur pour gagner des points.
 */
public class Coin extends Entity {
    private static final float ROTATION_SPEED = 180f; // Degrés par seconde
    private static final float BOUNCE_HEIGHT = 0.5f;
    private static final float BOUNCE_DURATION = 0.5f;
    
    private Animation<TextureRegion> animation;
    private boolean isCollected = false;
    private float spawnY;
    private float bounceTimer = 0f;
    private float rotation = 0f;
    
    public Coin(World world, float x, float y) {
        super(world, x, y, 0.6f, 0.6f);
        this.spawnY = y;
        
        // Charger l'animation (à implémenter avec l'AssetManager)
        // animation = assets.getAnimation("coin");
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
        CircleShape shape = new CircleShape();
        shape.setRadius(width / 2);
        
        // Définir les propriétés physiques
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.isSensor = true;
        
        // Définir les filtres de collision
        fixtureDef.filter.categoryBits = CollisionBits.ITEM;
        fixtureDef.filter.maskBits = CollisionBits.PLAYER;
        
        // Créer la fixture
        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData("coin");
        
        // Libérer la forme
        shape.dispose();
    }
    
    @Override
    public void update(float deltaTime) {
        if (isCollected) return;
        
        // Mettre à jour la position à partir du corps physique
        if (body != null) {
            position.set(body.getPosition());
        }
        
        // Mettre à jour l'animation
        stateTime += deltaTime;
        
        // Mettre à jour la rotation
        rotation = (rotation + ROTATION_SPEED * deltaTime) % 360;
        
        // Mettre à jour l'animation de rebond
        bounceTimer = (bounceTimer + deltaTime) % BOUNCE_DURATION;
    }
    
    @Override
    public void render(SpriteBatch batch) {
        if (isCollected || animation == null) return;
        
        // Obtenir la frame d'animation actuelle
        TextureRegion currentFrame = animation.getKeyFrame(stateTime, true);
        
        // Calculer le décalage de rebond
        float bounceOffset = (float) Math.sin((bounceTimer / BOUNCE_DURATION) * Math.PI * 2) * BOUNCE_HEIGHT;
        
        // Dessiner la pièce avec rotation et rebond
        float x = position.x;
        float y = position.y + bounceOffset;
        
        batch.draw(
            currentFrame,
            x - width / 2, y - height / 2,
            width / 2, height / 2,
            width, height,
            1, 1,
            rotation
        );
    }
    
    /**
     * Appelé lorsque le joueur entre en collision avec la pièce.
     * @param player Le joueur qui a collecté la pièce
     * @return true si la pièce a été collectée, false sinon
     */
    public boolean collect(Player player) {
        if (isCollected) return false;
        
        isCollected = true;
        
        // Donner les points au joueur
        if (player != null) {
            player.collectCoin();
        }
        
        // Jouer un son de collection
        // soundManager.playSound("coin", 0.3f);
        
        // Créer un effet visuel
        // particleManager.createEffect("coin_collect", position.x, position.y);
        
        // Marquer pour suppression
        remove();
        
        return true;
    }
    
    /**
     * Crée une animation de pièce qui tourne.
     */
    public static void createCoinAnimation() {
        // À implémenter avec l'AssetManager
        // Exemple :
        // Array<TextureRegion> frames = new Array<>();
        // for (int i = 0; i < 4; i++) {
        //     frames.add(new TextureRegion(texture, i * 16, 0, 16, 16));
        // }
        // return new Animation<>(0.1f, frames, Animation.PlayMode.LOOP);
    }
    
    @Override
    public void dispose() {
        super.dispose();
        // Libérer les ressources si nécessaire
    }
}
