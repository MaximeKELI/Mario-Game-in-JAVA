package com.mariogame.systems;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.mariogame.utils.Constants;

/**
 * Système de lighting dynamique 2D avec ombres et éclairage réaliste.
 * Supporte les lumières directionnelles, ponctuelles et spotlights.
 */
public class LightingSystem {
    private final Array<Light> lights = new Array<>();
    private final Array<ShadowCaster> shadowCasters = new Array<>();
    private Color ambientLight = new Color(0.3f, 0.3f, 0.3f, 1f);
    private boolean enabled = true;
    
    /**
     * Ajoute une lumière ponctuelle.
     */
    public Light addPointLight(float x, float y, float radius, Color color, float intensity) {
        Light light = new Light(LightType.POINT, x, y, radius, color, intensity);
        lights.add(light);
        return light;
    }
    
    /**
     * Ajoute une lumière directionnelle (soleil).
     */
    public Light addDirectionalLight(float angle, Color color, float intensity) {
        Light light = new Light(LightType.DIRECTIONAL, 0, 0, 0, color, intensity);
        light.angle = angle;
        lights.add(light);
        return light;
    }
    
    /**
     * Ajoute un caster d'ombre.
     */
    public void addShadowCaster(float x, float y, float width, float height) {
        ShadowCaster caster = new ShadowCaster(x, y, width, height);
        shadowCasters.add(caster);
    }
    
    /**
     * Calcule l'intensité de la lumière à un point donné.
     */
    public float getLightIntensity(float x, float y) {
        if (!enabled) return 1f;
        
        float totalIntensity = ambientLight.r;
        
        for (Light light : lights) {
            if (light.type == LightType.POINT) {
                float distance = Vector2.dst(x, y, light.x, light.y);
                if (distance < light.radius) {
                    float attenuation = 1f - (distance / light.radius);
                    totalIntensity += light.intensity * attenuation * light.color.r;
                }
            } else if (light.type == LightType.DIRECTIONAL) {
                totalIntensity += light.intensity * light.color.r;
            }
        }
        
        // Vérifier les ombres
        for (ShadowCaster caster : shadowCasters) {
            if (isInShadow(x, y, caster)) {
                totalIntensity *= 0.3f; // Réduire l'intensité dans l'ombre
            }
        }
        
        return Math.min(1f, totalIntensity);
    }
    
    /**
     * Vérifie si un point est dans l'ombre d'un caster.
     */
    private boolean isInShadow(float x, float y, ShadowCaster caster) {
        // Vérification simplifiée (peut être améliorée avec raycasting)
        return x >= caster.x && x <= caster.x + caster.width &&
               y >= caster.y && y <= caster.y + caster.height;
    }
    
    /**
     * Applique l'éclairage à un sprite lors du rendu.
     */
    public void applyLighting(SpriteBatch batch, float x, float y) {
        if (!enabled) {
            batch.setColor(Color.WHITE);
            return;
        }
        
        float intensity = getLightIntensity(x, y);
        Color lightingColor = new Color(intensity, intensity, intensity, 1f);
        batch.setColor(lightingColor);
    }
    
    /**
     * Met à jour le système d'éclairage.
     */
    public void update(float deltaTime) {
        // Mettre à jour les lumières animées
        for (Light light : lights) {
            if (light.flickering) {
                light.intensity = light.baseIntensity + 
                    (float)Math.sin(System.currentTimeMillis() * 0.01) * 0.2f;
            }
        }
    }
    
    /**
     * Nettoie le système.
     */
    public void clear() {
        lights.clear();
        shadowCasters.clear();
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public void setAmbientLight(Color color) {
        this.ambientLight.set(color);
    }
    
    /**
     * Type de lumière.
     */
    public enum LightType {
        POINT, DIRECTIONAL, SPOT
    }
    
    /**
     * Représente une source de lumière.
     */
    public static class Light {
        public LightType type;
        public float x, y;
        public float radius;
        public Color color;
        public float intensity;
        public float baseIntensity;
        public float angle;
        public boolean flickering = false;
        
        public Light(LightType type, float x, float y, float radius, Color color, float intensity) {
            this.type = type;
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.color = new Color(color);
            this.intensity = intensity;
            this.baseIntensity = intensity;
        }
        
        public void setFlickering(boolean flickering) {
            this.flickering = flickering;
        }
    }
    
    /**
     * Objet qui projette des ombres.
     */
    private static class ShadowCaster {
        float x, y, width, height;
        
        ShadowCaster(float x, float y, float width, float height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }
}

