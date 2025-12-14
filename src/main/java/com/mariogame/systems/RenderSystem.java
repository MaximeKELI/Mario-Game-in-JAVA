package com.mariogame.systems;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Sort;
import com.mariogame.utils.Constants;

/**
 * Système de rendu ultra-optimisé avec culling spatial et batching intelligent.
 * Architecture inspirée des moteurs AAA pour performance maximale.
 */
public class RenderSystem {
    private static final int INITIAL_CAPACITY = 1000;
    private static final int MAX_BATCH_SIZE = 5000;
    
    // Structures de données optimisées
    private final Array<Renderable> renderQueue = new Array<>(INITIAL_CAPACITY);
    private final Array<Renderable> visibleObjects = new Array<>(INITIAL_CAPACITY);
    private final Pool<Renderable> renderablePool = new Pool<Renderable>() {
        @Override
        protected Renderable newObject() {
            return new Renderable();
        }
    };
    
    // Culling spatial
    private final SpatialGrid spatialGrid;
    private final Frustum frustum = new Frustum();
    
    // Batching
    private final SpriteBatch spriteBatch;
    private int currentBatchSize = 0;
    private boolean batchStarted = false;
    
    // Statistiques
    private int renderedObjects = 0;
    private int culledObjects = 0;
    private int batches = 0;
    
    public RenderSystem(SpriteBatch spriteBatch, float worldWidth, float worldHeight, int gridSize) {
        this.spriteBatch = spriteBatch;
        this.spatialGrid = new SpatialGrid(worldWidth, worldHeight, gridSize);
    }
    
    /**
     * Ajoute un objet à rendre.
     */
    public void addRenderable(TextureRegion texture, float x, float y, float width, float height, 
                             float rotation, int layer, boolean flipX, boolean flipY) {
        Renderable renderable = renderablePool.obtain();
        renderable.set(texture, x, y, width, height, rotation, layer, flipX, flipY);
        renderQueue.add(renderable);
        spatialGrid.insert(renderable);
    }
    
    /**
     * Effectue le culling spatial et prépare le rendu.
     */
    public void prepareRender(Camera camera) {
        // Mettre à jour le frustum
        frustum.update(camera.combined);
        
        // Culling spatial
        visibleObjects.clear();
        spatialGrid.query(frustum, visibleObjects);
        
        // Trier par layer pour le rendu correct
        Sort.instance().sort(visibleObjects, (a, b) -> Integer.compare(a.layer, b.layer));
        
        culledObjects = renderQueue.size - visibleObjects.size;
        renderedObjects = visibleObjects.size;
    }
    
    /**
     * Rend tous les objets visibles avec batching optimisé.
     */
    public void render() {
        if (!batchStarted) {
            spriteBatch.begin();
            batchStarted = true;
        }
        
        batches = 0;
        currentBatchSize = 0;
        
        for (Renderable renderable : visibleObjects) {
            // Vérifier si on doit commencer un nouveau batch
            if (currentBatchSize >= MAX_BATCH_SIZE) {
                spriteBatch.end();
                spriteBatch.begin();
                batches++;
                currentBatchSize = 0;
            }
            
            // Rendre l'objet
            renderObject(renderable);
            currentBatchSize++;
        }
        
        if (batchStarted) {
            spriteBatch.end();
            batchStarted = false;
        }
    }
    
    /**
     * Rend un objet individuel.
     */
    private void renderObject(Renderable renderable) {
        TextureRegion texture = renderable.texture;
        if (texture == null) return;
        
        float x = renderable.x * Constants.WorldConfig.PPM;
        float y = renderable.y * Constants.WorldConfig.PPM;
        float width = renderable.width * Constants.WorldConfig.PPM;
        float height = renderable.height * Constants.WorldConfig.PPM;
        
        // Appliquer les transformations
        if (renderable.rotation != 0) {
            spriteBatch.draw(texture, x, y, width / 2, height / 2, 
                           width, height, 1, 1, renderable.rotation);
        } else {
            spriteBatch.draw(texture, x, y, width, height);
        }
    }
    
    /**
     * Nettoie le système après le rendu.
     */
    public void flush() {
        // Libérer les renderables
        for (Renderable renderable : renderQueue) {
            renderablePool.free(renderable);
        }
        renderQueue.clear();
        spatialGrid.clear();
    }
    
    /**
     * Classe interne pour les objets à rendre.
     */
    private static class Renderable {
        TextureRegion texture;
        float x, y, width, height;
        float rotation;
        int layer;
        boolean flipX, flipY;
        
        void set(TextureRegion texture, float x, float y, float width, float height,
                 float rotation, int layer, boolean flipX, boolean flipY) {
            this.texture = texture;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.rotation = rotation;
            this.layer = layer;
            this.flipX = flipX;
            this.flipY = flipY;
        }
        
        void reset() {
            texture = null;
            x = y = width = height = rotation = 0;
            layer = 0;
            flipX = flipY = false;
        }
    }
    
    /**
     * Grille spatiale pour le culling.
     */
    private static class SpatialGrid {
        private final float cellSize;
        private final int gridWidth, gridHeight;
        private final Array<Renderable>[][] cells;
        
        @SuppressWarnings("unchecked")
        SpatialGrid(float worldWidth, float worldHeight, int gridSize) {
            this.cellSize = gridSize;
            this.gridWidth = (int) Math.ceil(worldWidth / cellSize);
            this.gridHeight = (int) Math.ceil(worldHeight / cellSize);
            this.cells = new Array[gridWidth][gridHeight];
            
            for (int x = 0; x < gridWidth; x++) {
                for (int y = 0; y < gridHeight; y++) {
                    cells[x][y] = new Array<>();
                }
            }
        }
        
        void insert(Renderable renderable) {
            int cellX = (int) (renderable.x / cellSize);
            int cellY = (int) (renderable.y / cellSize);
            
            if (cellX >= 0 && cellX < gridWidth && cellY >= 0 && cellY < gridHeight) {
                cells[cellX][cellY].add(renderable);
            }
        }
        
        void query(Frustum frustum, Array<Renderable> result) {
            // Parcourir les cellules visibles
            for (int x = 0; x < gridWidth; x++) {
                for (int y = 0; y < gridHeight; y++) {
                    Array<Renderable> cell = cells[x][y];
                    for (Renderable renderable : cell) {
                        // Test de visibilité simplifié
                        if (isVisible(renderable, frustum)) {
                            result.add(renderable);
                        }
                    }
                }
            }
        }
        
        private boolean isVisible(Renderable renderable, Frustum frustum) {
            // Test de visibilité basique (peut être amélioré avec AABB)
            Vector3 point = new Vector3(renderable.x, renderable.y, 0);
            return frustum.pointInFrustum(point);
        }
        
        void clear() {
            for (int x = 0; x < gridWidth; x++) {
                for (int y = 0; y < gridHeight; y++) {
                    cells[x][y].clear();
                }
            }
        }
    }
    
    // Getters pour statistiques
    public int getRenderedObjects() { return renderedObjects; }
    public int getCulledObjects() { return culledObjects; }
    public int getBatches() { return batches; }
    public float getCullingRatio() { 
        return renderQueue.size > 0 ? (float)culledObjects / renderQueue.size : 0f; 
    }
}

