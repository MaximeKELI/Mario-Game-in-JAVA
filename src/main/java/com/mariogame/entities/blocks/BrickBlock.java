package com.mariogame.entities.blocks;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.mariogame.entities.Entity;
import com.mariogame.entities.Player;
import com.mariogame.managers.GameManager;
import com.mariogame.managers.SoundManager;
import com.mariogame.utils.Constants.CollisionBits;

/**
 * Bloc de brique qui peut être détruit par le joueur s'il est suffisamment puissant.
 */
public class BrickBlock extends Entity {
    private enum State {
        NORMAL,
        HIT,
        DESTROYED
    }
    
    private State currentState = State.NORMAL;
    private float stateTime = 0f;
    private boolean isBouncing = false;
    private float bounceTimer = 0f;
    private final float BOUNCE_DURATION = 0.2f;
    private final float BOUNCE_HEIGHT = 0.1f;
    private float originalY;
    
    // Contenu du bloc (optionnel)
    private QuestionBlock.ContentType hiddenContent = null;
    private boolean hasHiddenContent = false;
    
    // Références
    private final SoundManager soundManager;
    private final GameManager gameManager;
    
    // Animations
    private Animation<TextureRegion> normalAnimation;
    private Animation<TextureRegion> hitAnimation;
    private TextureRegion currentFrame;
    
    public BrickBlock(World world, float x, float y) {
        this(world, x, y, null);
    }
    
    public BrickBlock(World world, float x, float y, QuestionBlock.ContentType hiddenContent) {
        super(world, x, y, 1.0f, 1.0f);
        this.originalY = y;
        
        if (hiddenContent != null) {
            this.hiddenContent = hiddenContent;
            this.hasHiddenContent = true;
        }
        
        // Initialiser les gestionnaires
        this.soundManager = SoundManager.getInstance();
        this.gameManager = GameManager.getInstance();
        
        // Charger les animations (à implémenter avec l'AssetManager)
        // loadAnimations();
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
        fixtureDef.friction = 0.6f;
        
        // Définir les filtres de collision
        fixtureDef.filter.categoryBits = CollisionBits.BLOCK;
        fixtureDef.filter.maskBits = (short) (
            CollisionBits.PLAYER | 
            CollisionBits.ENEMY | 
            CollisionBits.ITEM |
            CollisionBits.SHELL
        );
        
        // Créer la fixture principale
        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData("brick_block");
        
        // Créer un capteur pour détecter les collisions par le bas
        createBottomSensor();
        
        // Libérer la forme
        shape.dispose();
    }
    
    private void createBottomSensor() {
        // Créer un capteur sous le bloc
        PolygonShape sensorShape = new PolygonShape();
        sensorShape.setAsBox(
            width / 2.2f, 
            0.1f, 
            new Vector2(0, -height / 2), 
            0
        );
        
        FixtureDef sensorDef = new FixtureDef();
        sensorDef.shape = sensorShape;
        sensorDef.isSensor = true;
        sensorDef.filter.categoryBits = CollisionBits.BLOCK;
        sensorDef.filter.maskBits = CollisionBits.PLAYER_HEAD;
        
        Fixture sensor = body.createFixture(sensorDef);
        sensor.setUserData("brick_block_bottom");
        
        sensorShape.dispose();
    }
    
    @Override
    public void update(float deltaTime) {
        if (currentState == State.DESTROYED) return;
        
        // Mettre à jour le temps d'état
        stateTime += deltaTime;
        
        // Gérer l'animation de rebond
        if (isBouncing) {
            bounceTimer += deltaTime;
            
            if (bounceTimer >= BOUNCE_DURATION) {
                isBouncing = false;
                bounceTimer = 0;
                body.setTransform(position.x, originalY, 0);
                
                // Si le bloc doit être détruit après le rebond
                if (currentState == State.HIT) {
                    destroy();
                }
            } else {
                // Calculer la hauteur du rebond
                float progress = bounceTimer / BOUNCE_DURATION;
                float bounceOffset = (float) Math.sin(progress * Math.PI) * BOUNCE_HEIGHT;
                body.setTransform(position.x, originalY + bounceOffset, 0);
            }
        }
        
        // Mettre à jour l'animation
        updateAnimation();
    }
    
    private void updateAnimation() {
        switch (currentState) {
            case NORMAL:
                currentFrame = normalAnimation.getKeyFrame(stateTime, true);
                break;
            case HIT:
                currentFrame = hitAnimation.getKeyFrame(stateTime, false);
                break;
            case DESTROYED:
                // Ne plus afficher le bloc
                break;
        }
    }
    
    @Override
    public void render(SpriteBatch batch) {
        if (currentFrame == null || currentState == State.DESTROYED) return;
        
        // Dessiner le bloc
        float x = position.x - width / 2;
        float y = position.y - height / 2;
        
        batch.draw(currentFrame, x, y, width, height);
    }
    
    /**
     * Appelé lorsque le joueur frappe le bloc par le dessous.
     * @param player Le joueur qui a frappé le bloc
     */
    public void hit(Player player) {
        if (currentState != State.NORMAL) return;
        
        // Vérifier si le joueur est suffisamment puissant pour casser le bloc
        if (player.isBig() || player.hasFirePower()) {
            // Le joueur peut casser le bloc
            soundManager.playSound("break_block", 0.5f);
            
            // Faire apparaître le contenu caché si présent
            if (hasHiddenContent) {
                spawnHiddenContent();
            }
            
            // Détruire le bloc après un léger délai
            currentState = State.HIT;
            isBouncing = true;
            bounceTimer = 0;
            
            // Ajouter des points
            gameManager.addScore(50);
        } else {
            // Le joueur est trop petit, le bloc rebondit simplement
            soundManager.playSound("bump", 0.5f);
            isBouncing = true;
            bounceTimer = 0;
        }
    }
    
    /**
     * Fait apparaître le contenu caché du bloc.
     */
    private void spawnHiddenContent() {
        if (!hasHiddenContent || hiddenContent == null) return;
        
        // Créer un bloc de question temporaire pour gérer le contenu
        QuestionBlock tempBlock = new QuestionBlock(world, position.x, position.y, hiddenContent);
        tempBlock.hit(null); // Activer le contenu sans avoir besoin d'un joueur
        
        hasHiddenContent = false;
    }
    
    /**
     * Détruit le bloc.
     */
    private void destroy() {
        currentState = State.DESTROYED;
        
        // Créer des particules de débris
        // particleManager.createEffect("brick_break", position.x, position.y);
        
        // Marquer pour suppression
        remove();
    }
    
    /**
     * Vérifie si le bloc est détruit.
     */
    public boolean isDestroyed() {
        return currentState == State.DESTROYED;
    }
    
    @Override
    public void dispose() {
        super.dispose();
        // Libérer les ressources des animations si nécessaire
    }
}
