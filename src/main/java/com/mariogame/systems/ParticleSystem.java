package com.mariogame.systems;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.mariogame.utils.Constants;

/**
 * Système de particules ultra-performant avec effets avancés.
 * Supporte des milliers de particules avec interpolation et effets physiques.
 */
public class ParticleSystem {
    private static final int MAX_PARTICLES = 10000;
    private static final int INITIAL_POOL_SIZE = 1000;
    
    private final Array<Particle> activeParticles = new Array<>(MAX_PARTICLES);
    private final Pool<Particle> particlePool = new Pool<Particle>(INITIAL_POOL_SIZE, MAX_PARTICLES) {
        @Override
        protected Particle newObject() {
            return new Particle();
        }
    };
    
    private final Array<Emitter> emitters = new Array<>();
    
    /**
     * Crée une explosion de particules.
     */
    public void createExplosion(float x, float y, int count, Color color, float speed) {
        for (int i = 0; i < count; i++) {
            Particle particle = particlePool.obtain();
            particle.init(x, y, 
                         MathUtils.random(0, 360), 
                         MathUtils.random(speed * 0.5f, speed * 1.5f),
                         color,
                         MathUtils.random(0.3f, 1.0f),
                         MathUtils.random(0.5f, 2.0f));
            activeParticles.add(particle);
        }
    }
    
    /**
     * Crée un effet de collecte (pièce, power-up).
     */
    public void createCollectEffect(float x, float y, Color color) {
        for (int i = 0; i < 20; i++) {
            Particle particle = particlePool.obtain();
            float angle = (360f / 20f) * i;
            particle.init(x, y, angle, MathUtils.random(2f, 5f), color, 1f, MathUtils.random(0.3f, 0.8f));
            activeParticles.add(particle);
        }
    }
    
    /**
     * Crée un effet de saut.
     */
    public void createJumpEffect(float x, float y) {
        for (int i = 0; i < 15; i++) {
            Particle particle = particlePool.obtain();
            float angle = MathUtils.random(180f, 360f); // Vers le bas
            particle.init(x, y, angle, MathUtils.random(1f, 3f), Color.WHITE, 0.8f, MathUtils.random(0.2f, 0.5f));
            activeParticles.add(particle);
        }
    }
    
    /**
     * Crée un effet de dash.
     */
    public void createDashEffect(float x, float y, boolean facingRight) {
        Color dashColor = new Color(0.5f, 0.8f, 1f, 0.9f);
        for (int i = 0; i < 30; i++) {
            Particle particle = particlePool.obtain();
            float angle = facingRight ? MathUtils.random(-30f, 30f) : MathUtils.random(150f, 210f);
            particle.init(x, y, angle, MathUtils.random(5f, 10f), dashColor, 0.9f, MathUtils.random(0.3f, 0.6f));
            activeParticles.add(particle);
        }
    }
    
    /**
     * Crée un effet de feu.
     */
    public void createFireEffect(float x, float y, float intensity) {
        for (int i = 0; i < (int)(intensity * 20); i++) {
            Particle particle = particlePool.obtain();
            float angle = MathUtils.random(80f, 100f); // Vers le haut
            Color fireColor = new Color(
                MathUtils.random(0.8f, 1f),
                MathUtils.random(0.3f, 0.6f),
                0f,
                MathUtils.random(0.7f, 1f)
            );
            particle.init(x + MathUtils.random(-0.2f, 0.2f), y, angle, 
                         MathUtils.random(1f, 3f), fireColor, 1f, MathUtils.random(0.2f, 0.5f));
            activeParticles.add(particle);
        }
    }
    
    /**
     * Met à jour toutes les particules.
     */
    public void update(float deltaTime) {
        // Mettre à jour les émetteurs
        for (int i = emitters.size - 1; i >= 0; i--) {
            Emitter emitter = emitters.get(i);
            emitter.update(deltaTime);
            if (emitter.isFinished()) {
                emitters.removeIndex(i);
            }
        }
        
        // Mettre à jour les particules
        for (int i = activeParticles.size - 1; i >= 0; i--) {
            Particle particle = activeParticles.get(i);
            particle.update(deltaTime);
            
            if (particle.isDead()) {
                activeParticles.removeIndex(i);
                particlePool.free(particle);
            }
        }
    }
    
    /**
     * Rend toutes les particules.
     */
    public void render(SpriteBatch batch) {
        batch.setColor(Color.WHITE);
        
        for (Particle particle : activeParticles) {
            particle.render(batch);
        }
    }
    
    /**
     * Nettoie toutes les particules.
     */
    public void clear() {
        for (Particle particle : activeParticles) {
            particlePool.free(particle);
        }
        activeParticles.clear();
        emitters.clear();
    }
    
    /**
     * Classe représentant une particule.
     */
    private static class Particle {
        Vector2 position = new Vector2();
        Vector2 velocity = new Vector2();
        Color color = new Color();
        float life;
        float maxLife;
        float size;
        float rotation;
        float rotationSpeed;
        
        void init(float x, float y, float angle, float speed, Color color, float life, float size) {
            position.set(x, y);
            float radians = angle * MathUtils.degreesToRadians;
            velocity.set(MathUtils.cos(radians) * speed, MathUtils.sin(radians) * speed);
            this.color.set(color);
            this.life = life;
            this.maxLife = life;
            this.size = size;
            this.rotation = MathUtils.random(0, 360);
            this.rotationSpeed = MathUtils.random(-180f, 180f);
        }
        
        void update(float deltaTime) {
            // Physique
            position.add(velocity.x * deltaTime, velocity.y * deltaTime);
            
            // Gravité
            velocity.y += Constants.WorldConfig.GRAVITY * deltaTime * 0.1f;
            
            // Friction
            velocity.scl(0.98f);
            
            // Vie
            life -= deltaTime;
            
            // Rotation
            rotation += rotationSpeed * deltaTime;
            
            // Fade out
            float alpha = Math.max(0, life / maxLife);
            color.a = alpha;
        }
        
        void render(SpriteBatch batch) {
            batch.setColor(color);
            // Rendu simplifié (peut être amélioré avec des textures)
            // Ici on dessine un carré simple, mais on pourrait utiliser des sprites
            float halfSize = size * Constants.WorldConfig.PPM * 0.5f;
            float x = position.x * Constants.WorldConfig.PPM - halfSize;
            float y = position.y * Constants.WorldConfig.PPM - halfSize;
            
            // Utiliser une texture de particule si disponible
            // batch.draw(particleTexture, x, y, halfSize, halfSize, size * PPM, size * PPM, 1, 1, rotation);
        }
        
        boolean isDead() {
            return life <= 0 || position.y < -10; // Hors écran
        }
    }
    
    /**
     * Émetteur de particules continu.
     */
    private static class Emitter {
        float x, y;
        float rate; // Particules par seconde
        float timer;
        Color color;
        float duration;
        float elapsed;
        
        void update(float deltaTime) {
            elapsed += deltaTime;
            timer += deltaTime;
            
            if (timer >= 1f / rate) {
                // Émettre une particule
                timer = 0;
            }
        }
        
        boolean isFinished() {
            return elapsed >= duration;
        }
    }
    
    public int getActiveParticleCount() {
        return activeParticles.size;
    }
}

