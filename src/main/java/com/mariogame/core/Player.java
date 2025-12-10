package com.mariogame.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;

public class Player {
    // Constantes de jeu
    private static final float SPEED = 2.5f;
    private static final float JUMP_FORCE = 10f;
    private static final float DAMPING = 0.9f;
    private static final float MAX_SPEED = 4f;
    
    // États du joueur
    public enum State { IDLE, RUNNING, JUMPING, FALLING, CROUCHING }
    private State currentState = State.IDLE;
    private State previousState = State.IDLE;
    private boolean facingRight = true;
    private boolean isJumping = false;
    private boolean isCrouching = false;
    private boolean isInvincible = false;
    private float invincibilityTimer = 0f;
    
    // Physique
    private Body body;
    private Fixture fixture;
    
    // Graphismes
    private TextureAtlas playerAtlas;
    private Animation<TextureRegion> idleAnimation;
    private Animation<TextureRegion> runAnimation;
    private Animation<TextureRegion> jumpAnimation;
    private Animation<TextureRegion> fallAnimation;
    private Animation<TextureRegion> crouchAnimation;
    private Animation<TextureRegion> currentAnimation;
    private float stateTime = 0;
    private Sprite currentFrame;
    
    // Statistiques
    private int lives = 3;
    private int coins = 0;
    private float score = 0;
    
    // Sons
    private Sound jumpSound;
    private Sound coinSound;
    private Sound powerupSound;
    private Sound deathSound;
    
    // Interface pour gérer la mort du joueur
    public interface OnPlayerDeath {
        void onDeath();
    }
    private OnPlayerDeath onPlayerDeath;
    
    // Constructeur
    public Player(World world, float x, float y) {
        createPhysics(world, x, y);
        loadAnimations();
        loadSounds();
    }
    private Sound coinSound;
    private Sound hurtSound;
    
    // Propriétés du joueur
    private float stateTime = 0;
    private boolean isFacingRight = true;
    private float invincibleTimer = 0;
    private static final float INVINCIBLE_DURATION = 2f;
    
    public Player(World world, float x, float y) {
        // Chargement des ressources
        GameManager gameManager = GameManager.getInstance();
        playerAtlas = gameManager.getAtlas(PLAYER_ATLAS);
        jumpSound = gameManager.getSound(JUMP_SOUND);
        coinSound = gameManager.getSound(COIN_SOUND);
        
        // Initialisation des animations
        createAnimations();
        
        // Création du corps physique
        BodyDef bdef = new BodyDef();
        bdef.position.set(x / MarioGame.PPM, y / MarioGame.PPM);
        bdef.type = BodyDef.BodyType.DynamicBody;
        bdef.fixedRotation = true;
        body = world.createBody(bdef);
        
        // Définition de la forme du joueur
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(8 / MarioGame.PPM, 15 / MarioGame.PPM, new Vector2(0, 0), 0);
        
        // Définition des propriétés physiques
        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;
        fdef.friction = 0.6f;
        fdef.density = 1.0f;
        fdef.restitution = 0.1f;
        
        // Filtre de collision
        Filter filter = new Filter();
        filter.categoryBits = MarioGame.PLAYER_BIT;
        filter.maskBits = MarioGame.GROUND_BIT | MarioGame.COIN_BIT | MarioGame.ENEMY_BIT | MarioGame.OBJECT_BIT;
        
        fixture = body.createFixture(fdef);
        fixture.setUserData(this);
        fixture.setFilterData(filter);
        
        // Initialisation du sprite
        currentFrame = new Sprite();
        currentState = State.IDLE;
        previousState = State.IDLE;
        
        // Chargement des sons
        // jumpSound = Gdx.audio.newSound(Gdx.files.internal("sounds/jump.wav"));
        // coinSound = Gdx.audio.newSound(Gdx.files.internal("sounds/coin.wav"));
        // hurtSound = Gdx.audio.newSound(Gdx.files.internal("sounds/hurt.wav"));
        
        shape.dispose();
        fdef.density = 1f;
        fdef.friction = 0.5f;
        
        body.createFixture(fdef).setUserData(this);
    private void createAnimations() {
        // Création des animations à partir de l'atlas
        float frameDuration = 0.1f;
        
        // Animation d'idle
        idleAnimation = new Animation<>(
            frameDuration,
            playerAtlas.findRegions("idle"),
            Animation.PlayMode.LOOP
        );
        
        // Animation de course
        runAnimation = new Animation<>(
            frameDuration,
            playerAtlas.findRegions("run"),
            Animation.PlayMode.LOOP
        );
        
        // Animation de saut
        jumpAnimation = new Animation<>(
            frameDuration,
            playerAtlas.findRegions("jump"),
            Animation.PlayMode.NORMAL
        );
        
        // Animation de chute
        fallAnimation = new Animation<>(
            frameDuration,
            playerAtlas.findRegions("fall"),
            Animation.PlayMode.NORMAL
        );
        
        // Animation d'accroupi
        crouchAnimation = new Animation<>(
            frameDuration,
            playerAtlas.findRegions("crouch"),
            Animation.PlayMode.NORMAL
        );
        
        currentAnimation = idleAnimation;
    }
        
        // Création du sprite (pour l'instant un simple carré)
        sprite = new Sprite(new Texture(Gdx.files.internal("player.png")));
        sprite.setSize(32 / MarioGame.PPM, 32 / MarioGame.PPM);
    }
    
