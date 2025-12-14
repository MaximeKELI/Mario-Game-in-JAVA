package com.mariogame.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.mariogame.managers.AudioManager;
import com.mariogame.managers.InputManager;
import com.mariogame.utils.Constants;
import com.mariogame.utils.Constants.PlayerState;
import com.mariogame.world.GameWorld;

/**
 * Classe professionnelle représentant le personnage jouable (Mario).
 * Gère les contrôles, l'animation et la physique du joueur avec un niveau de qualité AAA.
 * Architecture inspirée des meilleures pratiques de l'industrie du jeu vidéo.
 */
public class Player extends Entity {
    // Constantes de contrôle (utilisant les constantes du jeu)
    private static final float MOVE_FORCE = 10f;
    private static final float MAX_SPEED = Constants.PlayerConfig.RUN_SPEED;
    private static final float WALK_SPEED = Constants.PlayerConfig.WALK_SPEED;
    private static final float JUMP_FORCE = Constants.PlayerConfig.JUMP_FORCE;
    private static final float DASH_FORCE = 15f;
    private static final float DASH_DURATION = 0.15f;
    private static final float DASH_COOLDOWN = 0.5f;
    private static final float WALL_SLIDE_SPEED = 1f;
    private static final float WALL_JUMP_FORCE_X = 8f;
    private static final float WALL_JUMP_FORCE_Y = 10f;
    private static final float COYOTE_TIME = 0.15f;
    private static final float JUMP_BUFFER_TIME = 0.1f;
    private static final float ANIMATION_FPS = 12f;
    
    // États du joueur
    private PlayerState state = PlayerState.IDLE;
    private PlayerState previousState = PlayerState.IDLE;
    private boolean facingRight = true;
    private boolean isOnGround = false;
    private boolean isWallSliding = false;
    private boolean isDashing = false;
    private boolean canDash = true;
    private boolean isDead = false;
    
    // Compteurs et temporisateurs
    private float dashTime = 0f;
    private float dashCooldown = 0f;
    private float coyoteTime = 0f;
    private float jumpBufferTime = 0f;
    private float invincibleTimer = 0f;
    private float blinkTimer = 0f;
    private boolean isBlinking = false;
    
    // Propriétés du joueur
    private int lives = Constants.PlayerConfig.STARTING_LIVES;
    private int coins = 0;
    private int score = 0;
    private boolean isInvincible = false;
    private boolean hasPowerUp = false;
    
    // Références
    private GameWorld gameWorld;
    private InputManager inputManager;
    private AudioManager audioManager;
    private AssetManager assetManager;
    
    // Animations
    private Animation<TextureRegion> idleAnimation;
    private Animation<TextureRegion> walkAnimation;
    private Animation<TextureRegion> runAnimation;
    private Animation<TextureRegion> jumpAnimation;
    private Animation<TextureRegion> fallAnimation;
    private Animation<TextureRegion> wallSlideAnimation;
    private Animation<TextureRegion> dashAnimation;
    private Animation<TextureRegion> currentAnimation;
    
    // Capteurs de collision
    private Fixture groundSensor;
    private Fixture leftWallSensor;
    private Fixture rightWallSensor;
    
    public Player(World world, float x, float y) {
        super(world, x, y, Constants.PlayerConfig.WIDTH, Constants.PlayerConfig.HEIGHT);
        createBody();
    }
    
    /**
     * Initialise les références aux managers.
     */
    public void initialize(InputManager inputManager, AudioManager audioManager, AssetManager assetManager) {
        this.inputManager = inputManager;
        this.audioManager = audioManager;
        this.assetManager = assetManager;
        loadAnimations();
    }
    
    @Override
    protected void createBody() {
        // Définir la position et le type du corps
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(position);
        bodyDef.fixedRotation = true;
        
        // Créer le corps dans le monde
        body = world.createBody(bodyDef);
        body.setUserData(this);
        
        // Créer la forme de collision principale
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width / 2, height / 2);
        
        // Définir les propriétés physiques
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = Constants.PlayerConfig.DENSITY;
        fixtureDef.friction = Constants.PlayerConfig.FRICTION;
        fixtureDef.restitution = Constants.PlayerConfig.RESTITUTION;
        
        // Définir les filtres de collision
        fixtureDef.filter.categoryBits = Constants.CollisionBits.PLAYER;
        fixtureDef.filter.maskBits = Constants.CollisionBits.Masks.PLAYER;
        
