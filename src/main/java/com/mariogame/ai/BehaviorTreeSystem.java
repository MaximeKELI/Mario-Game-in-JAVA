package com.mariogame.ai;

import com.badlogic.gdx.math.Vector2;

/**
 * Système d'arbres de comportement pour IA complexe et modulaire.
 * Permet de créer des comportements sophistiqués avec conditions et actions.
 */
public class BehaviorTreeSystem {
    
    /**
     * Nœud de base pour l'arbre de comportement.
     */
    public abstract static class BehaviorNode {
        public enum Status {
            SUCCESS, FAILURE, RUNNING
        }
        
        public abstract Status execute(Object entity, Vector2 target);
    }
    
    /**
     * Nœud composite : séquence (tous doivent réussir).
     */
    public static class SequenceNode extends BehaviorNode {
        private final BehaviorNode[] children;
        private int currentIndex = 0;
        
        public SequenceNode(BehaviorNode... children) {
            this.children = children;
        }
        
        @Override
        public Status execute(Object entity, Vector2 target) {
            for (int i = currentIndex; i < children.length; i++) {
                Status status = children[i].execute(entity, target);
                
                if (status == Status.FAILURE) {
                    currentIndex = 0;
                    return Status.FAILURE;
                }
                
                if (status == Status.RUNNING) {
                    currentIndex = i;
                    return Status.RUNNING;
                }
            }
            
            currentIndex = 0;
            return Status.SUCCESS;
        }
    }
    
    /**
     * Nœud composite : sélecteur (un seul doit réussir).
     */
    public static class SelectorNode extends BehaviorNode {
        private final BehaviorNode[] children;
        private int currentIndex = 0;
        
        public SelectorNode(BehaviorNode... children) {
            this.children = children;
        }
        
        @Override
        public Status execute(Object entity, Vector2 target) {
            for (int i = currentIndex; i < children.length; i++) {
                Status status = children[i].execute(entity, target);
                
                if (status == Status.SUCCESS) {
                    currentIndex = 0;
                    return Status.SUCCESS;
                }
                
                if (status == Status.RUNNING) {
                    currentIndex = i;
                    return Status.RUNNING;
                }
            }
            
            currentIndex = 0;
            return Status.FAILURE;
        }
    }
    
    /**
     * Nœud condition : vérifie une condition.
     */
    public static class ConditionNode extends BehaviorNode {
        private final Condition condition;
        
        public ConditionNode(Condition condition) {
            this.condition = condition;
        }
        
        @Override
        public Status execute(Object entity, Vector2 target) {
            return condition.check(entity, target) ? Status.SUCCESS : Status.FAILURE;
        }
    }
    
    /**
     * Nœud action : exécute une action.
     */
    public static class ActionNode extends BehaviorNode {
        private final Action action;
        
        public ActionNode(Action action) {
            this.action = action;
        }
        
        @Override
        public Status execute(Object entity, Vector2 target) {
            return action.execute(entity, target);
        }
    }
    
    /**
     * Interface pour les conditions.
     */
    public interface Condition {
        boolean check(Object entity, Vector2 target);
    }
    
    /**
     * Interface pour les actions.
     */
    public interface Action {
        Status execute(Object entity, Vector2 target);
    }
    
    /**
     * Conditions prédéfinies.
     */
    public static class Conditions {
        public static Condition isPlayerNearby(float range) {
            return (entity, target) -> {
                // Calculer la distance
                // return distance < range;
                return true; // Simplifié
            };
        }
        
        public static Condition isHealthLow(float threshold) {
            return (entity, target) -> {
                // Vérifier la santé
                return true; // Simplifié
            };
        }
    }
    
    /**
     * Actions prédéfinies.
     */
    public static class Actions {
        public static Action moveTowards(Vector2 target) {
            return (entity, targetPos) -> {
                // Se déplacer vers la cible
                return Status.SUCCESS;
            };
        }
        
        public static Action attack() {
            return (entity, target) -> {
                // Attaquer
                return Status.SUCCESS;
            };
        }
    }
}

