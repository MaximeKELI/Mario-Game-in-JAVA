package com.mariogame.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.mariogame.entities.Player;
import com.mariogame.utils.Constants;

/**
 * Gère l'environnement dynamique du jeu (jour/nuit, météo, effets atmosphériques).
 */
public class EnvironmentManager {
    // Constantes pour le cycle jour/nuit
    private static final float DAY_LENGTH = 300f; // en secondes
    private static final float DAWN_START = 0.2f; // 20% du cycle
    private static final float DAY_START = 0.3f;  // 30% du cycle
    private static final float DUSK_START = 0.7f; // 70% du cycle
    private static final float NIGHT_START = 0.8f; // 80% du cycle
    
    // Couleurs pour les différentes phases de la journée
    private final Color DAY_COLOR = new Color(0.7f, 0.8f, 1f, 0.3f);
    private final Color DUSK_COLOR = new Color(0.9f, 0.6f, 0.4f, 0.4f);
    private final Color NIGHT_COLOR = new Color(0.1f, 0.1f, 0.3f, 0.6f);
    private final Color DAWN_COLOR = new Color(0.6f, 0.7f, 0.9f, 0.4f);
    
    // Variables d'état
    private float timeOfDay = 0.3f; // Commence à 30% du cycle (jour)
    private boolean isRaining = false;
    private float rainIntensity = 0f;
    private final Array<RainDrop> raindrops = new Array<>();
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    
    // Références
    private final Player player;
    private final OrthographicCamera camera;
    
    public EnvironmentManager(Player player, OrthographicCamera camera) {
        this.player = player;
        this.camera = camera;
        
        // Initialiser les particules de pluie
        for (int i = 0; i < 500; i++) {
            raindrops.add(new RainDrop());
        }
    }
    
    public void update(float deltaTime) {
        // Mettre à jour l'heure de la journée
        updateTimeOfDay(deltaTime);
        
        // Mettre à jour la météo
        updateWeather(deltaTime);
        
        // Mettre à jour les particules de pluie
        if (isRaining) {
            updateRain(deltaTime);
        }
    }
    
    private void updateTimeOfDay(float deltaTime) {
        // Avancer le temps (boucle de 0 à 1)
        timeOfDay = (timeOfDay + (deltaTime / DAY_LENGTH)) % 1f;
        
        // Mettre à jour l'éclairage en fonction de l'heure
        updateLighting();
    }
    
    private void updateLighting() {
        // Ici, vous pourriez mettre à jour l'éclairage du jeu
        // en fonction de l'heure de la journée
    }
    
    private void updateWeather(float deltaTime) {
        // Changement aléatoire de météo
        if (MathUtils.random() < 0.0005f * deltaTime * 60) {
            isRaining = !isRaining;
            rainIntensity = isRaining ? MathUtils.random(0.3f, 1f) : 0f;
        }
    }
    
    private void updateRain(float deltaTime) {
        for (RainDrop drop : raindrops) {
            // Mettre à jour la position de la goutte de pluie
            drop.y -= drop.speed * deltaTime * 60 * rainIntensity;
            
            // Réinitialiser les gouttes qui sont tombées hors de l'écran
            if (drop.y < camera.position.y - camera.viewportHeight / 2) {
                resetRainDrop(drop);
            }
        }
    }
    
    private void resetRainDrop(RainDrop drop) {
        float viewportWidth = camera.viewportWidth * 1.5f;
        float viewportHeight = camera.viewportHeight * 1.5f;
        
        drop.x = camera.position.x - viewportWidth / 2 + MathUtils.random(viewportWidth);
        drop.y = camera.position.y + viewportHeight / 2 + MathUtils.random(100);
        drop.speed = MathUtils.random(300f, 800f) * rainIntensity;
        drop.length = MathUtils.random(10f, 30f) * rainIntensity;
    }
    
    public void render(SpriteBatch batch) {
        // Sauvegarder l'état du batch
        batch.end();
        
        // Rendu des effets atmosphériques
        renderAtmosphericEffects();
        
        // Rendu de la pluie
        if (isRaining) {
            renderRain();
        }
        
        // Rendu de l'effet jour/nuit
        renderDayNightCycle();
        
        // Reprendre le rendu normal
        batch.begin();
    }
    
    private void renderAtmosphericEffects() {
        // Ici, vous pourriez ajouter des effets atmosphériques comme du brouillard
    }
    
    private void renderRain() {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        
        for (RainDrop drop : raindrops) {
            // Couleur bleu clair pour la pluie
            shapeRenderer.setColor(0.6f, 0.7f, 1f, 0.6f * rainIntensity);
            shapeRenderer.line(drop.x, drop.y, drop.x, drop.y - drop.length);
        }
        
        shapeRenderer.end();
    }
    
    private void renderDayNightCycle() {
        Color color = getCurrentLightColor();
        
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(color);
        shapeRenderer.rect(
            camera.position.x - camera.viewportWidth / 2,
            camera.position.y - camera.viewportHeight / 2,
            camera.viewportWidth,
            camera.viewportHeight
        );
        shapeRenderer.end();
    }
    
    private Color getCurrentLightColor() {
        if (timeOfDay < DAWN_START) {
            // Nuit à aube
            float t = timeOfDay / DAWN_START;
            return NIGHT_COLOR.cpy().lerp(DAWN_COLOR, t);
        } else if (timeOfDay < DAY_START) {
            // Aube à jour
            float t = (timeOfDay - DAWN_START) / (DAY_START - DAWN_START);
            return DAWN_COLOR.cpy().lerp(DAY_COLOR, t);
        } else if (timeOfDay < DUSK_START) {
            // Jour
            return DAY_COLOR;
        } else if (timeOfDay < NIGHT_START) {
            // Crépuscule
            float t = (timeOfDay - DUSK_START) / (NIGHT_START - DUSK_START);
            return DAY_COLOR.cpy().lerp(DUSK_COLOR, t);
        } else {
            // Nuit
            float t = (timeOfDay - NIGHT_START) / (1f - NIGHT_START);
            return DUSK_COLOR.cpy().lerp(NIGHT_COLOR, t);
        }
    }
    
    public void resize(int width, int height) {
        // Réinitialiser les gouttes de pluie lors d'un redimensionnement
        for (RainDrop drop : raindrops) {
            resetRainDrop(drop);
        }
    }
    
    public void dispose() {
        shapeRenderer.dispose();
    }
    
    // Classe interne pour les gouttes de pluie
    private static class RainDrop {
        float x, y;
        float speed;
        float length;
        
        public RainDrop() {
            // Les valeurs seront initialisées par resetRainDrop()
        }
    }
    
    // Getters et setters
    public float getTimeOfDay() {
        return timeOfDay;
    }
    
    public boolean isDay() {
        return timeOfDay >= DAY_START && timeOfDay < DUSK_START;
    }
    
    public boolean isNight() {
        return timeOfDay >= NIGHT_START || timeOfDay < DAWN_START;
    }
    
    public boolean isRaining() {
        return isRaining;
    }
    
    public float getRainIntensity() {
        return rainIntensity;
    }
}
