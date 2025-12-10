package com.mariogame.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.mariogame.entities.Entity;
import com.mariogame.entities.Player;
import com.mariogame.entities.enemies.Goomba;
import com.mariogame.entities.items.Coin;
import com.mariogame.entities.items.Mushroom;
import com.mariogame.managers.PhysicsManager;
import com.mariogame.utils.Constants;
import com.mariogame.utils.MapLoader;

/**
 * Représente le monde de jeu, gérant les entités, la physique et les interactions.
 */
public class GameWorld implements Disposable {
    private final World physicsWorld;
    private final PhysicsManager physicsManager;
    private final Array<Entity> entities = new Array<>();
    private final Array<Entity> entitiesToAdd = new Array<>();
    private final Array<Entity> entitiesToRemove = new Array<>();
    private Player player;
    private TiledMap currentMap;
    private MapLoader mapLoader;
    private String currentLevel;
    private OrthographicCamera gameCamera;
    private boolean isPaused = false;
    
    public GameWorld() {
        // Créer le monde physique avec la gravité par défaut
        physicsWorld = new World(new Vector2(0, Constants.WorldConfig.GRAVITY), true);
        physicsManager = new PhysicsManager();
        
        // Configurer le gestionnaire de collisions
        setupCollisionHandling();
    }
    
    /**
     * Charge un niveau à partir d'un fichier TMX.
     */
    public void loadLevel(String levelName) {
        // Désactiver les mises à jour pendant le chargement
        isPaused = true;
        
        // Nettoyer le niveau actuel
        clearLevel();
        
        try {
            // Charger la carte Tiled
            currentMap = new TmxMapLoader().load("maps/" + levelName + ".tmx");
            currentLevel = levelName;
            
            // Initialiser le chargeur de carte
            mapLoader = new MapLoader(physicsWorld, currentMap);
            
            // Charger les calques de la carte
            mapLoader.loadBackgroundLayers();
            mapLoader.loadCollisionLayers();
            
            // Créer le joueur à la position de départ
            Vector2 startPosition = mapLoader.getPlayerStartPosition();
            if (startPosition != null) {
                player = new Player(physicsWorld, startPosition.x, startPosition.y);
                addEntity(player);
            } else {
                Gdx.app.error("GameWorld", "No player start position found in the map!");
                player = new Player(physicsWorld, 2, 10); // Position par défaut
                addEntity(player);
            }
            
            // Charger les entités du niveau
            loadEntities();
            
            isPaused = false;
            Gdx.app.log("GameWorld", "Level loaded: " + levelName);
            
        } catch (Exception e) {
            Gdx.app.error("GameWorld", "Error loading level: " + levelName, e);
            // En cas d'erreur, créer un niveau vide avec juste le joueur
            player = new Player(physicsWorld, 2, 10);
            addEntity(player);
            isPaused = false;
        }
    }
    
    /**
     * Charge les entités à partir de la carte Tiled.
     */
    private void loadEntities() {
        if (mapLoader == null) return;
        
        // Charger les ennemis
        Array<Vector2> goombaPositions = mapLoader.getObjectPositions("enemies", "goomba");
        for (Vector2 pos : goombaPositions) {
            addEntity(new Goomba(physicsWorld, pos.x, pos.y));
        }
        
        // Charger les pièces
        Array<Vector2> coinPositions = mapLoader.getObjectPositions("items", "coin");
        for (Vector2 pos : coinPositions) {
            addEntity(new Coin(physicsWorld, pos.x, pos.y));
        }
        
        // Charger les champignons
        Array<Vector2> mushroomPositions = mapLoader.getObjectPositions("items", "mushroom");
        for (Vector2 pos : mushroomPositions) {
            addEntity(new Mushroom(physicsWorld, pos.x, pos.y));
        }
    }
    
    /**
     * Nettoie le niveau actuel.
     */
    private void clearLevel() {
        // Marquer toutes les entités pour suppression
        for (Entity entity : entities) {
            entity.remove();
        }
        
        // Nettoyer les tableaux
        entities.clear();
        entitiesToAdd.clear();
        entitiesToRemove.clear();
        
        // Libérer la carte actuelle
        if (currentMap != null) {
            currentMap.dispose();
            currentMap = null;
        }
        
        // Réinitialiser le joueur
        player = null;
    }
    
