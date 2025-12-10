package com.mariogame.entities.items;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.mariogame.entities.Entity;
import com.mariogame.entities.Player;
import com.mariogame.utils.Constants.CollisionBits;

/**
 * Champignon qui rend le joueur plus grand et lui permet de casser des blocs.
 */
public class Mushroom extends Entity {
    private static final float MOVE_SPEED = 1.5f;
    private static final float GRAVITY = -15f;
    private static final float BOUNCE_VELOCITY = 5f;
    
    private TextureRegion texture;
    private boolean isActive = false;
    private boolean isConsumed = false;
    private float spawnTimer = 0.5f; // Délai avant que le champignon ne commence à bouger
    private Vector2 velocity = new Vector2();
    private boolean movingRight = true;
    
    public Mushroom(World world, float x, float y) {
        super(world, x, y, 0.8f, 0.8f);
        
        // Charger la texture (à implémenter avec l'AssetManager)
        // texture = assets.getTexture("mushroom");
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
        fixtureDef.density = 0.5f;
        fixtureDef.friction = 0.5f;
        fixtureDef.restitution = 0.1f;
        
        // Définir les filtres de collision
        fixtureDef.filter.categoryBits = CollisionBits.ITEM;
        fixtureDef.filter.maskBits = (short) (
            CollisionBits.GROUND | 
            CollisionBits.PLAYER | 
            CollisionBits.ENEMY |
            CollisionBits.ITEM_BLOCK
        );
        
        // Créer la fixture principale
        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData("mushroom");
        
        // Créer un capteur pour détecter les collisions avec les murs
        createWallSensors();
        
        // Désactiver la gravité initialement
        body.setGravityScale(0);
        
        // Désactiver les collisions jusqu'à ce que le champignon soit actif
        setActive(false);
        
        // Libérer la forme
        shape.dispose();
    }
    
    private void createWallSensors() {
        // Capteur pour le mur à droite
        createWallSensor(width / 2 + 0.05f, 0, "right_sensor");
        
        // Capteur pour le mur à gauche
        createWallSensor(-width / 2 - 0.05f, 0, "left_sensor");
    }
    
    private void createWallSensor(float x, float y, String userData) {
        PolygonShape wallShape = new PolygonShape();
        wallShape.setAsBox(0.05f, height / 2 - 0.1f, new Vector2(x, y), 0);
        
        FixtureDef wallDef = new FixtureDef();
        wallDef.shape = wallShape;
        wallDef.isSensor = true;
        wallDef.filter.categoryBits = CollisionBits.ITEM;
        wallDef.filter.maskBits = CollisionBits.GROUND;
        
        Fixture wallSensor = body.createFixture(wallDef);
        wallSensor.setUserData(userData);
        
        wallShape.dispose();
    }
    
    @Override
    public void update(float deltaTime) {
        if (isConsumed) return;
        
        // Mettre à jour la position à partir du corps physique
        if (body != null) {
            position.set(body.getPosition());
        }
        
        // Mettre à jour le timer d'activation
        if (!isActive) {
            spawnTimer -= deltaTime;
            if (spawnTimer <= 0) {
                activate();
            }
            return;
        }
        
        // Mettre à jour la vitesse
        if (body != null) {
            velocity.y += GRAVITY * deltaTime;
            velocity.x = movingRight ? MOVE_SPEED : -MOVE_SPEED;
            
            // Appliquer la vélocité
            body.setLinearVelocity(velocity);
            
            // Vérifier les collisions avec les murs
            checkWallCollisions();
        }
    }
    
    private void checkWallCollisions() {
        if (body == null) return;
        
        // Vérifier les contacts avec les murs
        for (ContactEdge edge = body.getContactList(); edge != null; edge = edge.next) {
            if (!edge.contact.isTouching()) continue;
            
            Object userDataA = edge.contact.getFixtureA().getUserData();
            Object userDataB = edge.contact.getFixtureB().getUserData();
            
            // Vérifier la collision avec un mur à droite
            if ((userDataA != null && userDataA.equals("right_sensor")) || 
                (userDataB != null && userDataB.equals("right_sensor"))) {
                movingRight = false;
                break;
            }
            
            // Vérifier la collision avec un mur à gauche
            if ((userDataA != null && userDataA.equals("left_sensor")) || 
                (userDataB != null && userDataB.equals("left_sensor"))) {
                movingRight = true;
                break;
            }
        }
    }
    
    private void activate() {
        if (isActive) return;
        
        isActive = true;
        
        // Activer la gravité et les collisions
        if (body != null) {
            body.setGravityScale(1);
            body.setActive(true);
            
            // Donner une petite impulsion vers le haut
            body.applyLinearImpulse(new Vector2(0, BOUNCE_VELOCITY), body.getWorldCenter(), true);
        }
        
        // Jouer un son d'apparition
        // soundManager.playSound("powerup_appear", 0.5f);
    }
    
    @Override
    public void render(SpriteBatch batch) {
        if (isConsumed || texture == null) return;
        
        // Dessiner le champignon
        float x = position.x - width / 2;
        float y = position.y - height / 2;
        
        // Animation de flottement
        float offsetY = (float) Math.sin(stateTime * 3) * 0.05f;
        
        batch.draw(texture, x, y + offsetY, width, height);
    }
    
    /**
     * Appelé lorsque le joueur entre en collision avec le champignon.
     * @param player Le joueur qui a collecté le champignon
     * @return true si le champignon a été consommé, false sinon
     */
    public boolean collect(Player player) {
        if (isConsumed || !isActive) return false;
        
        isConsumed = true;
        
        // Donner l'effet au joueur
        player.collectPowerUp();
        
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
