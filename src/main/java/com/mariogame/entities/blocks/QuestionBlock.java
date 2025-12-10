package com.mariogame.entities.blocks;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.mariogame.entities.Entity;
import com.mariogame.entities.Player;
import com.mariogame.entities.items.Coin;
import com.mariogame.entities.items.Mushroom;
import com.mariogame.managers.GameManager;
import com.mariogame.managers.SoundManager;
import com.mariogame.utils.Constants.CollisionBits;

/**
 * Bloc à points d'interrogation qui peut contenir des pièces ou des objets spéciaux.
 */
public class QuestionBlock extends Entity {
    private enum State {
        NORMAL,
        HIT,
        EMPTY
    }
    
    private State currentState = State.NORMAL;
    private float stateTime = 0f;
    private boolean isBouncing = false;
    private float bounceTimer = 0f;
    private final float BOUNCE_DURATION = 0.2f;
    private final float BOUNCE_HEIGHT = 0.2f;
    private float originalY;
    
    // Contenu du bloc
    private ContentType contentType;
    private boolean hasContent = true;
    
    // Références
    private final SoundManager soundManager;
    private final GameManager gameManager;
    
    // Animations
    private Animation<TextureRegion> normalAnimation;
    private Animation<TextureRegion> hitAnimation;
    private TextureRegion emptyTexture;
    private TextureRegion currentFrame;
    
    public enum ContentType {
        COIN,       // Pièce simple
        MULTI_COIN, // Plusieurs pièces (jusqu'à 10)
        MUSHROOM,   // Champignon (vie supplémentaire ou grandissement)
        FIRE_FLOWER,// Fleur de feu (si le joueur est grand)
        STAR        // Étoile d'invincibilité
    }
    
    public QuestionBlock(World world, float x, float y, ContentType contentType) {
        super(world, x, y, 1.0f, 1.0f);
        this.contentType = contentType;
        this.originalY = y;
        
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
        bodyDef.type = BodyDef.BodyType.StaticBody;
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
        fixture.setUserData("question_block");
        
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
        sensor.setUserData("question_block_bottom");
        
        sensorShape.dispose();
    }
    
    @Override
    public void update(float deltaTime) {
        if (currentState == State.EMPTY) return;
        
        // Mettre à jour le temps d'état
        stateTime += deltaTime;
        
        // Gérer l'animation de rebond
        if (isBouncing) {
            bounceTimer += deltaTime;
            
            if (bounceTimer >= BOUNCE_DURATION) {
                isBouncing = false;
                bounceTimer = 0;
                body.setTransform(position.x, originalY, 0);
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
                // Passer à l'état EMPTY une fois l'animation terminée
                if (hitAnimation.isAnimationFinished(stateTime)) {
                    currentState = State.EMPTY;
                    currentFrame = emptyTexture;
                }
                break;
            case EMPTY:
                currentFrame = emptyTexture;
                break;
        }
    }
    
    @Override
    public void render(SpriteBatch batch) {
        if (currentFrame == null) return;
        
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
        if (currentState != State.NORMAL || !hasContent) return;
        
        // Jouer le son de frappe
        soundManager.playSound("bump", 0.5f);
        
        // Faire rebondir le bloc
        isBouncing = true;
        bounceTimer = 0;
        
        // Changer l'état du bloc
        currentState = State.HIT;
        stateTime = 0;
        
        // Générer le contenu du bloc
        spawnContent();
    }
    
    /**
     * Fait apparaître le contenu du bloc.
     */
    private void spawnContent() {
        if (!hasContent) return;
        
        hasContent = false;
        
        switch (contentType) {
            case COIN:
                spawnCoin();
                break;
                
            case MULTI_COIN:
                // À implémenter : faire apparaître plusieurs pièces
                spawnCoin();
                break;
                
            case MUSHROOM:
                spawnMushroom();
                break;
                
            case FIRE_FLOWER:
                // À implémenter : faire apparaître une fleur de feu
                spawnMushroom(); // Temporaire
                break;
                
            case STAR:
                // À implémenter : faire apparaître une étoile
                spawnMushroom(); // Temporaire
                break;
        }
    }
    
    private void spawnCoin() {
        // Créer une pièce au-dessus du bloc
        Coin coin = new Coin(world, position.x, position.y + height);
        // world.addEntity(coin); // À implémenter dans la classe World
        
        // Ajouter les points
        gameManager.addScore(200);
        gameManager.addCoin();
        
        // Jouer le son de pièce
        soundManager.playSound("coin", 0.3f);
    }
    
    private void spawnMushroom() {
        // Créer un champignon au-dessus du bloc
        Mushroom mushroom = new Mushroom(world, position.x, position.y + height);
        // world.addEntity(mushroom); // À implémenter dans la classe World
        
        // Ajouter les points
        gameManager.addScore(1000);
        
        // Jouer le son de power-up
        soundManager.playSound("powerup_appear", 0.5f);
    }
    
    @Override
    public void dispose() {
        super.dispose();
        // Libérer les ressources des animations si nécessaire
    }
}
