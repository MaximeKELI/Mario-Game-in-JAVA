package com.mariogame.systems;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.mariogame.utils.Constants;

/**
 * Optimiseur de physique avec spatial partitioning pour améliorer les performances.
 * Réduit drastiquement le nombre de calculs de collision.
 */
public class PhysicsOptimizer {
    private final World world;
    private final SpatialHashGrid spatialGrid;
    private static final float CELL_SIZE = 2f; // 2 mètres par cellule
    
    public PhysicsOptimizer(World world, float worldWidth, float worldHeight) {
        this.world = world;
        int gridWidth = (int) Math.ceil(worldWidth / CELL_SIZE);
        int gridHeight = (int) Math.ceil(worldHeight / CELL_SIZE);
        this.spatialGrid = new SpatialHashGrid(gridWidth, gridHeight, CELL_SIZE);
    }
    
    /**
     * Met à jour la grille spatiale avec tous les corps.
     */
    public void update() {
        spatialGrid.clear();
        
        Array<Body> bodies = new Array<>();
        world.getBodies(bodies);
        
        for (Body body : bodies) {
            if (body.isActive()) {
                Vector2 pos = body.getPosition();
                spatialGrid.insert(body, pos.x, pos.y);
            }
        }
    }
    
    /**
     * Obtient les corps proches d'une position.
     */
    public Array<Body> getNearbyBodies(float x, float y, float radius) {
        return spatialGrid.query(x, y, radius);
    }
    
    /**
     * Grille de hachage spatiale pour optimiser les requêtes.
     */
    private static class SpatialHashGrid {
        private final int gridWidth, gridHeight;
        private final float cellSize;
        private final Array<Body>[][] cells;
        
        @SuppressWarnings("unchecked")
        SpatialHashGrid(int gridWidth, int gridHeight, float cellSize) {
            this.gridWidth = gridWidth;
            this.gridHeight = gridHeight;
            this.cellSize = cellSize;
            this.cells = new Array[gridWidth][gridHeight];
            
            for (int x = 0; x < gridWidth; x++) {
                for (int y = 0; y < gridHeight; y++) {
                    cells[x][y] = new Array<>();
                }
            }
        }
        
        void insert(Body body, float x, float y) {
            int cellX = (int) (x / cellSize);
            int cellY = (int) (y / cellSize);
            
            if (isValid(cellX, cellY)) {
                cells[cellX][cellY].add(body);
            }
        }
        
        Array<Body> query(float x, float y, float radius) {
            Array<Body> result = new Array<>();
            
            int minX = (int) ((x - radius) / cellSize);
            int maxX = (int) ((x + radius) / cellSize);
            int minY = (int) ((y - radius) / cellSize);
            int maxY = (int) ((y + radius) / cellSize);
            
            for (int cx = minX; cx <= maxX; cx++) {
                for (int cy = minY; cy <= maxY; cy++) {
                    if (isValid(cx, cy)) {
                        Array<Body> cell = cells[cx][cy];
                        for (Body body : cell) {
                            Vector2 pos = body.getPosition();
                            float distance = Vector2.dst(x, y, pos.x, pos.y);
                            if (distance <= radius && !result.contains(body, true)) {
                                result.add(body);
                            }
                        }
                    }
                }
            }
            
            return result;
        }
        
        boolean isValid(int x, int y) {
            return x >= 0 && x < gridWidth && y >= 0 && y < gridHeight;
        }
        
        void clear() {
            for (int x = 0; x < gridWidth; x++) {
                for (int y = 0; y < gridHeight; y++) {
                    cells[x][y].clear();
                }
            }
        }
    }
}

