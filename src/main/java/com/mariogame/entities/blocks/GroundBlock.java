package com.mariogame.entities.blocks;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.mariogame.entities.Entity;
import com.mariogame.utils.Constants.CollisionBits;

/**
 * Bloc de sol solide qui ne peut pas être détruit.
 */
public class GroundBlock extends Entity {
    private TextureRegion texture;
    
    public GroundBlock(World world, float x, float y) {
        super(world, x, y, 1.0f, 1.0f);
        
        // Charger la texture (à implémenter avec l'AssetManager)
        // texture = assets.getTexture("ground_block");
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
        fixtureDef.filter.categoryBits = CollisionBits.GROUND;
        fixtureDef.filter.maskBits = (short) (
            CollisionBits.PLAYER | 
            CollisionBits.ENEMY | 
            CollisionBits.ITEM |
            CollisionBits.SHELL |
            CollisionBits.PROJECTILE
        );
        
        // Créer la fixture
        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData("ground_block");
        
        // Créer un capteur pour le dessus du bloc (pour les sauts sur les ennemis)
        createTopSensor();
        
        // Libérer la forme
        shape.dispose();
    }
    
    private void createTopSensor() {
        // Créer un capteur sur le dessus du bloc
        PolygonShape sensorShape = new PolygonShape();
        sensorShape.setAsBox(
            width / 2.2f, 
            0.1f, 
            new Vector2(0, height / 2), 
            0
        );
        
        FixtureDef sensorDef = new FixtureDef();
        sensorDef.shape = sensorShape;
        sensorDef.isSensor = true;
        sensorDef.filter.categoryBits = CollisionBits.GROUND;
        sensorDef.filter.maskBits = CollisionBits.PLAYER_FOOT;
        
        Fixture sensor = body.createFixture(sensorDef);
        sensor.setUserData("ground_block_top");
        
        sensorShape.dispose();
    }
    
    @Override
    public void update(float deltaTime) {
        // Les blocs de sol ne nécessitent pas de mise à jour
    }
    
    @Override
    public void render(SpriteBatch batch) {
        if (texture == null) return;
        
        // Dessiner le bloc
        float x = position.x - width / 2;
        float y = position.y - height / 2;
        
        batch.draw(texture, x, y, width, height);
    }
    
    @Override
    public void dispose() {
        super.dispose();
        // Libérer la texture si nécessaire
    }
}
