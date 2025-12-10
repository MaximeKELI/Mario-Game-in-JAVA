package com.mariogame.managers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.physics.box2d.*;
import com.mariogame.entities.*;
import com.mariogame.entities.enemies.Goomba;
import com.mariogame.entities.items.Coin;
import com.mariogame.entities.items.Mushroom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CollisionManagerTest {

    @Mock private World world;
    @Mock private Contact contact;
    @Mock private Fixture fixtureA, fixtureB;
    @Mock private Body bodyA, bodyB;
    @Mock private Player player;
    @Mock private Goomba goomba;
    @Mock private Coin coin;
    @Mock private Mushroom mushroom;
    
    private CollisionManager collisionManager;
    
    @BeforeEach
    void setUp() {
        // Configuration des mocks de base
        when(contact.getFixtureA()).thenReturn(fixtureA);
        when(contact.getFixtureB()).thenReturn(fixtureB);
        when(fixtureA.getBody()).thenReturn(bodyA);
        when(fixtureB.getBody()).thenReturn(bodyB);
        
        // Initialisation du gestionnaire de collisions
        collisionManager = new CollisionManager(world);
    }

    @Test
    void testPlayerCoinCollision() {
        // Configuration du test pour une collision entre le joueur et une pièce
        when(bodyA.getUserData()).thenReturn(player);
        when(bodyB.getUserData()).thenReturn(coin);
        
        // Déclenchement de la collision
        collisionManager.beginContact(contact);
        
        // Vérification que la pièce a été collectée
        verify(coin).onCollected();
    }
    
    @Test
    void testPlayerGoombaCollision() {
        // Configuration du test pour une collision entre le joueur et un Goomba
        when(bodyA.getUserData()).thenReturn(player);
        when(bodyB.getUserData()).thenReturn(goomba);
        
        // Configuration du joueur pour qu'il saute sur le Goomba
        when(contact.getWorldManifold()).thenReturn(mock(WorldManifold.class));
        when(contact.getWorldManifold().getNormal()).thenReturn(new float[]{0, 1}); // Collision par le dessus
        
        // Déclenchement de la collision
        collisionManager.beginContact(contact);
        
        // Vérification que le Goomba a été éliminé
        verify(goomba).onHitFromTop();
    }
    
    @Test
    void testPlayerMushroomCollision() {
        // Configuration du test pour une collision entre le joueur et un champignon
        when(bodyA.getUserData()).thenReturn(player);
        when(bodyB.getUserData()).thenReturn(mushroom);
        
        // Déclenchement de la collision
        collisionManager.beginContact(contact);
        
        // Vérification que le champignon a été collecté
        verify(mushroom).onCollected();
    }
    
    @Test
    void testPlayerHurtByEnemy() {
        // Configuration du test pour une collision latérale avec un ennemi
        when(bodyA.getUserData()).thenReturn(player);
        when(bodyB.getUserData()).thenReturn(goomba);
        
        // Configuration pour une collision latérale (pas par le dessus)
        when(contact.getWorldManifold()).thenReturn(mock(WorldManifold.class));
        when(contact.getWorldManifold().getNormal()).thenReturn(new float[]{1, 0}); // Collision latérale
        
        // Configuration du joueur pour qu'il ne soit pas invincible
        when(player.isInvincible()).thenReturn(false);
        
        // Déclenchement de la collision
        collisionManager.beginContact(contact);
        
        // Vérification que le joueur a été blessé
        verify(player).takeDamage();
    }
    
    @Test
    void testEndContact() {
        // Configuration du test pour la fin d'une collision
        when(bodyA.getUserData()).thenReturn(player);
        when(bodyB.getUserData()).thenReturn(coin);
        
        // Déclenchement de la fin de collision
        collisionManager.endContact(contact);
        
        // Vérification que la méthode a été appelée (pas d'erreur)
        assertTrue(true);
    }
    
    @Test
    void testNullUserData() {
        // Configuration avec des données utilisateur nulles
        when(bodyA.getUserData()).thenReturn(null);
        when(bodyB.getUserData()).thenReturn(null);
        
        // Vérification qu'aucune exception n'est levée
        assertDoesNotThrow(() -> {
            collisionManager.beginContact(contact);
            collisionManager.endContact(contact);
        });
    }
    
    @Test
    void testPreSolve() {
        // Configuration du test pour la pré-résolution des collisions
        when(bodyA.getUserData()).thenReturn(player);
        when(bodyB.getUserData()).thenReturn(goomba);
        
        // Création d'un mock pour Manifold
        Manifold manifold = mock(Manifold.class);
        
        // Déclenchement de la pré-résolution
        collisionManager.preSolve(contact, manifold);
        
        // Vérification que la méthode a été appelée (pas d'erreur)
        assertTrue(true);
    }
    
    @Test
    void testPostSolve() {
        // Configuration du test pour la post-résolution des collisions
        when(bodyA.getUserData()).thenReturn(player);
        when(bodyB.getUserData()).thenReturn(goomba);
        
        // Création d'un mock pour ContactImpulse
        ContactImpulse impulse = mock(ContactImpulse.class);
        
        // Déclenchement de la post-résolution
        collisionManager.postSolve(contact, impulse);
        
        // Vérification que la méthode a été appelée (pas d'erreur)
        assertTrue(true);
    }
}
