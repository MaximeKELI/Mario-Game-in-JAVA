package com.mariogame.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.mariogame.entities.Player;
import com.mariogame.utils.Constants;

/**
 * Gère la caméra qui suit le joueur avec un défilement fluide et des effets spéciaux.
 */
public class CameraManager {
    // Références
    private final OrthographicCamera camera;
    private final Player target;
    
    // Paramètres de la caméra
    private float viewportWidth;
    private float viewportHeight;
    private float zoom = 1.0f;
    private float targetZoom = 1.0f;
    private final float minZoom = 0.5f;
    private final float maxZoom = 2.0f;
    private final float zoomSpeed = 2.0f;
    
    // Paramètres de suivi
    private final Vector2 position = new Vector2();
    private final Vector2 targetPosition = new Vector2();
    private final Vector2 lookAhead = new Vector2();
    private final Vector2 shakeOffset = new Vector2();
    private float lookAheadFactor = 0.1f;
    private float smoothSpeed = 5.0f;
    private boolean isFollowing = true;
    
    // Effets de caméra
    private float shakeIntensity = 0f;
    private float shakeDuration = 0f;
    private float currentShakeTime = 0f;
    
    // Limites de la caméra
    private float minX = 0f;
    private float maxX = Float.MAX_VALUE;
    private float minY = 0f;
    private float maxY = Float.MAX_VALUE;
    
    public CameraManager(OrthographicCamera camera, Player target, float viewportWidth, float viewportHeight) {
        this.camera = camera;
        this.target = target;
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
        
        // Configurer la caméra
        camera.setToOrtho(false, viewportWidth, viewportHeight);
        camera.update();
    }
    
    /**
     * Met à jour la position et les effets de la caméra.
     * @param delta Temps écoulé depuis la dernière mise à jour
     */
    public void update(float delta) {
        if (target == null) return;
        
        // Mettre à jour la position cible si le suivi est activé
        if (isFollowing) {
            updateTargetPosition();
        }
        
        // Appliquer le lissage du mouvement
        position.lerp(targetPosition, smoothSpeed * delta);
        
        // Appliquer l'effet de zoom
        zoom = MathUtils.lerp(zoom, targetZoom, zoomSpeed * delta);
        zoom = MathUtils.clamp(zoom, minZoom, maxZoom);
        
        // Mettre à jour la position de la caméra avec le shake
        updateCameraPosition();
        
        // Mettre à jour l'effet de secousse (shake)
        updateShake(delta);
        
        // Mettre à jour la vue de la caméra
        camera.update();
    }
    
    /**
     * Met à jour la position cible de la caméra en fonction de la position du joueur.
     */
    private void updateTargetPosition() {
        // Suivre la position du joueur
        targetPosition.set(target.getPosition());
        
        // Ajouter un décalage de regard en avant (look-ahead) en fonction de la direction du joueur
        float lookAheadX = target.getFacingDirection() * lookAheadFactor * 2.0f;
        float lookAheadY = 0f; // Peut être utilisé pour regarder vers le haut/bas
        
        // Interpolation fluide du look-ahead
        lookAhead.x = MathUtils.lerp(lookAhead.x, lookAheadX, 5.0f * Gdx.graphics.getDeltaTime());
        lookAhead.y = MathUtils.lerp(lookAhead.y, lookAheadY, 5.0f * Gdx.graphics.getDeltaTime());
        
        targetPosition.add(lookAhead);
        
        // Appliquer les limites de la caméra
        float halfViewportWidth = (viewportWidth * zoom) / 2;
        float halfViewportHeight = (viewportHeight * zoom) / 2;
        
        targetPosition.x = MathUtils.clamp(
            targetPosition.x, 
            minX + halfViewportWidth, 
            maxX - halfViewportWidth
        );
        
        targetPosition.y = MathUtils.clamp(
            targetPosition.y, 
            minY + halfViewportHeight, 
            maxY - halfViewportHeight
        );
    }
    
    /**
     * Met à jour la position de la caméra en fonction de la position cible et des effets.
     */
    private void updateCameraPosition() {
        // Calculer la position de la caméra avec l'effet de shake
        float cameraX = position.x + shakeOffset.x;
        float cameraY = position.y + shakeOffset.y;
        
        // Définir la position de la caméra
        camera.position.set(cameraX, cameraY, 0);
        camera.zoom = zoom;
    }
    
