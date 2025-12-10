package com.mariogame.entities;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.mariogame.core.GameManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EntityTest {

    @Mock private World world;
    @Mock private Body body;
    @Mock private Fixture fixture;
    @Mock private SpriteBatch batch;
    @Mock private GameManager gameManager;
    
    private TestEntity testEntity;
    
    // Classe de test concrète pour tester la classe abstraite Entity
    private static class TestEntity extends Entity {
        public TestEntity(World world, float x, float y) {
            super(world, x, y);
        }

        @Override
        protected void defineBody() {
            BodyDef def = new BodyDef();
            def.type = BodyDef.BodyType.DynamicBody;
            def.position.set(x, y);
            
            body = world.createBody(def);
            
            FixtureDef fixtureDef = new FixtureDef();
            PolygonShape shape = new PolygonShape();
            shape.setAsBox(0.5f, 0.5f);
            
            fixtureDef.shape = shape;
            fixture = body.createFixture(fixtureDef);
            shape.dispose();
            
            body.setUserData(this);
        }

        @Override
        public void update(float delta) {
            // Implémentation de test
        }

        @Override
        public void render(SpriteBatch batch) {
            // Implémentation de test
        }
    }
    
    @BeforeEach
    void setUp() {
        when(world.createBody(any())).thenReturn(body);
        when(body.createFixture(any())).thenReturn(fixture);
        
        testEntity = new TestEntity(world, 10, 5);
    }

    @Test
    void testInitialPosition() {
        Vector2 position = testEntity.getPosition();
        assertEquals(10, position.x);
        assertEquals(5, position.y);
    }
    
    @Test
    void testSetPosition() {
        testEntity.setPosition(20, 30);
        
        verify(body).setTransform(20, 30, 0);
        
        Vector2 position = testEntity.getPosition();
        assertEquals(20, position.x);
        assertEquals(30, position.y);
    }
    
    @Test
    void testSetVelocity() {
        testEntity.setVelocity(5, -2);
        
        verify(body).setLinearVelocity(5, -2);
    }
    
    @Test
    void testApplyForce() {
        testEntity.applyForce(10, 5);
        
        verify(body).applyForceToCenter(
            eq(new Vector2(10, 5)), 
            eq(true)
        );
    }
    
    @Test
    void testApplyImpulse() {
        testEntity.applyImpulse(0, 10);
        
        verify(body).applyLinearImpulse(
            eq(new Vector2(0, 10)), 
            any(Vector2.class), 
            eq(true)
        );
    }
    
    @Test
    void testDispose() {
        testEntity.dispose();
        
        // Vérifier que le corps est bien supprimé du monde
        verify(world).destroyBody(body);
    }
    
    @Test
    void testGetBoundingBox() {
        // Configuration du mock Body
        when(body.getPosition()).thenReturn(new Vector2(10, 5));
        
        // La méthode getBoundingBox devrait retourner une boîte de collision
        // centrée sur la position de l'entité avec une taille par défaut
        assertNotNull(testEntity.getBoundingBox());
    }
    
    @Test
    void testIsActive() {
        when(body.isActive()).thenReturn(true);
        assertTrue(testEntity.isActive());
        
        when(body.isActive()).thenReturn(false);
        assertFalse(testEntity.isActive());
    }
    
    @Test
    void testSetActive() {
        testEntity.setActive(true);
        verify(body).setActive(true);
        
        testEntity.setActive(false);
        verify(body).setActive(false);
    }
}
