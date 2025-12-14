package com.mariogame.utils;

import com.badlogic.gdx.utils.Pool;

/**
 * Pool d'objets générique ultra-optimisé pour éviter les allocations.
 * Réduit drastiquement le garbage collection.
 */
public class ObjectPool<T> extends Pool<T> {
    private final PoolFactory<T> factory;
    private int peakSize = 0;
    
    public ObjectPool(PoolFactory<T> factory) {
        this.factory = factory;
    }
    
    public ObjectPool(PoolFactory<T> factory, int initialCapacity, int max) {
        super(initialCapacity, max);
        this.factory = factory;
    }
    
    @Override
    protected T newObject() {
        return factory.create();
    }
    
    @Override
    public T obtain() {
        T obj = super.obtain();
        if (getFree() > peakSize) {
            peakSize = getFree();
        }
        return obj;
    }
    
    @Override
    public void free(T object) {
        if (object == null) return;
        reset(object);
        super.free(object);
    }
    
    /**
     * Réinitialise un objet avant de le remettre dans le pool.
     */
    protected void reset(T object) {
        // À surcharger dans les sous-classes si nécessaire
    }
    
    /**
     * Interface pour créer de nouveaux objets.
     */
    public interface PoolFactory<T> {
        T create();
    }
    
    public int getPeakSize() {
        return peakSize;
    }
    
    public void clearStats() {
        peakSize = 0;
    }
}