        // Créer la fixture principale
        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData("player_main");
        
        // Créer les capteurs
        createGroundSensor();
        createWallSensors();
        
        // Libérer la forme
        shape.dispose();
    }
    
    private void createGroundSensor() {
        PolygonShape sensorShape = new PolygonShape();
        sensorShape.setAsBox(width / 2.2f, 0.1f, new Vector2(0, -height / 2), 0);
        
        FixtureDef sensorDef = new FixtureDef();
        sensorDef.shape = sensorShape;
        sensorDef.isSensor = true;
        sensorDef.filter.categoryBits = Constants.CollisionBits.PLAYER_FOOT;
        sensorDef.filter.maskBits = Constants.CollisionBits.Masks.PLAYER_FOOT;
        
        groundSensor = body.createFixture(sensorDef);
        groundSensor.setUserData("player_foot_sensor");
        
        sensorShape.dispose();
    }
    
    private void createWallSensors() {
        // Capteur pour le mur à droite
        leftWallSensor = createWallSensor(-width / 2 - 0.05f, 0, "left_wall_sensor");
        // Capteur pour le mur à gauche
        rightWallSensor = createWallSensor(width / 2 + 0.05f, 0, "right_wall_sensor");
    }
    
    private Fixture createWallSensor(float x, float y, String userData) {
        PolygonShape wallShape = new PolygonShape();
        wallShape.setAsBox(0.1f, height / 2 - 0.1f, new Vector2(x, y), 0);
        
        FixtureDef wallDef = new FixtureDef();
        wallDef.shape = wallShape;
        wallDef.isSensor = true;
        wallDef.filter.categoryBits = Constants.CollisionBits.PLAYER;
        wallDef.filter.maskBits = (short) (Constants.CollisionBits.GROUND | Constants.CollisionBits.WALL);
        
        Fixture wallSensor = body.createFixture(wallDef);
        wallSensor.setUserData(userData);
        
        wallShape.dispose();
        return wallSensor;
    }
    
    /**
     * Charge les animations depuis l'atlas de textures.
     */
    private void loadAnimations() {
        if (assetManager == null) {
            Gdx.app.error("Player", "AssetManager not initialized, using placeholder animations");
            createPlaceholderAnimations();
            return;
        }
        
        try {
            String atlasPath = Constants.Assets.Atlases.PLAYER;
            if (assetManager.isLoaded(atlasPath, TextureAtlas.class)) {
                TextureAtlas atlas = assetManager.get(atlasPath, TextureAtlas.class);
                
                // Charger les animations depuis l'atlas
                idleAnimation = new Animation<>(1f / ANIMATION_FPS, atlas.findRegions("idle"));
                walkAnimation = new Animation<>(1f / ANIMATION_FPS, atlas.findRegions("walk"));
                runAnimation = new Animation<>(1f / ANIMATION_FPS, atlas.findRegions("run"));
                jumpAnimation = new Animation<>(1f / ANIMATION_FPS, atlas.findRegions("jump"));
                fallAnimation = new Animation<>(1f / ANIMATION_FPS, atlas.findRegions("fall"));
                wallSlideAnimation = new Animation<>(1f / ANIMATION_FPS, atlas.findRegions("wall_slide"));
                dashAnimation = new Animation<>(1f / ANIMATION_FPS, atlas.findRegions("dash"));
                
                currentAnimation = idleAnimation;
            } else {
                Gdx.app.warn("Player", "Player atlas not loaded, using placeholder animations");
                createPlaceholderAnimations();
            }
        } catch (Exception e) {
            Gdx.app.error("Player", "Error loading animations", e);
            createPlaceholderAnimations();
        }
    }
    
    /**
     * Crée des animations de placeholder si les vraies animations ne sont pas disponibles.
     */
    private void createPlaceholderAnimations() {
        // Créer une texture de placeholder simple
        TextureRegion placeholder = new TextureRegion();
        idleAnimation = new Animation<>(1f, placeholder);
        walkAnimation = new Animation<>(1f, placeholder);
        runAnimation = new Animation<>(1f, placeholder);
        jumpAnimation = new Animation<>(1f, placeholder);
        fallAnimation = new Animation<>(1f, placeholder);
        wallSlideAnimation = new Animation<>(1f, placeholder);
        dashAnimation = new Animation<>(1f, placeholder);
        currentAnimation = idleAnimation;
    }
    
    @Override
    public void update(float deltaTime) {
        if (isDead || body == null) return;
        
        super.update(deltaTime);
        
        // Gérer l'invincibilité
        updateInvincibility(deltaTime);
        
        // Gérer les entrées utilisateur
        if (inputManager != null) {
            handleInput(deltaTime);
        }
        
        // Appliquer la physique personnalisée
        applyPhysics(deltaTime);
        
        // Mettre à jour les animations
        updateAnimations(deltaTime);
        
        // Mettre à jour les états
        updateState(deltaTime);
        
        // Détecter le sol via les contacts
        updateGroundDetection();
    }
    
    /**
     * Met à jour la détection du sol via les contacts Box2D.
     */
    private void updateGroundDetection() {
        isOnGround = false;
        
        if (groundSensor != null) {
            for (Contact contact : world.getContactList()) {
                if (!contact.isTouching()) continue;
                
                Fixture fixtureA = contact.getFixtureA();
                Fixture fixtureB = contact.getFixtureB();
                
                if ((fixtureA == groundSensor || fixtureB == groundSensor)) {
                    isOnGround = true;
                    break;
                }
            }
        }
    }
    
    private void handleInput(float deltaTime) {
        if (inputManager == null) return;
        
        // Réinitialiser l'état de glissade sur les murs
        isWallSliding = false;
        
        // Gestion du mouvement horizontal
        float moveInput = inputManager.getHorizontalAxis();
        boolean isRunning = inputManager.isRunPressed();
        
        // Mettre à jour la direction
        if (moveInput > 0 && !facingRight) {
            facingRight = true;
        } else if (moveInput < 0 && facingRight) {
            facingRight = false;
        }
        
        // Appliquer une force de mouvement
        if (!isDashing && moveInput != 0) {
            float targetSpeed = isRunning ? MAX_SPEED : WALK_SPEED;
            float targetVelocity = moveInput * targetSpeed;
            float velocityDiff = targetVelocity - body.getLinearVelocity().x;
            float force = body.getMass() * velocityDiff / (deltaTime > 0 ? deltaTime : 1f/60f);
            body.applyForceToCenter(force, 0, true);
        }
        
        // Gestion du saut
        if (inputManager.isJumpJustPressed()) {
            jumpBufferTime = JUMP_BUFFER_TIME;
        } else if (jumpBufferTime > 0) {
            jumpBufferTime -= deltaTime;
        }
        
        // Gestion du dash
        if (inputManager.isRunPressed() && canDash && !isDashing) {
            startDash();
        }
        
        // Mise à jour du dash
        if (isDashing) {
            updateDash(deltaTime);
        }
        
        // Gestion du saut depuis un mur ou le sol
        if (jumpBufferTime > 0 && (isOnGround || coyoteTime > 0 || isWallSliding)) {
            jump();
            jumpBufferTime = 0;
        }
        
        // Mise à jour du cooldown du dash
        if (dashCooldown > 0) {
            dashCooldown -= deltaTime;
        } else if (!canDash && isOnGround) {
            canDash = true;
        }
    }
    
    private void applyPhysics(float deltaTime) {
        // Limiter la vitesse horizontale
        Vector2 velocity = body.getLinearVelocity();
        float maxSpeed = isDashing ? MAX_SPEED * 1.5f : MAX_SPEED;
        velocity.x = MathUtils.clamp(velocity.x, -maxSpeed, maxSpeed);
        body.setLinearVelocity(velocity);
        
        // Mise à jour du temps de coyote
        if (isOnGround) {
            coyoteTime = COYOTE_TIME;
        } else if (coyoteTime > 0) {
            coyoteTime -= deltaTime;
        }
        
        // Gestion de la glissade sur les murs
        handleWallSliding();
    }
    
    private void handleWallSliding() {
        boolean touchingLeftWall = isTouchingLeftWall();
        boolean touchingRightWall = isTouchingRightWall();
        
        if ((touchingLeftWall || touchingRightWall) && !isOnGround && body.getLinearVelocity().y < 0) {
            isWallSliding = true;
            
            // Ralentir la chute contre le mur
            Vector2 velocity = body.getLinearVelocity();
            velocity.y = Math.max(velocity.y, -WALL_SLIDE_SPEED);
            body.setLinearVelocity(velocity.x, velocity.y);
            
            // Déterminer la direction du mur
            if (touchingLeftWall) {
                facingRight = true;
            } else {
                facingRight = false;
            }
        }
    }
    
    private void jump() {
        if (isOnGround || coyoteTime > 0) {
            // Saut normal
            body.setLinearVelocity(body.getLinearVelocity().x, 0);
            body.applyLinearImpulse(new Vector2(0, JUMP_FORCE), body.getWorldCenter(), true);
            if (audioManager != null) {
                audioManager.playSound(Constants.Assets.Sounds.JUMP, 0.5f);
            }
            isOnGround = false;
            coyoteTime = 0;
        } else if (isWallSliding) {
            // Saut mural
            float direction = isTouchingLeftWall() ? 1 : -1;
            body.setLinearVelocity(0, 0);
            body.applyLinearImpulse(
                new Vector2(WALL_JUMP_FORCE_X * direction, WALL_JUMP_FORCE_Y), 
                body.getWorldCenter(), 
                true
            );
            if (audioManager != null) {
                audioManager.playSound(Constants.Assets.Sounds.JUMP, 0.5f);
            }
            isWallSliding = false;
        }
    }
    
    private void startDash() {
        if (!canDash || dashCooldown > 0) return;
        
        isDashing = true;
        canDash = false;
        dashTime = DASH_DURATION;
        dashCooldown = DASH_COOLDOWN;
        
        // Appliquer la force de dash dans la direction actuelle
        float direction = facingRight ? 1 : -1;
        body.setLinearVelocity(0, 0);
        body.applyLinearImpulse(
            new Vector2(DASH_FORCE * direction, 0), 
            body.getWorldCenter(), 
            true
        );
        
        if (audioManager != null) {
            audioManager.playSound(Constants.Assets.Sounds.POWERUP, 0.7f);
        }
    }
    
    private void updateDash(float deltaTime) {
        dashTime -= deltaTime;
        
        if (dashTime <= 0) {
            isDashing = false;
            
            // Ralentir après le dash
            Vector2 velocity = body.getLinearVelocity();
            velocity.x *= 0.5f;
            body.setLinearVelocity(velocity);
        }
    }
    
    private void updateInvincibility(float deltaTime) {
        if (isInvincible) {
            invincibleTimer -= deltaTime;
            blinkTimer += deltaTime;
            
            // Clignotement pendant l'invincibilité
            if (blinkTimer >= 0.1f) {
                isBlinking = !isBlinking;
                blinkTimer = 0;
            }
            
            // Fin de l'invincibilité
            if (invincibleTimer <= 0) {
                isInvincible = false;
                isBlinking = false;
            }
        }
    }
    
    private void updateState(float deltaTime) {
        previousState = state;
        
        // Mettre à jour l'état en fonction de la vitesse et des entrées
        Vector2 velocity = body.getLinearVelocity();
        
        if (isDashing) {
            state = PlayerState.DASHING;
        } else if (!isOnGround && coyoteTime <= 0) {
            if (isWallSliding) {
                state = PlayerState.WALL_SLIDING;
            } else if (velocity.y > 0) {
                state = PlayerState.JUMPING;
            } else {
                state = PlayerState.FALLING;
            }
        } else if (Math.abs(velocity.x) > 0.1f) {
            state = inputManager != null && inputManager.isRunPressed() ? PlayerState.RUNNING : PlayerState.WALKING;
        } else {
            state = PlayerState.IDLE;
        }
        
        // Réinitialiser le temps d'état si l'état a changé
        if (state != previousState) {
            stateTime = 0;
        }
    }
    
    private void updateAnimations(float deltaTime) {
        // Mettre à jour l'animation en fonction de l'état
        switch (state) {
            case IDLE:
                currentAnimation = idleAnimation;
                break;
            case WALKING:
                currentAnimation = walkAnimation;
                break;
            case RUNNING:
                currentAnimation = runAnimation;
                break;
            case JUMPING:
                currentAnimation = jumpAnimation;
                break;
            case FALLING:
                currentAnimation = fallAnimation;
                break;
            case WALL_SLIDING:
                currentAnimation = wallSlideAnimation;
                break;
            case DASHING:
                currentAnimation = dashAnimation;
                break;
            default:
                currentAnimation = idleAnimation;
                break;
        }
    }
    
    @Override
    public void render(SpriteBatch batch) {
        if (isDead || (isBlinking && currentAnimation == null)) return;
        
        if (currentAnimation == null) {
            return;
        }
        
        // Récupérer la frame d'animation actuelle
        TextureRegion currentFrame = currentAnimation.getKeyFrame(stateTime, true);
        
        if (currentFrame == null) return;
        
        // Dessiner le joueur
        float drawX = (position.x - width / 2) * Constants.WorldConfig.PPM;
        float drawY = (position.y - height / 2) * Constants.WorldConfig.PPM;
        float drawWidth = width * Constants.WorldConfig.PPM;
        float drawHeight = height * Constants.WorldConfig.PPM;
        
        // Inverser le sprite si nécessaire
        boolean flipX = !facingRight;
        if (flipX != currentFrame.isFlipX()) {
            currentFrame.flip(true, false);
        }
        
        // Dessiner le sprite
        batch.draw(currentFrame, drawX, drawY, drawWidth, drawHeight);
    }
    
    // Méthodes utilitaires pour la détection des murs
    
    public boolean isTouchingLeftWall() {
        if (leftWallSensor == null) return false;
        
        for (Contact contact : world.getContactList()) {
            if (contact.isTouching()) {
                Fixture fixtureA = contact.getFixtureA();
                Fixture fixtureB = contact.getFixtureB();
                if ((fixtureA == leftWallSensor || fixtureB == leftWallSensor)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public boolean isTouchingRightWall() {
        if (rightWallSensor == null) return false;
        
        for (Contact contact : world.getContactList()) {
            if (contact.isTouching()) {
                Fixture fixtureA = contact.getFixtureA();
                Fixture fixtureB = contact.getFixtureB();
                if ((fixtureA == rightWallSensor || fixtureB == rightWallSensor)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public void takeDamage() {
        if (isInvincible || isDead) return;
        
        if (hasPowerUp) {
            // Perdre le power-up au lieu d'une vie
            hasPowerUp = false;
            if (audioManager != null) {
                audioManager.playSound(Constants.Assets.Sounds.POWERUP, 0.7f);
            }
            
            // Rendre invincible brièvement
            isInvincible = true;
            invincibleTimer = Constants.PlayerConfig.INVINCIBILITY_DURATION;
        } else {
            // Perdre une vie
            lives--;
            
            if (lives <= 0) {
                die();
            } else {
                // Rendre invincible brièvement
                isInvincible = true;
                invincibleTimer = Constants.PlayerConfig.INVINCIBILITY_DURATION;
                if (audioManager != null) {
                    audioManager.playSound(Constants.Assets.Sounds.HURT, 0.7f);
                }
                
                // Repousser le joueur
                float direction = facingRight ? -1 : 1;
                body.applyLinearImpulse(new Vector2(5f * direction, 5f), body.getWorldCenter(), true);
            }
        }
    }
    
    public void die() {
        if (isDead) return;
        
        isDead = true;
        if (audioManager != null) {
            audioManager.playSound(Constants.Assets.Sounds.HURT, 1f);
        }
        
        // Désactiver les collisions
        if (body != null) {
            body.setActive(false);
        }
    }
    
    public void collectCoin() {
        coins++;
        score += 100;
        if (audioManager != null) {
            audioManager.playSound(Constants.Assets.Sounds.COIN, 0.3f);
        }
        
        // Vérifier les vies supplémentaires
        if (coins % 100 == 0) {
            lives++;
            if (audioManager != null) {
                audioManager.playSound(Constants.Assets.Sounds.POWERUP, 0.7f);
            }
        }
    }
    
    public void collectPowerUp() {
        hasPowerUp = true;
        if (audioManager != null) {
            audioManager.playSound(Constants.Assets.Sounds.POWERUP, 0.7f);
        }
    }
    
    // Getters et setters
    
    public PlayerState getState() {
        return state;
    }
    
    public boolean isFacingRight() {
        return facingRight;
    }
    
    public void setFacingRight(boolean facingRight) {
        this.facingRight = facingRight;
    }
    
    public int getLives() {
        return lives;
    }
    
    public int getCoins() {
        return coins;
    }
    
    public int getScore() {
        return score;
    }
    
    public boolean hasPowerUp() {
        return hasPowerUp;
    }
    
    public boolean isInvincible() {
        return isInvincible;
    }
    
    public boolean isDead() {
        return isDead;
    }
    
    public void setOnGround(boolean onGround) {
        this.isOnGround = onGround;
    }
    
    public void setGameWorld(GameWorld gameWorld) {
        this.gameWorld = gameWorld;
        setWorld(gameWorld);
    }
    
    @Override
    public void dispose() {
        // Les textures sont gérées par l'AssetManager
        super.dispose();
    }
}