    /**
     * Met à jour l'effet de secousse (shake) de la caméra.
     * @param delta Temps écoulé depuis la dernière mise à jour
     */
    private void updateShake(float delta) {
        if (currentShakeTime < shakeDuration) {
            // Mettre à jour le temps de shake
            currentShakeTime += delta;
            
            // Calculer l'intensité actuelle du shake (décroissante)
            float currentShake = shakeIntensity * (1 - (currentShakeTime / shakeDuration));
            
            // Générer un décalage aléatoire
            shakeOffset.set(
                MathUtils.random(-1f, 1f) * currentShake,
                MathUtils.random(-1f, 1f) * currentShake
            );
        } else {
            // Réinitialiser l'effet de shake
            shakeOffset.set(0, 0);
            shakeDuration = 0;
            currentShakeTime = 0;
        }
    }
    
    /**
     * Déclenche un effet de secousse de caméra.
     * @param intensity Intensité du shake
     * @param duration Durée du shake en secondes
     */
    public void shake(float intensity, float duration) {
        this.shakeIntensity = intensity;
        this.shakeDuration = duration;
        this.currentShakeTime = 0;
    }
    
    /**
     * Définit les limites de déplacement de la caméra.
     * @param minX Position X minimale
     * @param maxX Position X maximale
     * @param minY Position Y minimale
     * @param maxY Position Y maximale
     */
    public void setBounds(float minX, float maxX, float minY, float maxY) {
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
    }
    
    /**
     * Définit le facteur de zoom de la caméra.
     * @param zoom Niveau de zoom (entre minZoom et maxZoom)
     */
    public void setZoom(float zoom) {
        this.targetZoom = MathUtils.clamp(zoom, minZoom, maxZoom);
    }
    
    /**
     * Active ou désactive le suivi du joueur.
     * @param following true pour activer le suivi, false pour le désactiver
     */
    public void setFollowing(boolean following) {
        this.isFollowing = following;
    }
    
    /**
     * Définit la vitesse de lissage du mouvement de la caméra.
     * @param smoothSpeed Vitesse de lissage (plus élevé = plus fluide mais plus lent)
     */
    public void setSmoothSpeed(float smoothSpeed) {
        this.smoothSpeed = Math.max(0.1f, smoothSpeed);
    }
    
    /**
     * Convertit les coordonnées de l'écran en coordonnées du monde.
     * @param screenX Position X à l'écran
     * @param screenY Position Y à l'écran
     * @return Vecteur contenant les coordonnées du monde
     */
    public Vector2 unproject(float screenX, float screenY) {
        Vector3 worldCoords = camera.unproject(new Vector3(screenX, screenY, 0));
        return new Vector2(worldCoords.x, worldCoords.y);
    }
    
    /**
     * Vérifie si un point est visible à l'écran.
     * @param x Position X dans le monde
     * @param y Position Y dans le monde
     * @param margin Marge autour de l'écran (en unités du monde)
     * @return true si le point est visible, false sinon
     */
    public boolean isInView(float x, float y, float margin) {
        float halfWidth = (viewportWidth * zoom) / 2 + margin;
        float halfHeight = (viewportHeight * zoom) / 2 + margin;
        
        return x >= position.x - halfWidth && 
               x <= position.x + halfWidth &&
               y >= position.y - halfHeight && 
               y <= position.y + halfHeight;
    }
    
    // Getters
    
    public OrthographicCamera getCamera() {
        return camera;
    }
    
    public float getZoom() {
        return zoom;
    }
    
    public float getViewportWidth() {
        return viewportWidth * zoom;
    }
    
    public float getViewportHeight() {
        return viewportHeight * zoom;
    }
    
    public Vector2 getPosition() {
        return new Vector2(position);
    }
    
    /**
     * Redimensionne la vue de la caméra.
     * @param width Nouvelle largeur de la vue
     * @param height Nouvelle hauteur de la vue
     */
    public void resize(float width, float height) {
        viewportWidth = width;
        viewportHeight = height;
        camera.setToOrtho(false, viewportWidth, viewportHeight);
        camera.update();
    }
}
