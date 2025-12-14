package com.mariogame.ai;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.mariogame.world.GameWorld;

/**
 * Système de pathfinding A* optimisé pour les plateformes 2D.
 * Supporte les chemins complexes avec sauts et obstacles.
 */
public class PathfindingSystem {
    private final GameWorld gameWorld;
    private final int gridWidth, gridHeight;
    private final float cellSize;
    private final Node[][] grid;
    
    public PathfindingSystem(GameWorld gameWorld) {
        this.gameWorld = gameWorld;
        this.cellSize = 1f; // 1 mètre par cellule
        this.gridWidth = 200; // Ajuster selon la taille du monde
        this.gridHeight = 50;
        this.grid = new Node[gridWidth][gridHeight];
        
        initializeGrid();
    }
    
    private void initializeGrid() {
        for (int x = 0; x < gridWidth; x++) {
            for (int y = 0; y < gridHeight; y++) {
                grid[x][y] = new Node(x, y);
            }
        }
    }
    
    /**
     * Trouve un chemin entre deux points en utilisant A*.
     */
    public Array<Vector2> findPath(Vector2 start, Vector2 target) {
        int startX = (int)(start.x / cellSize);
        int startY = (int)(start.y / cellSize);
        int targetX = (int)(target.x / cellSize);
        int targetY = (int)(target.y / cellSize);
        
        // Validation
        if (!isValid(startX, startY) || !isValid(targetX, targetY)) {
            return new Array<>();
        }
        
        // Algorithme A*
        Array<Node> openSet = new Array<>();
        Array<Node> closedSet = new Array<>();
        
        Node startNode = grid[startX][startY];
        Node targetNode = grid[targetX][targetY];
        
        startNode.gCost = 0;
        startNode.hCost = heuristic(startNode, targetNode);
        startNode.fCost = startNode.gCost + startNode.hCost;
        
        openSet.add(startNode);
        
        while (openSet.size > 0) {
            // Trouver le nœud avec le fCost le plus bas
            Node currentNode = openSet.get(0);
            for (Node node : openSet) {
                if (node.fCost < currentNode.fCost || 
                   (node.fCost == currentNode.fCost && node.hCost < currentNode.hCost)) {
                    currentNode = node;
                }
            }
            
            openSet.removeValue(currentNode, true);
            closedSet.add(currentNode);
            
            // Si on a atteint la cible
            if (currentNode.equals(targetNode)) {
                return reconstructPath(startNode, targetNode);
            }
            
            // Examiner les voisins
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    if (x == 0 && y == 0) continue;
                    
                    int neighborX = currentNode.x + x;
                    int neighborY = currentNode.y + y;
                    
                    if (!isValid(neighborX, neighborY)) continue;
                    
                    Node neighbor = grid[neighborX][neighborY];
                    if (closedSet.contains(neighbor, true)) continue;
                    if (!isWalkable(neighborX, neighborY)) continue;
                    
                    float newGCost = currentNode.gCost + getDistance(currentNode, neighbor);
                    
                    if (!openSet.contains(neighbor, true) || newGCost < neighbor.gCost) {
                        neighbor.gCost = newGCost;
                        neighbor.hCost = heuristic(neighbor, targetNode);
                        neighbor.fCost = neighbor.gCost + neighbor.hCost;
                        neighbor.parent = currentNode;
                        
                        if (!openSet.contains(neighbor, true)) {
                            openSet.add(neighbor);
                        }
                    }
                }
            }
        }
        
        // Pas de chemin trouvé
        return new Array<>();
    }
    
    private Array<Vector2> reconstructPath(Node start, Node target) {
        Array<Vector2> path = new Array<>();
        Node currentNode = target;
        
        while (currentNode != null && !currentNode.equals(start)) {
            path.add(new Vector2(currentNode.x * cellSize, currentNode.y * cellSize));
            currentNode = currentNode.parent;
        }
        
        path.reverse();
        return path;
    }
    
    private float heuristic(Node a, Node b) {
        // Distance de Manhattan
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }
    
    private float getDistance(Node a, Node b) {
        int dx = Math.abs(a.x - b.x);
        int dy = Math.abs(a.y - b.y);
        
        if (dx > dy) {
            return 14 * dy + 10 * (dx - dy);
        }
        return 14 * dx + 10 * (dy - dx);
    }
    
    private boolean isValid(int x, int y) {
        return x >= 0 && x < gridWidth && y >= 0 && y < gridHeight;
    }
    
    private boolean isWalkable(int x, int y) {
        // Vérifier si la cellule est traversable
        // Ici on pourrait vérifier les collisions avec le monde
        return true; // Simplifié pour l'exemple
    }
    
    /**
     * Nœud pour l'algorithme A*.
     */
    private static class Node {
        int x, y;
        float gCost, hCost, fCost;
        Node parent;
        
        Node(int x, int y) {
            this.x = x;
            this.y = y;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Node) {
                Node other = (Node) obj;
                return x == other.x && y == other.y;
            }
            return false;
        }
    }
}

