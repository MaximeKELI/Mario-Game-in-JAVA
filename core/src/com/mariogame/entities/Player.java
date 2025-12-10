package com.mariogame.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.mariogame.managers.SoundManager;
import com.mariogame.utils.Constants;
import com.mariogame.utils.Constants.PlayerState;
import com.mariogame.world.GameWorld;

/**
 * Classe représentant le personnage jouable (Mario).
 * Gère les contrôles, l'animation et la physique du joueur.
 */
public class Player extends Entity {
    // Constantes de contrôle
    private static final float MOVE_FORCE = 10f;
    private static final float MAX_SPEED = 5f;
    private static final float JUMP_FORCE = 12f;
    private static final float DASH_FORCE = 15f;
    private static final float DASH_DURATION = 0.15f;
    private static final float DASH_COOLDOWN = 0.5f;
    private static final float WALL_SLIDE_SPEED = 1f;
    private static final float WALL_JUMP_FORCE_X = 8f;
    private static final float WALL_JUMP_FORCE_Y = 10f;
    private static final float COYOTE_TIME = 0.15f;
    private static final float JUMP_BUFFER_TIME = 0.1f;
    
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
    private float stateTime = 0f;
    private float dashTime = 0f;
    private float dashCooldown = 0f;
    private float coyoteTime = 0f;
    private float jumpBufferTime = 0f;
    private float invincibleTimer = 0f;
    private float blinkTimer = 0f;
    private boolean isBlinking = false;
    
    // Propriétés du joueur
    private int lives = 3;
    private int coins = 0;
    private int score = 0;
    private boolean isInvincible = false;
    private boolean hasPowerUp = false;
    
    // Références
    private final GameWorld gameWorld;
    private final SoundManager soundManager;
    
    // Animations
    private Animation<TextureRegion> idleAnimation;
    private Animation<TextureRegion> runAnimation;
    private Animation<TextureRegion> jumpAnimation;
    private Animation<TextureRegion> fallAnimation;
    private Animation<TextureRegion> wallSlideAnimation;
    private Animation<TextureRegion> dashAnimation;
    private Animation<TextureRegion> currentAnimation;
    
    public Player(World world, float x, float y) {
        super(world, x, y, 0.8f, 1.8f);
        
        // Initialiser les animations (à implémenter)
        // loadAnimations();
        
        // Initialiser le corps physique
        createBody();
        
        // Initialiser le gestionnaire de son
        this.soundManager = new SoundManager(this);
        
        // Référence au monde de jeu
        this.gameWorld = null; // Sera défini par GameWorld
    }
    
    @Override
    protected void createBody() {
        // Définir la position et le type du corps
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(position);
        bodyDef.fixedRotation = true; // Empêcher la rotation
        
        // Créer le corps dans le monde
        body = world.createBody(bodyDef);
        body.setUserData(this);
        
        // Créer la forme de collision (hitbox)
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width / 2, height / 2);
        
        // Définir les propriétés physiques
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1f;
        fixtureDef.friction = 0.2f;
        fixtureDef.restitution = 0.1f;
        
        // Définir les filtres de collision
        fixtureDef.filter.categoryBits = Constants.CollisionBits.PLAYER;
        fixtureDef.filter.maskBits = (short) (
            Constants.CollisionBits.GROUND |
            Constants.CollisionBits.ENEMY |
            Constants.CollisionBits.ITEM |
            Constants.CollisionBits.PLATFORM
        );
        
        // Créer la fixture principale
        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData("player_main");
        
        // Créer un capteur pour détecter le sol
        createGroundSensor();
        
        // Créer des capteurs pour détecter les murs
        createWallSensors();
        
