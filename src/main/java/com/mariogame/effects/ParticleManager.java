package com.mariogame.effects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;
import com.mariogame.utils.Constants;

/**
 * Gère les effets de particules du jeu avec un système de pooling pour de meilleures performances.
 */
public class ParticleManager implements Disposable {
    private static final String PARTICLE_DIR = "particles/";
    
    private final ObjectMap<String, ParticleEffectPool> effectPools = new ObjectMap<>();
    private final Array<ParticleEffectPool.PooledEffect> activeEffects = new Array<>();
    private final SpriteBatch batch;
    
    public ParticleManager(SpriteBatch batch) {
        this.batch = batch;
        loadEffects();
    }
    
    /**
     * Charge tous les effets de particules utilisés dans le jeu.
     */
    private void loadEffects() {
        // Effet d'explosion
        createEffectPool("explosion", 2, 5);
        
        // Effet de poussière lors de la course
        createEffectPool("dust", 5, 10);
        
        // Effet de particules d'eau
        createEffectPool("water_splash", 3, 8);
        
        // Effet d'étincelles
        createEffectPool("sparks", 5, 10);
        
        // Effet de fumée
        createEffectPool("smoke", 3, 8);
        
        // Effet de pièces
        createEffectPool("coin_glow", 10, 20);
    }
    
    /**
     * Crée un pool pour un effet de particules.
     */
    private void createEffectPool(String name, int initialCapacity, int maxCapacity) {
        ParticleEffect template = new ParticleEffect();
        template.load(Gdx.files.internal(PARTICLE_DIR + name + ".p"), 
                     Gdx.files.internal(""));
        
        effectPools.put(name, new ParticleEffectPool(template, initialCapacity, maxCapacity));
    }
    
    /**
     * Crée un effet de particules à la position spécifiée.
     */
    public void createEffect(String effectName, float x, float y) {
        createEffect(effectName, x, y, 0);
    }
    
    /**
     * Crée un effet de particules avec une rotation spécifique.
     */
    public void createEffect(String effectName, float x, float y, float rotation) {
        createEffect(effectName, x, y, rotation, Color.WHITE);
    }
    
    /**
     * Crée un effet de particules avec une couleur spécifique.
     */
    public void createEffect(String effectName, float x, float y, float rotation, Color color) {
        if (!effectPools.containsKey(effectName)) {
            Gdx.app.error("ParticleManager", "Effect not found: " + effectName);
            return;
        }
        
        ParticleEffectPool pool = effectPools.get(effectName);
        ParticleEffectPool.PooledEffect effect = pool.obtain();
        
        // Configurer l'effet
        effect.setPosition(x, y);
        effect.getEmitters().first().setRotation(rotation);
        effect.getEmitters().first().tint(new float[]{
            color.r, color.g, color.b, 1f,
            color.r, color.g, color.b, 0.5f
        });
        
        // Activer l'effet et l'ajouter à la liste des effets actifs
        effect.start();
        activeEffects.add(effect);
    }
    
    /**
     * Met à jour tous les effets de particules actifs.
     */
    public void update(float deltaTime) {
        for (int i = activeEffects.size - 1; i >= 0; i--) {
            ParticleEffectPool.PooledEffect effect = activeEffects.get(i);
            effect.update(deltaTime);
            
            // Retirer les effets terminés
            if (effect.isComplete()) {
                effect.free();
                activeEffects.removeIndex(i);
            }
        }
    }
    
    /**
     * Dessine tous les effets de particules actifs.
     */
    public void render() {
        batch.setProjectionMatrix(Constants.getGameViewport().getCamera().combined);
        batch.begin();
        
        for (ParticleEffectPool.PooledEffect effect : activeEffects) {
            effect.draw(batch);
        }
        
        batch.end();
    }
    
    /**
     * Effet d'explosion à une position donnée.
     */
    public void createExplosion(float x, float y) {
        createEffect("explosion", x, y);
    }
    
    /**
     * Effet de poussière lors de la course.
     */
    public void createDustEffect(float x, float y, boolean facingRight) {
        float offsetX = facingRight ? -0.3f : 0.3f;
        createEffect("dust", x + offsetX, y + 0.2f);
    }
    
    /**
     * Effet d'éclaboussure d'eau.
     */
    public void createWaterSplash(float x, float y) {
        createEffect("water_splash", x, y);
    }
    
    /**
     * Effet d'étincelles.
     */
    public void createSparks(float x, float y, float rotation) {
        createEffect("sparks", x, y, rotation);
    }
    
    /**
     * Effet de fumée.
     */
    public void createSmoke(float x, float y, Color color) {
        createEffect("smoke", x, y, 0, color);
    }
    
    /**
     * Effet de pièces qui brillent.
     */
    public void createCoinEffect(float x, float y) {
        createEffect("coin_glow", x, y);
    }
    
    /**
     * Libère toutes les ressources utilisées par le gestionnaire de particules.
     */
    @Override
    public void dispose() {
        // Libérer tous les effets actifs
        for (ParticleEffectPool.PooledEffect effect : activeEffects) {
            effect.free();
        }
        activeEffects.clear();
        
        // Libérer les pools d'effets
        for (ParticleEffectPool pool : effectPools.values()) {
            pool.clear();
        }
        effectPools.clear();
    }
}
