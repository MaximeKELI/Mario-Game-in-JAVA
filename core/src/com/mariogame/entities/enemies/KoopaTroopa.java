package com.mariogame.entities.enemies;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.mariogame.entities.Entity;
import com.mariogame.utils.Constants.CollisionBits;

/**
 * Classe représentant un ennemi Koopa Troopa.
 * Peut se cacher dans sa carapace et être poussé.
 */
public class KoopaTroopa extends Entity {
    private static final float WALK_SPEED = 0.8f;
    private static final float SHELL_SPEED = 8f;
    private static final float WAKE_UP_TIME = 5f;
    private static final float DAMAGE_COOLDOWN = 1f;
    
    private enum State {
        WALKING,
        SHELL,
        SHELL_MOVING,
        WAKE_UP
    }
    
    private State currentState = State.WALKING;
    private State previousState = State.WALKING;
    private float stateTime = 0f;
    private boolean facingRight = false;
    private float damageCooldown = 0f;
    private float wakeUpTimer = 0f;
    private boolean isDead = false;
    
    // Animations
    private Animation<TextureRegion> walkAnimation;
    private Animation<TextureRegion> shellAnimation;
    private TextureRegion currentFrame;
    
    public KoopaTroopa(World world, float x, float y) {
        super(world, x, y, 0.8f, 1.0f);
        
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
            CollisionBits.ITEM |
            CollisionBits.SHELL
        );
        
        // Créer la fixture principale
        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData("koopa_body");
        
        // Créer un capteur pour détecter les collisions avec le joueur par le dessus
        createHeadSensor();
        
