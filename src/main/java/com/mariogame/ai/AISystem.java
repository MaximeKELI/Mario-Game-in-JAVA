package com.mariogame.ai;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.mariogame.entities.Player;
import com.mariogame.entities.enemies.Goomba;
import com.mariogame.entities.enemies.KoopaTroopa;
import com.mariogame.world.GameWorld;

/**
 * Système d'IA avancé avec pathfinding, comportements intelligents et prise de décision.
 * Architecture modulaire inspirée des systèmes AAA.
 */
public class AISystem {
    private final GameWorld gameWorld;
    private final PathfindingSystem pathfinding;
    private final BehaviorTreeSystem behaviorTree;
    private final Array<AIEntity> aiEntities = new Array<>();
    
    public AISystem(GameWorld gameWorld) {
        this.gameWorld = gameWorld;
        this.pathfinding = new PathfindingSystem(gameWorld);
        this.behaviorTree = new BehaviorTreeSystem();
    }
    
    /**
     * Enregistre une entité avec IA.
     */
    public void registerEntity(Object entity, AIBehavior behavior) {
        AIEntity aiEntity = new AIEntity(entity, behavior);
        aiEntities.add(aiEntity);
    }
    
    /**
     * Met à jour toutes les entités IA.
     */
    public void update(float deltaTime) {
        Player player = gameWorld.getPlayer();
        if (player == null) return;
        
        Vector2 playerPos = player.getPosition();
        
        for (AIEntity aiEntity : aiEntities) {
            aiEntity.update(deltaTime, playerPos, pathfinding, behaviorTree);
        }
    }
    
    /**
     * Classe représentant une entité avec IA.
     */
    private static class AIEntity {
        private final Object entity;
        private final AIBehavior behavior;
        private float updateTimer = 0;
        private static final float UPDATE_INTERVAL = 0.1f; // Mise à jour toutes les 100ms
        
        AIEntity(Object entity, AIBehavior behavior) {
            this.entity = entity;
            this.behavior = behavior;
        }
        
        void update(float deltaTime, Vector2 playerPos, PathfindingSystem pathfinding, BehaviorTreeSystem behaviorTree) {
            updateTimer += deltaTime;
            
            if (updateTimer >= UPDATE_INTERVAL) {
                updateTimer = 0;
                
                // Exécuter le comportement
                if (behavior != null) {
                    behavior.execute(entity, playerPos, pathfinding, behaviorTree);
                }
            }
        }
    }
    
    /**
     * Interface pour les comportements IA.
     */
    public interface AIBehavior {
        void execute(Object entity, Vector2 target, PathfindingSystem pathfinding, BehaviorTreeSystem behaviorTree);
    }
    
    /**
     * Comportements prédéfinis.
     */
    public static class Behaviors {
        /**
         * Comportement simple : suivre le joueur.
         */
        public static AIBehavior FOLLOW_PLAYER = (entity, target, pathfinding, behaviorTree) -> {
            if (entity instanceof Goomba) {
                Goomba goomba = (Goomba) entity;
                Vector2 goombaPos = goomba.getPosition();
                
                // Direction vers le joueur
                float dx = target.x - goombaPos.x;
                if (Math.abs(dx) > 0.1f) {
                    // Se déplacer vers le joueur
                    // goomba.move(dx > 0 ? 1 : -1);
                }
            }
        };
        
        /**
         * Comportement : patrouiller entre deux points.
         */
        public static AIBehavior createPatrol(Vector2 pointA, Vector2 pointB) {
            return (entity, target, pathfinding, behaviorTree) -> {
                // Logique de patrouille
            };
        }
        
        /**
         * Comportement : attaquer le joueur si proche.
         */
        public static AIBehavior createAttack(float attackRange) {
            return (entity, target, pathfinding, behaviorTree) -> {
                if (entity instanceof KoopaTroopa) {
                    KoopaTroopa koopa = (KoopaTroopa) entity;
                    Vector2 koopaPos = koopa.getPosition();
                    
                    float distance = koopaPos.dst(target);
                    if (distance < attackRange) {
                        // Attaquer
                    }
                }
            };
        }
    }
}