    /**
     * Met à jour l'état du monde de jeu.
     */
    public void update(float deltaTime) {
        if (isPaused) return;
        
        // Mettre à jour la physique
        physicsManager.update(deltaTime);
        
        // Mettre à jour les entités
        for (Entity entity : entities) {
            if (!entity.isRemoved()) {
                entity.update(deltaTime);
            } else {
                entitiesToRemove.add(entity);
            }
        }
        
        // Traiter les entités à ajouter/supprimer
        processEntityChanges();
        
        // Mettre à jour la caméra pour suivre le joueur
        updateCamera(deltaTime);
    }
    
    /**
     * Traite les ajouts et suppressions d'entités.
     */
    private void processEntityChanges() {
        // Ajouter les nouvelles entités
        if (entitiesToAdd.size > 0) {
            entities.addAll(entitiesToAdd);
            entitiesToAdd.clear();
        }
        
        // Supprimer les entités marquées pour suppression
        if (entitiesToRemove.size > 0) {
            for (Entity entity : entitiesToRemove) {
                entities.removeValue(entity, true);
                entity.dispose();
            }
            entitiesToRemove.clear();
        }
    }
    
    /**
     * Met à jour la position de la caméra pour suivre le joueur.
     */
    private void updateCamera(float deltaTime) {
        if (player == null || gameCamera == null) return;
        
        // Position cible de la caméra (avec décalage vers l'avant)
        float targetX = player.getPosition().x + (player.isFacingRight() ? 2f : -2f);
        float targetY = player.getPosition().y + 1f;
        
        // Lisser le mouvement de la caméra
        float lerp = 0.1f;
        float cameraX = gameCamera.position.x + (targetX - gameCamera.position.x) * lerp;
        float cameraY = gameCamera.position.y + (targetY - gameCamera.position.y) * lerp;
        
        // Limiter la caméra aux bords de la carte
        if (mapLoader != null) {
            float viewportWidth = gameCamera.viewportWidth / 2f;
            float viewportHeight = gameCamera.viewportHeight / 2f;
            
            cameraX = Math.max(viewportWidth, Math.min(
                mapLoader.getMapWidthPixels() - viewportWidth, 
                cameraX
            ));
            
            cameraY = Math.max(viewportHeight, Math.min(
                mapLoader.getMapHeightPixels() - viewportHeight, 
                cameraY
            ));
        }
        
        // Appliquer la nouvelle position de la caméra
        gameCamera.position.set(cameraX, cameraY, 0);
        gameCamera.update();
    }
    
    /**
     * Dessine le monde de jeu.
     */
    public void render(SpriteBatch batch) {
        if (isPaused) return;
        
        // Rendu de la carte
        if (mapLoader != null) {
            mapLoader.renderBackgroundLayers(batch);
        }
        
        // Rendu des entités
        for (Entity entity : entities) {
            entity.render(batch);
        }
        
        // Rendu des calques avant-plan de la carte
        if (mapLoader != null) {
            mapLoader.renderForegroundLayers(batch);
        }
    }
    
    /**
     * Configure la gestion des collisions.
     */
    private void setupCollisionHandling() {
        // Configurer les filtres de collision
        // (à implémenter selon les besoins spécifiques du jeu)
    }
    
    /**
     * Ajoute une entité au monde.
     */
    public void addEntity(Entity entity) {
        if (entity != null) {
            entity.setWorld(this);
            entitiesToAdd.add(entity);
        }
    }
    
    /**
     * Supprime une entité du monde.
     */
    public void removeEntity(Entity entity) {
        if (entity != null) {
            entity.remove();
            entitiesToRemove.add(entity);
        }
    }
    
    /**
     * Définit la caméra utilisée pour le rendu.
     */
    public void setGameCamera(OrthographicCamera camera) {
        this.gameCamera = camera;
    }
    
    /**
     * Met le jeu en pause ou le reprend.
     */
    public void setPaused(boolean paused) {
        this.isPaused = paused;
    }
    
    /**
     * Vérifie si le jeu est en pause.
     */
    public boolean isPaused() {
        return isPaused;
    }
    
    /**
     * Obtient le joueur.
     */
    public Player getPlayer() {
        return player;
    }
    
    /**
     * Obtient le monde physique Box2D.
     */
    public World getPhysicsWorld() {
        return physicsWorld;
    }
    
    /**
     * Libère les ressources utilisées par le monde de jeu.
     */
    @Override
    public void dispose() {
        // Libérer toutes les entités
        for (Entity entity : entities) {
            entity.dispose();
        }
        entities.clear();
        
        // Libérer la carte
        if (currentMap != null) {
            currentMap.dispose();
        }
        
        // Libérer le monde physique
        if (physicsWorld != null) {
            physicsWorld.dispose();
        }
    }
}