        // Libérer la forme
        shape.dispose();
    }
    
    private void createHeadSensor() {
        // Créer un capteur au-dessus de la tête du Koopa
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
        headFixture.setUserData("koopa_head");
        
        headShape.dispose();
    }
    
    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        
        if (isDead) {
            return;
        }
        
        // Mettre à jour le temps d'état
        stateTime += deltaTime;
        
        // Mettre à jour le temps de cooldown des dégâts
        if (damageCooldown > 0) {
            damageCooldown -= deltaTime;
        }
        
        // Mettre à jour le comportement en fonction de l'état
        switch (currentState) {
            case WALKING:
                updateWalking(deltaTime);
                break;
                
            case SHELL:
                updateShell(deltaTime);
                break;
                
            case SHELL_MOVING:
                updateShellMoving(deltaTime);
                break;
                
            case WAKE_UP:
                updateWakeUp(deltaTime);
                break;
        }
        
        // Mettre à jour l'animation
        updateAnimation(deltaTime);
    }
    
    private void updateWalking(float deltaTime) {
        if (body != null) {
            // Inverser la direction si nécessaire (détection des bords)
            Vector2 velocity = body.getLinearVelocity();
            body.setLinearVelocity(
                facingRight ? WALK_SPEED : -WALK_SPEED,
                velocity.y
            );
        }
    }
    
    private void updateShell(float deltaTime) {
        // Réduire le timer de réveil
        wakeUpTimer -= deltaTime;
        
        // Commencer à se réveiller si le timer est écoulé
        if (wakeUpTimer <= 0) {
            startWakeUp();
        }
    }
    
    private void updateShellMoving(float deltaTime) {
        // Le mouvement est géré par les collisions
        if (body != null && Math.abs(body.getLinearVelocity().x) < 0.1f) {
            // Arrêter la coquille si elle est presque arrêtée
            body.setLinearVelocity(0, body.getLinearVelocity().y);
            currentState = State.SHELL;
            wakeUpTimer = WAKE_UP_TIME;
        }
    }
    
    private void updateWakeUp(float deltaTime) {
        // Vérifier si l'animation de réveil est terminée
        if (stateTime > 1.0f) { // Durée de l'animation de réveil
            currentState = State.WALKING;
            // Réactiver les collisions normales
            if (body != null) {
                for (Fixture fixture : body.getFixtureList()) {
                    if (fixture.getUserData().equals("koopa_body")) {
                        Filter filter = fixture.getFilterData();
                        filter.categoryBits = CollisionBits.ENEMY;
                        fixture.setFilterData(filter);
                    }
                }
            }
        }
    }
    
    private void updateAnimation(float deltaTime) {
        // Mettre à jour l'animation en fonction de l'état
        switch (currentState) {
            case WALKING:
            case WAKE_UP:
                currentFrame = walkAnimation.getKeyFrame(stateTime, true);
                break;
                
            case SHELL:
            case SHELL_MOVING:
                currentFrame = shellAnimation.getKeyFrame(stateTime, true);
                break;
        }
    }
    
    @Override
    public void render(SpriteBatch batch) {
        if (currentFrame == null || isDead) return;
        
        // Dessiner le Koopa
        float x = position.x - width / 2;
        float y = position.y - height / 2;
        
        // Ajuster la position et la taille en mode coquille
        float renderY = y;
        float renderHeight = height;
        
        if (currentState == State.SHELL || currentState == State.SHELL_MOVING) {
            renderY += height * 0.25f; // Ajuster la position Y pour la coquille
            renderHeight = height * 0.75f; // Réduire la hauteur
        }
        
        // Inverser le sprite si nécessaire
        if ((facingRight && currentFrame.isFlipX()) || 
            (!facingRight && !currentFrame.isFlipX())) {
            currentFrame.flip(true, false);
        }
        
        batch.draw(currentFrame, x, renderY, width, renderHeight);
    }
    
    /**
     * Appelé lorsque le Koopa est touché par le joueur.
     * @param fromAbove Indique si le joueur a touché le Koopa par le haut
     * @return true si le Koopa a été touché avec succès, false sinon
     */
    public boolean hit(boolean fromAbove) {
        if (isDead || damageCooldown > 0) return false;
        
        if (fromAbove) {
            // Le joueur a sauté sur le Koopa
            if (currentState == State.WALKING) {
                // Mettre le Koopa en mode coquille
                enterShell();
            } else if (currentState == State.SHELL) {
                // Faire glisser la coquille
                kickShell();
            } else if (currentState == State.SHELL_MOVING) {
                // Arrêter la coquille
                stopShell();
            }
            return true;
        } else {
            // Le joueur a été touché par le côté
            if (currentState == State.SHELL_MOVING) {
                // Le joueur peut donner un coup de pied dans la coquille
                kickShell();
                return true;
            } else if (damageCooldown <= 0) {
                damageCooldown = DAMAGE_COOLDOWN;
                // Faire reculer le Koopa
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
    
    private void enterShell() {
        currentState = State.SHELL;
        stateTime = 0f;
        wakeUpTimer = WAKE_UP_TIME;
        
        // Arrêter le mouvement
        if (body != null) {
            body.setLinearVelocity(0, 0);
        }
        
        // Jouer un son de coquille
        // soundManager.playSound("shell", 0.5f);
    }
    
    private void kickShell() {
        if (currentState != State.SHELL && currentState != State.SHELL_MOVING) {
            return;
        }
        
        currentState = State.SHELL_MOVING;
        stateTime = 0f;
        
        // Définir la direction du coup de pied (vers le joueur)
        // Ici, nous supposons que le joueur est à gauche ou à droite
        // Dans une implémentation réelle, vous devriez passer la position du joueur
        facingRight = !facingRight;
        
        // Appliquer une impulsion pour faire glisser la coquille
        if (body != null) {
            body.setLinearVelocity(facingRight ? SHELL_SPEED : -SHELL_SPEED, 0);
            
            // Changer les filtres de collision pour que la coquille puisse toucher les ennemis
            for (Fixture fixture : body.getFixtureList()) {
                if (fixture.getUserData().equals("koopa_body")) {
                    Filter filter = fixture.getFilterData();
                    filter.categoryBits = CollisionBits.SHELL;
                    fixture.setFilterData(filter);
                }
            }
        }
        
        // Jouer un son de coup de pied
        // soundManager.playSound("kick", 0.7f);
    }
    
    private void stopShell() {
        currentState = State.SHELL;
        stateTime = 0f;
        wakeUpTimer = WAKE_UP_TIME;
        
        if (body != null) {
            body.setLinearVelocity(0, 0);
        }
    }
    
    private void startWakeUp() {
        currentState = State.WAKE_UP;
        stateTime = 0f;
        
        // Jouer une animation de réveil
        // soundManager.playSound("wake_up", 0.5f);
    }
    
    /**
     * Appelé lorsque la coquille entre en collision avec un ennemi.
     */
    public void onShellHit() {
        // Le Koopa est déjà dans sa coquille, donc il est vaincu
        isDead = true;
        remove();
        
        // Jouer un effet de particules
        // particleManager.createEffect("enemy_death", position.x, position.y);
    }
    
    /**
     * Change la direction du déplacement du Koopa.
     */
    public void changeDirection() {
        if (currentState == State.WALKING) {
            facingRight = !facingRight;
        }
    }
    
    @Override
    public void dispose() {
        super.dispose();
        // Libérer les ressources des animations si nécessaire
    }
}
