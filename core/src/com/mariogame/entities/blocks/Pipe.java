package com.mariogame.entities.blocks;

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
 * Tuyau qui peut être utilisé pour la téléportation ou comme entrée secrète.
 */
public class Pipe extends Entity {
    public enum PipeType {
        VERTICAL,      // Tuyau vertical standard
        HORIZONTAL,    // Tuyau horizontal (pour les niveaux souterrains)
        WARP,          // Tuyau de téléportation
        ENTRANCE,      // Entrée de niveau
        EXIT           // Sortie de niveau
    }
    
    private PipeType type;
    private boolean isEntrance;
    private String targetMap;  // Carte de destination pour la téléportation
    private Vector2 targetPosition; // Position de destination dans la carte cible
    private TextureRegion texture;
    private float width, height;
    private boolean isWarping = false;
    private float warpTimer = 0f;
    private final float WARP_DURATION = 1.5f;
    
    // Références
    private final SoundManager soundManager;
    private final GameManager gameManager;
    
    public Pipe(World world, float x, float y, PipeType type, boolean isEntrance) {
        this(world, x, y, type, isEntrance, null, null);
    }
    
    public Pipe(World world, float x, float y, PipeType type, boolean isEntrance, 
               String targetMap, Vector2 targetPosition) {
        super(world, x, y, 1.0f, 1.0f);
        this.type = type;
        this.isEntrance = isEntrance;
        this.targetMap = targetMap;
        this.targetPosition = targetPosition;
        
        // Définir la taille en fonction du type de tuyau
        if (type == PipeType.VERTICAL || type == PipeType.WARP) {
            this.width = 1.0f;
            this.height = 2.0f; // Tuyau vertical plus haut
        } else {
            this.width = 2.0f; // Tuyau horizontal plus large
            this.height = 1.0f;
        }
        
        // Initialiser les gestionnaires
        this.soundManager = SoundManager.getInstance();
        this.gameManager = GameManager.getInstance();
        
        // Charger la texture (à implémenter avec l'AssetManager)
        // texture = assets.getTexture("pipe_" + type.name().toLowerCase());
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
        
        // Créer la forme de collision principale
        createMainFixture();
        
        // Créer un capteur d'entrée si c'est une entrée de tuyau
        if (isEntrance) {
            createEntranceSensor();
        }
    }
    
    private void createMainFixture() {
        PolygonShape shape = new PolygonShape();
        
        // Ajuster la forme en fonction du type de tuyau
        if (type == PipeType.VERTICAL || type == PipeType.WARP) {
            shape.setAsBox(width / 2, height / 2);
        } else {
            shape.setAsBox(width / 2, height / 2);
        }
        
        // Définir les propriétés physiques
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.friction = 0.8f;
        
        // Définir les filtres de collision
        fixtureDef.filter.categoryBits = CollisionBits.BLOCK;
        fixtureDef.filter.maskBits = (short) (
            CollisionBits.PLAYER | 
            CollisionBits.ENEMY | 
            CollisionBits.ITEM |
            CollisionBits.SHELL
        );
        
        // Créer la fixture
        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData("pipe_" + type.name().toLowerCase());
        
        shape.dispose();
    }
    
    private void createEntranceSensor() {
        // Créer un capteur à l'entrée du tuyau
        PolygonShape sensorShape = new PolygonShape();
        
        // Positionner le capteur en fonction de l'orientation du tuyau
        if (type == PipeType.VERTICAL || type == PipeType.WARP) {
            // Capteur en haut pour les tuyaux verticaux
            sensorShape.setAsBox(
                width / 2.5f, 
                0.2f, 
                new Vector2(0, height / 2), 
                0
            );
        } else {
            // Capteur sur le côté pour les tuyaux horizontaux
            sensorShape.setAsBox(
                0.2f, 
                height / 2.5f, 
                new Vector2(width / 2, 0), 
                0
            );
        }
        
        FixtureDef sensorDef = new FixtureDef();
        sensorDef.shape = sensorShape;
        sensorDef.isSensor = true;
        sensorDef.filter.categoryBits = CollisionBits.SENSOR;
        sensorDef.filter.maskBits = CollisionBits.PLAYER;
        
        Fixture sensor = body.createFixture(sensorDef);
        sensor.setUserData("pipe_entrance");
        
        sensorShape.dispose();
    }
    
    @Override
    public void update(float deltaTime) {
        // Mettre à jour le timer de téléportation si nécessaire
        if (isWarping) {
            warpTimer += deltaTime;
            
            if (warpTimer >= WARP_DURATION) {
                isWarping = false;
                warpTimer = 0f;
                // La téléportation est gérée par le GameScreen
            }
        }
    }
    
    @Override
    public void render(SpriteBatch batch) {
        if (texture == null) return;
        
        // Dessiner le tuyau
        float x = position.x - width / 2;
        float y = position.y - height / 2;
        
        batch.draw(texture, x, y, width, height);
    }
    
    /**
     * Active la téléportation du joueur à travers le tuyau.
     * @param player Le joueur qui entre dans le tuyau
     * @return true si la téléportation a été déclenchée, false sinon
     */
    public boolean enter(Player player) {
        if (isWarping || !isEntrance) return false;
        
        // Vérifier si c'est un tuyau de téléportation
        if (type == PipeType.WARP || type == PipeType.ENTRANCE) {
            // Démarrer la séquence de téléportation
            isWarping = true;
            
            // Jouer le son de téléportation
            soundManager.playSound("pipe", 0.5f);
            
            // Désactiver les contrôles du joueur
            player.setInputEnabled(false);
            
            // Animer l'entrée dans le tuyau
            player.enterPipe(this);
            
            return true;
        }
        
        return false;
    }
    
    /**
     * Téléporte le joueur vers la destination du tuyau.
     * Cette méthode est appelée par le GameScreen après l'animation d'entrée.
     */
    public void executeWarp(Player player) {
        if (targetMap != null && targetPosition != null) {
            // Changer de niveau
            gameManager.changeLevel(targetMap, targetPosition);
        } else if (targetPosition != null) {
            // Téléporter dans le même niveau
            player.setPosition(targetPosition.x, targetPosition.y);
        }
        
        // Réactiver les contrôles du joueur
        player.setInputEnabled(true);
    }
    
    // Getters et setters
    
    public PipeType getType() {
        return type;
    }
    
    public boolean isEntrance() {
        return isEntrance;
    }
    
    public boolean isWarping() {
        return isWarping;
    }
    
    public float getWarpProgress() {
        return warpTimer / WARP_DURATION;
    }
    
    public float getWidth() {
        return width;
    }
    
    public float getHeight() {
        return height;
    }
    
    @Override
    public void dispose() {
        super.dispose();
        // Libérer les ressources si nécessaire
    }
}