    public void update(float delta) {
        // Mise à jour du temps d'invincibilité
        if (isInvincible) {
            invincibleTimer -= delta;
            if (invincibleTimer <= 0) {
                isInvincible = false;
            }
        }
        
        // Mise à jour du temps d'état pour les animations
        stateTime += delta;
        
        // Mise à jour de la position du sprite
        sprite.setPosition(
            body.getPosition().x - sprite.getWidth()/2, 
            body.getPosition().y - sprite.getHeight()/2
        );
        
        // Gestion des entrées utilisateur
        handleInput();
    }
    
    private void handleInput() {
        // Déplacement horizontal
        if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.RIGHT)) {
            body.setLinearVelocity(SPEED, body.getLinearVelocity().y);
            sprite.setFlip(false, false);
        } else if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.LEFT)) {
            body.setLinearVelocity(-SPEED, body.getLinearVelocity().y);
            sprite.setFlip(true, false);
        } else {
            body.setLinearVelocity(0, body.getLinearVelocity().y);
        }
        }
    }
    
    public void render(SpriteBatch batch) {
        // Mise à jour de l'animation en fonction de l'état
        updateAnimation();
        
        // Récupérer la frame actuelle
        TextureRegion currentFrame = currentAnimation.getKeyFrame(stateTime, true);
        
        // Inverser le sprite si nécessaire
        if (!isFacingRight && !currentFrame.isFlipX()) {
            currentFrame.flip(true, false);
        } else if (isFacingRight && currentFrame.isFlipX()) {
            currentFrame.flip(true, false);
        }
        
        // Dessiner le joueur
        if (!isInvincible || (int)(stateTime * 10) % 2 == 0) {
            batch.draw(currentFrame, 
                     body.getPosition().x - currentFrame.getRegionWidth() / 2f / PPM, 
                     body.getPosition().y - currentFrame.getRegionHeight() / 2f / PPM,
                     currentFrame.getRegionWidth() / 2f / PPM,
                     currentFrame.getRegionHeight() / 2f / PPM,
                     currentFrame.getRegionWidth() / PPM,
                     currentFrame.getRegionHeight() / PPM,
                     1, 1, 0);
        }
    }
    
    private void updateAnimation() {
        Vector2 velocity = body.getLinearVelocity();
        
        if (isCrouching) {
            currentAnimation = crouchAnimation;
        } else if (!isGrounded()) {
            if (velocity.y > 0) {
                currentAnimation = jumpAnimation;
            } else {
                currentAnimation = fallAnimation;
            }
        } else if (Math.abs(velocity.x) > 0.1f) {
            currentAnimation = runAnimation;
        } else {
            currentAnimation = idleAnimation;
        }
    }
    
    public void jump() {
        if (isGrounded() && !isCrouching) {
            body.applyLinearImpulse(new Vector2(0, JUMP_FORCE), body.getWorldCenter(), true);
            isJumping = true;
            jumpSound.play(0.5f);
        }
    }
    
    public boolean isGrounded() {
        // Implémentez la détection du sol selon votre logique de jeu
        // Par exemple, en utilisant des raycasts ou des capteurs de pieds
        return !isJumping;
    }
    
    public void moveLeft() {
        if (!isCrouching) {
            body.applyForceToCenter(-SPEED * 50, 0, true);
            // Limiter la vitesse maximale
            if (body.getLinearVelocity().x < -MAX_SPEED) {
                body.setLinearVelocity(-MAX_SPEED, body.getLinearVelocity().y);
            }
        }
    }
    
    public void moveRight() {
        if (!isCrouching) {
            body.applyForceToCenter(SPEED * 50, 0, true);
            // Limiter la vitesse maximale
            if (body.getLinearVelocity().x > MAX_SPEED) {
                body.setLinearVelocity(MAX_SPEED, body.getLinearVelocity().y);
            }
        }
    }
    
    public void stop() {
        // Appliquer un frottement pour arrêter le joueur progressivement
        Vector2 velocity = body.getLinearVelocity();
        velocity.x *= DAMPING;
        if (Math.abs(velocity.x) < 0.1f) velocity.x = 0;
        body.setLinearVelocity(velocity);
    }
    
    public void crouch(boolean crouch) {
        if (isJumping) return;
        
        isCrouching = crouch;
        if (crouch) {
            // Réduire la taille de la hitbox
            fixture.getShape().setAsBox(8 / MarioGame.PPM, 10 / MarioGame.PPM, new Vector2(0, -5 / MarioGame.PPM), 0);
        } else {
            // Restaurer la taille normale
            fixture.getShape().setAsBox(8 / MarioGame.PPM, 15 / MarioGame.PPM, new Vector2(0, 0), 0);
        }
    }
    
    public void collectCoin() {
        coins++;
        score += 100;
        // coinSound.play(0.7f);
    }
    
    public void takeDamage() {
        if (isInvincible) return;
        
        lives--;
        if (lives <= 0) {
            lives = 0;
            // Déclencher l'événement de game over
            if (onPlayerDeath != null) {
                onPlayerDeath.onDeath();
            }
        } else {
            // Activer l'invincibilité
            isInvincible = true;
            invincibleTimer = INVINCIBLE_DURATION;
            
            // Effet de recul
            float direction = isFacingRight ? -1 : 1;
            body.setLinearVelocity(direction * 5, 10);
            
            if (hurtSound != null) {
                hurtSound.play(0.7f);
            }
        }
    }
    
    // Interface pour gérer la mort du joueur
    public interface OnPlayerDeath {
        void onDeath();
    }
    
    private OnPlayerDeath onPlayerDeath;
    
    public void setOnPlayerDeath(OnPlayerDeath listener) {
        this.onPlayerDeath = listener;
    }
    
    public void landed() {
        isJumping = false;
        // Réinitialiser l'animation de saut
        if (currentState == State.JUMPING || currentState == State.FALLING) {
            stateTime = 0;
        }
    }
    
    public void dispose() {
        // Les ressources sont gérées par le GameManager
        playerAtlas.dispose();
    }
    
    // Getters
    public Body getBody() { return body; }
    public int getLives() { return lives; }
    public int getCoins() { return coins; }
    public int getScore() { return (int)score; }
    public boolean isFacingRight() { return facingRight; }
    public boolean isInvincible() { return isInvincible; }
    public State getState() { return currentState; }
}
