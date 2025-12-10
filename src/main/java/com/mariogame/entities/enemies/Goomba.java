package com.mariogame.entities.enemies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.mariogame.entities.Entity;
import com.mariogame.utils.Constants;
import com.mariogame.utils.Constants.CollisionBits;

/**
 * Classe représentant un ennemi Goomba.
 */
public class Goomba extends Entity {
    private static final float WALK_SPEED = 1f;
    private static final float DEATH_DURATION = 0.5f;
    private static final float DAMAGE_COOLDOWN = 1f;
    
    private enum State {
        WALKING,
        DEAD,
        DEAD_FLIPPED
    }
    
    private State currentState = State.WALKING;
    private State previousState = State.WALKING;
    private float stateTime = 0f;
    private boolean facingRight = false;
    private boolean isDead = false;
    private float damageCooldown = 0f;
    
    // Animations
    private Animation<TextureRegion> walkAnimation;
    private Animation<TextureRegion> deadAnimation;
    private TextureRegion currentFrame;
    
    public Goomba(World world, float x, float y) {
        super(world, x, y, 0.8f, 0.8f);
        
        // Charger les animations (à implémenter avec l'AssetManager)
        // loadAnimations();
        
        // Définir la vitesse initiale
        if (body != null) {
            body.setLinearVelocity(WALK_SPEED * (Math.random() > 0.5 ? 1 : -1), 0);
        }
    }
    
    @Override
    protected void createBody() {
        // Créer la définition du corps
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
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
        fixtureDef.density = 1f;
        fixtureDef.friction = 0.5f;
        
        // Définir les filtres de collision
        fixtureDef.filter.categoryBits = CollisionBits.ENEMY;
        fixtureDef.filter.maskBits = (short) (
            CollisionBits.GROUND | 
            CollisionBits.PLAYER | 
            CollisionBits.ENEMY |
            CollisionBits.ITEM
        );
        
        // Créer la fixture principale
        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData("enemy_body");
        
        // Créer un capteur pour détecter les collisions avec le joueur par le dessus
        createHeadSensor();
        
        // Libérer la forme
        shape.dispose();
    }
    
    private void createHeadSensor() {
        // Créer un capteur au-dessus de la tête du Goomba
        PolygonShape headShape = new PolygonShape();
        headShape.setAsBox(
            width / 2.5f, 
            0.1f, 
            new Vector2(0, height / 2), 
            0
        );
        
        FixtureDef headFixtureDef = new FixtureDef();
        headFixtureDef.shape = headShape;
        headFixtureDef.isSensor = true;
        headFixtureDef.filter.categoryBits = CollisionBits.ENEMY_HEAD;
        headFixtureDef.filter.maskBits = CollisionBits.PLAYER;
        
        Fixture headFixture = body.createFixture(headFixtureDef);
        headFixture.setUserData("enemy_head");
        
        headShape.dispose();
    }
    
    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        
        if (isDead) {
            stateTime += deltaTime;
            
            // Supprimer le Goomba après l'animation de mort
            if (stateTime > DEATH_DURATION) {
                remove();
            }
            return;
        }
        
        // Mettre à jour le temps d'état
        stateTime += deltaTime;
        
        // Mettre à jour le temps de cooldown des dégâts
        if (damageCooldown > 0) {
            damageCooldown -= deltaTime;
        }
        
        // Mettre à jour la vitesse de déplacement
        if (currentState == State.WALKING && body != null) {
            Vector2 velocity = body.getLinearVelocity();
            body.setLinearVelocity(
                facingRight ? WALK_SPEED : -WALK_SPEED,
                velocity.y
            );
        }
        
        // Mettre à jour l'animation
        updateAnimation(deltaTime);
    }
    
    private void updateAnimation(float deltaTime) {
        // Mettre à jour l'animation en fonction de l'état
        switch (currentState) {
            case WALKING:
                currentFrame = walkAnimation.getKeyFrame(stateTime, true);
                break;
            case DEAD:
            case DEAD_FLIPPED:
                currentFrame = deadAnimation.getKeyFrame(stateTime, false);
                break;
        }
    }
    
    @Override
    public void render(SpriteBatch batch) {
        if (currentFrame == null) return;
        
        // Dessiner le Goomba
        float x = position.x - width / 2;
        float y = position.y - height / 2;
        
        // Inverser le sprite si nécessaire
        if ((facingRight && currentFrame.isFlipX()) || 
            (!facingRight && !currentFrame.isFlipX())) {
            currentFrame.flip(true, false);
        }
        
        // Appliquer une rotation si le Goomba est mort et retourné
        if (currentState == State.DEAD_FLIPPED) {
            batch.draw(
                currentFrame,
                x + width / 2, y + height / 2,
                -width / 2, -height / 2,
                width, height,
                1, 1,
                180, 0, 0,
                currentFrame.getRegionWidth(), currentFrame.getRegionHeight(),
                false, false
            );
        } else {
            batch.draw(currentFrame, x, y, width, height);
        }
    }
    
    /**
     * Appelé lorsque le Goomba est touché par le joueur.
     * @param fromAbove Indique si le joueur a touché le Goomba par le haut
     * @return true si le Goomba a été vaincu, false sinon
     */
    public boolean hit(boolean fromAbove) {
        if (isDead || damageCooldown > 0) return false;
        
        if (fromAbove) {
            // Le joueur a sauté sur le Goomba
            die();
            return true;
        } else {
            // Le joueur a été touché par le côté
            if (damageCooldown <= 0) {
                damageCooldown = DAMAGE_COOLDOWN;
                // Faire reculer le Goomba
                if (body != null) {
                    body.applyLinearImpulse(
                        new Vector2(facingRight ? -2f : 2f, 2f),
                        body.getWorldCenter(),
                        true
                    );
                }
            }
            return false;
        }
    }
    
    private void die() {
        if (isDead) return;
        
        isDead = true;
        stateTime = 0f;
        
        // Choisir un type de mort aléatoire
        currentState = Math.random() > 0.5 ? State.DEAD : State.DEAD_FLIPPED;
        
        // Désactiver les collisions
        if (body != null) {
            for (Fixture fixture : body.getFixtureList()) {
                fixture.setSensor(true);
            }
            
            // Appliquer une impulsion pour le faire sauter ou tourner
            if (currentState == State.DEAD) {
                body.setLinearVelocity(0, 5f);
            } else {
                body.setAngularVelocity(10f);
            }
            
            // Désactiver la gravité après un court délai
            // pour éviter qu'il ne tombe à travers le sol
            Gdx.app.postRunnable(() -> {
                if (body != null) {
                    body.setGravityScale(0);
                    body.setLinearVelocity(0, 0);
                }
            });
        }
        
        // Jouer un son de mort
        // soundManager.playSound("goomba_die", 0.5f);
    }
    
    /**
     * Change la direction du déplacement du Goomba.
     */
    public void changeDirection() {
        facingRight = !facingRight;
    }
    
    @Override
    public void dispose() {
        super.dispose();
        // Libérer les ressources des animations si nécessaire
    }
}
