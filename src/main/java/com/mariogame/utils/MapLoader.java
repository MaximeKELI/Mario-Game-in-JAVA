package com.mariogame.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.EllipseMapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Ellipse;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.mariogame.entities.Entity;
import com.mariogame.entities.Player;
import com.mariogame.entities.enemies.Goomba;
import com.mariogame.entities.items.Coin;
import com.mariogame.entities.items.Mushroom;
import com.mariogame.utils.Constants.CollisionBits;

/**
 * Classe utilitaire pour charger et gérer les cartes Tiled.
 */
public class MapLoader {
    private final World physicsWorld;
    private final TiledMap map;
    private final OrthogonalTiledMapRenderer renderer;
    private final float unitScale;
    
    // Calques de la carte
    private TiledMapTileLayer backgroundLayer;
    private TiledMapTileLayer groundLayer;
    private TiledMapTileLayer foregroundLayer;
    private MapLayer objectLayer;
    
    // Propriétés de la carte
    private final int mapWidth;
    private final int mapHeight;
    private final int tileWidth;
    private final int tileHeight;
    
    // Position de départ du joueur
    private Vector2 playerStartPosition;
    
    public MapLoader(World physicsWorld, TiledMap map) {
        this.physicsWorld = physicsWorld;
        this.map = map;
        this.unitScale = 1 / Constants.WorldConfig.PPM;
        
        // Initialiser le rendu de la carte
        this.renderer = new OrthogonalTiledMapRenderer(map, unitScale);
        
        // Récupérer les propriétés de la carte
        MapProperties properties = map.getProperties();
        this.mapWidth = properties.get("width", Integer.class);
        this.mapHeight = properties.get("height", Integer.class);
        this.tileWidth = properties.get("tilewidth", Integer.class);
        this.tileHeight = properties.get("tileheight", Integer.class);
        
        // Charger les calques
        loadLayers();
    }
    
    /**
     * Charge les différents calques de la carte.
     */
    private void loadLayers() {
        // Charger les calques principaux
        backgroundLayer = (TiledMapTileLayer) map.getLayers().get("background");
        groundLayer = (TiledMapTileLayer) map.getLayers().get("ground");
        foregroundLayer = (TiledMapTileLayer) map.getLayers().get("foreground");
        objectLayer = map.getLayers().get("objects");
        
        // Vérifier que les calques requis existent
        if (groundLayer == null) {
            Gdx.app.error("MapLoader", "Ground layer not found in the map!");
        }
        
        if (objectLayer == null) {
            Gdx.app.log("MapLoader", "No object layer found in the map.");
        }
    }
    
    /**
     * Charge les calques d'arrière-plan.
     */
    public void loadBackgroundLayers() {
        // Rien de spécial à faire ici, les calques sont déjà chargés
    }
    
    /**
     * Charge les calques de collision.
     */
    public void loadCollisionLayers() {
        if (groundLayer != null) {
            createCollisionObjects(groundLayer);
        }
        
        if (objectLayer != null) {
            loadObjects();
        }
    }
    
    /**
     * Crée les objets de collision à partir d'un calque de tuiles.
     */
    private void createCollisionObjects(TiledMapTileLayer layer) {
        float tileWidth = layer.getTileWidth() * unitScale;
        float tileHeight = layer.getTileHeight() * unitScale;
        
        for (int x = 0; x < layer.getWidth(); x++) {
            for (int y = 0; y < layer.getHeight(); y++) {
                TiledMapTileLayer.Cell cell = layer.getCell(x, y);
                
                if (cell != null && cell.getTile() != null) {
                    // Créer un corps physique pour cette tuile
                    BodyDef bodyDef = new BodyDef();
                    bodyDef.type = BodyDef.BodyType.StaticBody;
                    bodyDef.position.set(
                        (x + 0.5f) * tileWidth,
                        (layer.getHeight() - y - 0.5f) * tileHeight
                    );
                    
                    Body body = physicsWorld.createBody(bodyDef);
                    
                    // Créer une forme de collision pour la tuile
                    PolygonShape shape = new PolygonShape();
                    shape.setAsBox(tileWidth / 2, tileHeight / 2);
                    
                    FixtureDef fixtureDef = new FixtureDef();
                    fixtureDef.shape = shape;
                    fixtureDef.friction = 0.6f;
                    
                    // Définir les filtres de collision
                    fixtureDef.filter.categoryBits = CollisionBits.GROUND;
                    fixtureDef.filter.maskBits = (short) (CollisionBits.PLAYER | CollisionBits.ENEMY | CollisionBits.ITEM);
                    
                    // Créer la fixture
                    Fixture fixture = body.createFixture(fixtureDef);
                    fixture.setUserData("ground");
                    
                    shape.dispose();
                }
            }
        }
    }
    