        // Libérer la forme
        shape.dispose();
    }
    
    private void createGroundSensor() {
        // Créer un capteur sous les pieds du joueur
        PolygonShape sensorShape = new PolygonShape();
        sensorShape.setAsBox(width / 2.2f, 0.1f, new Vector2(0, -height / 2), 0);
        
        FixtureDef sensorDef = new FixtureDef();
        sensorDef.shape = sensorShape;
        sensorDef.isSensor = true;
        sensorDef.filter.categoryBits = Constants.CollisionBits.PLAYER_FOOT;
        sensorDef.filter.maskBits = (short) (Constants.CollisionBits.GROUND | Constants.CollisionBits.PLATFORM);
        
        Fixture sensor = body.createFixture(sensorDef);
        sensor.setUserData("player_foot_sensor");
        
        sensorShape.dispose();
    }
    
    private void createWallSensors() {
        // Capteur pour le mur à droite
        createWallSensor(width / 2 + 0.05f, 0, "right_wall_sensor");
        
        // Capteur pour le mur à gauche
        createWallSensor(-width / 2 - 0.05f, 0, "left_wall_sensor");
    }
    
    private void createWallSensor(float x, float y, String userData) {
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
    }
    
    @Override
    public void update(float deltaTime) {
        if (isDead) return;
        
        // Mettre à jour la position à partir du corps physique
        position.set(body.getPosition());
        
        // Gérer l'invincibilité
        updateInvincibility(deltaTime);
        
        // Gérer les entrées utilisateur
        handleInput(deltaTime);
        
        // Appliquer la physique personnalisée
        applyPhysics(deltaTime);
        
        // Mettre à jour les animations
        updateAnimations(deltaTime);
        
        // Mettre à jour les états
        updateState(deltaTime);
    }
    
    private void handleInput(float deltaTime) {
        // Réinitialiser l'état de glissade sur les murs
        isWallSliding = false;
        
        // Détecter si le joueur est contre un mur
        boolean touchingLeftWall = isTouchingLeftWall();
        boolean touchingRightWall = isTouchingRightWall();
        
        // Gestion du mouvement horizontal
        float moveInput = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
            moveInput = 1f;
            if (!facingRight) {
                facingRight = true;
                // Jouer un son de changement de direction
                soundManager.playSound("turn", 0.2f);
            }
        } else if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {
            moveInput = -1f;
            if (facingRight) {
                facingRight = false;
                // Jouer un son de changement de direction
                soundManager.playSound("turn", 0.2f);
            }
        }
        
        // Appliquer une force de mouvement
        if (!isDashing) {
            float targetVelocity = moveInput * MAX_SPEED;
            float velocityDiff = targetVelocity - body.getLinearVelocity().x;
            float force = body.getMass() * velocityDiff / (1f/60f); // deltaTime approximatif
            body.applyForceToCenter(force, 0, true);
        }
        
        // Gestion du saut
        boolean jumpPressed = Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || 
                            Gdx.input.isKeyJustPressed(Input.Keys.UP) || 
                            Gdx.input.isKeyJustPressed(Input.Keys.W);
        
        if (jumpPressed) {
            jumpBufferTime = JUMP_BUFFER_TIME;
        } else if (jumpBufferTime > 0) {
            jumpBufferTime -= deltaTime;
        }
        
        // Gestion du dash
        if (Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_LEFT) && canDash) {
            startDash();
        }
        
        // Mise à jour du dash
        if (isDashing) {
            updateDash(deltaTime);
        }
        
        // Gestion du saut depuis un mur
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
        velocity.x = MathUtils.clamp(velocity.x, -MAX_SPEED, MAX_SPEED);
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
            soundManager.playSound("jump", 0.5f);
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
            soundManager.playSound("jump", 0.5f);
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
        
        // Jouer un son de dash
        soundManager.playSound("dash", 0.7f);
        
        // Créer un effet visuel de dash
        if (gameWorld != null) {
            // gameWorld.createDashEffect(position.x, position.y, facingRight);
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
        stateTime += deltaTime;
        
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
            state = PlayerState.RUNNING;
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
        }
    }
    
    @Override
    public void render(SpriteBatch batch) {
        if (isDead || isBlinking) return;
        
        // Récupérer la frame d'animation actuelle
        TextureRegion currentFrame = currentAnimation.getKeyFrame(stateTime, true);
        
        // Dessiner le joueur
        float drawX = position.x - width / 2;
        float drawY = position.y - height / 2;
        
        // Inverser le sprite si nécessaire
        if (!facingRight && !currentFrame.isFlipX()) {
            currentFrame.flip(true, false);
        } else if (facingRight && currentFrame.isFlipX()) {
            currentFrame.flip(true, false);
        }
        
        // Dessiner le sprite
        batch.draw(currentFrame, drawX, drawY, width, height);
    }
    
    // Méthodes utilitaires
    
    public boolean isTouchingLeftWall() {
        for (Contact contact : world.getContactList()) {
            if (contact.isTouching() && 
                (contact.getFixtureA().getUserData() == "left_wall_sensor" || 
                 contact.getFixtureB().getUserData() == "left_wall_sensor")) {
                return true;
            }
        }
        return false;
    }
    
    public boolean isTouchingRightWall() {
        for (Contact contact : world.getContactList()) {
            if (contact.isTouching() && 
                (contact.getFixtureA().getUserData() == "right_wall_sensor" || 
                 contact.getFixtureB().getUserData() == "right_wall_sensor")) {
                return true;
            }
        }
        return false;
    }
    
    public void takeDamage() {
        if (isInvincible) return;
        
        if (hasPowerUp) {
            // Perdre le power-up au lieu d'une vie
            hasPowerUp = false;
            // gameWorld.createPowerDownEffect(position.x, position.y);
            soundManager.playSound("powerdown", 0.7f);
            
            // Rendre invincible brièvement
            isInvincible = true;
            invincibleTimer = 2f;
        } else {
            // Perdre une vie
            lives--;
            
            if (lives <= 0) {
                die();
            } else {
                // Rendre invincible brièvement
                isInvincible = true;
                invincibleTimer = 2f;
                soundManager.playSound("hurt", 0.7f);
                
                // Repousser le joueur
                float direction = facingRight ? -1 : 1;
                body.applyLinearImpulse(new Vector2(5f * direction, 5f), body.getWorldCenter(), true);
            }
        }
    }
    
    public void die() {
        if (isDead) return;
        
        isDead = true;
        soundManager.playSound("death", 1f);
        
        // Désactiver les collisions
        body.setActive(false);
        
        // Animation de mort
        // gameWorld.createDeathEffect(position.x, position.y);
        
        // Gérer le game over
        if (gameWorld != null) {
            // gameWorld.onPlayerDeath();
        }
    }
    
    public void collectCoin() {
        coins++;
        score += 100;
        soundManager.playSound("coin", 0.3f);
        
        // Vérifier les vies supplémentaires
        if (coins % 100 == 0) {
            lives++;
            soundManager.playSound("1up", 0.7f);
        }
    }
    
    public void collectPowerUp() {
        hasPowerUp = true;
        soundManager.playSound("powerup", 0.7f);
        // gameWorld.createPowerUpEffect(position.x, position.y);
    }
    
    // Getters et setters
    
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
    }
    
    @Override
    public void dispose() {
        // Libérer les ressources du joueur
        // (les textures sont gérées par l'AssetManager)
    }
}
