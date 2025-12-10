package com.mariogame.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.mariogame.entities.Entity;
import com.mariogame.entities.Player;
import com.mariogame.utils.Constants;
import com.mariogame.utils.Constants.CollisionBits;

/**
 * Gère la physique du jeu en utilisant Box2D.
 * Gère les collisions, les forces et les interactions physiques.
 */
public class PhysicsManager implements ContactListener {
    private final World world;
    private final Array<Body> bodiesToRemove = new Array<>();
    private final Array<Body> bodiesToAdd = new Array<>();
    private final Array<Body> bodiesToApplyForce = new Array<>();
    private final Array<ForceApplication> forcesToApply = new Array<>();
    private final float timeStep;
    private int velocityIterations;
    private int positionIterations;
    private float accumulator = 0;
    
    public PhysicsManager() {
        // Créer le monde Box2D avec la gravité par défaut
        this(Constants.WorldConfig.GRAVITY, 
             Constants.WorldConfig.TIME_STEP,
             Constants.WorldConfig.VELOCITY_ITERATIONS,
             Constants.WorldConfig.POSITION_ITERATIONS);
    }
    
    public PhysicsManager(Vector2 gravity, float timeStep, int velocityIterations, int positionIterations) {
        this.world = new World(gravity, true);
        this.timeStep = timeStep;
        this.velocityIterations = velocityIterations;
        this.positionIterations = positionIterations;
        
        // Configurer le gestionnaire de collisions
        world.setContactListener(this);
    }
    
    public void update(float deltaTime) {
        // Gestion du pas de temps fixe pour la stabilité physique
        float frameTime = Math.min(deltaTime, 0.25f);
        accumulator += frameTime;
        
        while (accumulator >= timeStep) {
            // Mettre à jour les corps à ajouter/supprimer
            processPendingBodies();
            
            // Appliquer les forces en attente
            applyPendingForces();
            
            // Mettre à jour la physique
            world.step(timeStep, velocityIterations, positionIterations);
            
            accumulator -= timeStep;
        }
        
        // Interpolation pour un rendu fluide
        float alpha = accumulator / timeStep;
        interpolateBodies(alpha);
    }
    
    private void processPendingBodies() {
        // Ajouter les nouveaux corps
        synchronized (bodiesToAdd) {
            for (Body body : bodiesToAdd) {
                // Les corps sont déjà créés, donc on ne fait que les activer
                body.setActive(true);
            }
            bodiesToAdd.clear();
        }
        
        // Supprimer les corps marqués pour suppression
        synchronized (bodiesToRemove) {
            for (Body body : bodiesToRemove) {
                if (body.getUserData() instanceof Entity) {
                    Entity entity = (Entity) body.getUserData();
                    entity.onRemove();
                }
                world.destroyBody(body);
            }
            bodiesToRemove.clear();
        }
    }
    
    private void applyPendingForces() {
        synchronized (forcesToApply) {
            for (int i = 0; i < forcesToApply.size; i++) {
                ForceApplication fa = forcesToApply.get(i);
                fa.body.applyForceToCenter(fa.force, true);
                
                // Pour les forces d'impulsion, on les applique une seule fois
                if (fa.impulse) {
                    forcesToApply.removeIndex(i);
                    i--;
                }
            }
        }
    }
    
    private void interpolateBodies(float alpha) {
        // Interpolation pour un rendu fluide
        Array<Body> bodies = new Array<>();
        world.getBodies(bodies);
        
        for (Body body : bodies) {
            if (body.getUserData() instanceof Entity) {
                Entity entity = (Entity) body.getUserData();
                entity.setInterpolationAlpha(alpha);
            }
        }
    }
    
    // Méthodes pour ajouter/supprimer des corps
    
    public void addBody(Body body) {
        synchronized (bodiesToAdd) {
            bodiesToAdd.add(body);
        }
    }
    
    public void removeBody(Body body) {
        synchronized (bodiesToRemove) {
            if (!bodiesToRemove.contains(body, true)) {
                bodiesToRemove.add(body);
            }
        }
    }
    
    public void applyForce(Body body, Vector2 force, boolean impulse) {
        synchronized (forcesToApply) {
            forcesToApply.add(new ForceApplication(body, force, impulse));
        }
    }
    
    // Gestion des collisions
    
    @Override
    public void beginContact(Contact contact) {
        // Récupérer les deux fixtures en collision
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();
        
        // Récupérer les corps associés
        Body bodyA = fixtureA.getBody();
        Body bodyB = fixtureB.getBody();
        
        // Récupérer les entités associées (si elles existent)
        Object userDataA = bodyA.getUserData();
        Object userDataB = bodyB.getUserData();
        
        // Vérifier les collisions avec le joueur
        if (userDataA instanceof Player) {
            handlePlayerCollision((Player) userDataA, fixtureA, userDataB, fixtureB, contact);
        } else if (userDataB instanceof Player) {
            handlePlayerCollision((Player) userDataB, fixtureB, userDataA, fixtureA, contact);
        }
        
        // Autres types de collisions...
    }
    
    private void handlePlayerCollision(Player player, Fixture playerFixture, 
                                     Object otherData, Fixture otherFixture, Contact contact) {
        // Vérifier si la collision se produit avec la tête du joueur
        boolean isHeadCollision = (playerFixture.getFilterData().categoryBits & CollisionBits.PLAYER_HEAD) != 0;
        
        // Gérer les collisions avec différents types d'objets
        if (otherData instanceof Entity) {
            Entity otherEntity = (Entity) otherData;
            
            // Appeler la méthode de gestion de collision de l'entité
            otherEntity.onCollision(player, contact, isHeadCollision);
            
            // Appeler la méthode de gestion de collision du joueur
            player.onCollision(otherEntity, contact, isHeadCollision);
        }
    }
    
    @Override
    public void endContact(Contact contact) {
        // Gérer la fin d'une collision si nécessaire
    }
    
    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
        // Modifier la collision avant qu'elle ne soit résolue
    }
    
    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {
        // Gérer les informations après la résolution de la collision
    }
    
    // Classe utilitaire pour les applications de force différées
    private static class ForceApplication {
        final Body body;
        final Vector2 force;
        final boolean impulse;
        
        public ForceApplication(Body body, Vector2 force, boolean impulse) {
            this.body = body;
            this.force = new Vector2(force);
            this.impulse = impulse;
        }
    }
    
    // Getters et setters
    
    public World getWorld() {
        return world;
    }
    
    public void setGravity(Vector2 gravity) {
        world.setGravity(gravity);
    }
    
    public void setGravity(float x, float y) {
        world.setGravity(x, y);
    }
    
    public void dispose() {
        world.dispose();
    }
}