    /**
     * Charge les objets de la couche d'objets.
     */
    private void loadObjects() {
        MapObjects objects = objectLayer.getObjects();
        
        for (MapObject object : objects) {
            MapProperties properties = object.getProperties();
            
            // Position de l'objet
            float x = properties.get("x", Float.class) * unitScale;
            float y = (mapHeight * tileHeight - properties.get("y", Float.class)) * unitScale;
            
            // Ajuster la position en fonction du type d'objet
            if (object instanceof RectangleMapObject) {
                Rectangle rect = ((RectangleMapObject) object).getRectangle();
                x += rect.width * unitScale / 2;
                y -= rect.height * unitScale / 2;
            } else if (object instanceof EllipseMapObject) {
                Ellipse ellipse = ((EllipseMapObject) object).getEllipse();
                x += ellipse.width * unitScale / 2;
                y -= ellipse.height * unitScale / 2;
            }
            
            // Vérifier le type d'objet
            if (properties.containsKey("player_start")) {
                // Position de départ du joueur
                playerStartPosition = new Vector2(x, y);
            } else if (properties.containsKey("enemy")) {
                String enemyType = properties.get("type", String.class, "goomba");
                // Créer l'ennemi en fonction du type
                // (à implémenter dans une méthode séparée)
            } else if (properties.containsKey("item")) {
                String itemType = properties.get("type", String.class, "coin");
                // Créer l'objet en fonction du type
                // (à implémenter dans une méthode séparée)
            }
        }
    }
    
    /**
     * Rendu des calques d'arrière-plan.
     */
    public void renderBackgroundLayers(SpriteBatch batch) {
        if (backgroundLayer != null) {
            renderer.getBatch().begin();
            renderer.renderTileLayer(backgroundLayer);
            renderer.getBatch().end();
        }
    }
    
    /**
     * Rendu des calques de premier plan.
     */
    public void renderForegroundLayers(SpriteBatch batch) {
        if (foregroundLayer != null) {
            renderer.getBatch().begin();
            renderer.renderTileLayer(foregroundLayer);
            renderer.getBatch().end();
        }
    }
    
    /**
     * Met à jour la vue de la caméra.
     */
    public void setView(OrthographicCamera camera) {
        renderer.setView(camera);
    }
    
    /**
     * Nettoie les ressources utilisées par le chargeur de carte.
     */
    public void dispose() {
        if (renderer != null) {
            renderer.dispose();
        }
    }
    
    // Getters
    
    /**
     * Retourne la position de départ du joueur.
     */
    public Vector2 getPlayerStartPosition() {
        return playerStartPosition != null ? playerStartPosition.cpy() : new Vector2(2, 10);
    }
    
    /**
     * Retourne la largeur de la carte en pixels.
     */
    public float getMapWidthPixels() {
        return mapWidth * tileWidth * unitScale;
    }
    
    /**
     * Retourne la hauteur de la carte en pixels.
     */
    public float getMapHeightPixels() {
        return mapHeight * tileHeight * unitScale;
    }
    
    /**
     * Récupère les positions des objets d'un certain type dans un groupe.
     */
    public Array<Vector2> getObjectPositions(String group, String type) {
        Array<Vector2> positions = new Array<>();
        
        if (objectLayer == null) {
            return positions;
        }
        
        for (MapObject object : objectLayer.getObjects()) {
            MapProperties props = object.getProperties();
            
            if (props.containsKey(group) && props.get("type", String.class, "").equals(type)) {
                float x = props.get("x", Float.class) * unitScale;
                float y = (mapHeight * tileHeight - props.get("y", Float.class)) * unitScale;
                
                // Ajuster la position en fonction du type d'objet
                if (object instanceof RectangleMapObject) {
                    Rectangle rect = ((RectangleMapObject) object).getRectangle();
                    x += rect.width * unitScale / 2;
                    y -= rect.height * unitScale / 2;
                } else if (object instanceof EllipseMapObject) {
                    Ellipse ellipse = ((EllipseMapObject) object).getEllipse();
                    x += ellipse.width * unitScale / 2;
                    y -= ellipse.height * unitScale / 2;
                }
                
                positions.add(new Vector2(x, y));
            }
        }
        
        return positions;
    }
}
