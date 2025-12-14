package com.mariogame.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.TimeUtils;

/**
 * Profiler ultra-précis pour monitoring en temps réel des performances.
 * Mesure le temps d'exécution de chaque système avec précision nanoseconde.
 */
public class PerformanceProfiler {
    private static final int MAX_SAMPLES = 60;
    
    private final ObjectMap<String, ProfileData> profiles = new ObjectMap<>();
    private final Array<String> activeProfiles = new Array<>();
    private boolean enabled = true;
    
    /**
     * Démarre le profilage d'une section.
     */
    public void start(String name) {
        if (!enabled) return;
        
        ProfileData data = profiles.get(name);
        if (data == null) {
            data = new ProfileData(name);
            profiles.put(name, data);
        }
        
        data.startTime = TimeUtils.nanoTime();
        activeProfiles.add(name);
    }
    
    /**
     * Arrête le profilage d'une section.
     */
    public void end(String name) {
        if (!enabled) return;
        
        ProfileData data = profiles.get(name);
        if (data == null) return;
        
        long endTime = TimeUtils.nanoTime();
        long duration = endTime - data.startTime;
        
        data.addSample(duration);
        activeProfiles.removeValue(name, false);
    }
    
    /**
     * Obtient les statistiques d'une section.
     */
    public ProfileStats getStats(String name) {
        ProfileData data = profiles.get(name);
        if (data == null) return null;
        
        return data.getStats();
    }
    
    /**
     * Affiche toutes les statistiques dans la console.
     */
    public void printStats() {
        if (!enabled) return;
        
        Gdx.app.log("Profiler", "=== Performance Stats ===");
        for (ObjectMap.Entry<String, ProfileData> entry : profiles) {
            ProfileStats stats = entry.value.getStats();
            Gdx.app.log("Profiler", String.format("%s: %.3fms (avg: %.3fms, min: %.3fms, max: %.3fms)", 
                entry.key, stats.lastTime / 1_000_000f, stats.avgTime / 1_000_000f,
                stats.minTime / 1_000_000f, stats.maxTime / 1_000_000f));
        }
    }
    
    /**
     * Réinitialise toutes les statistiques.
     */
    public void reset() {
        profiles.clear();
        activeProfiles.clear();
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Données de profilage pour une section.
     */
    private static class ProfileData {
        final String name;
        final Array<Long> samples = new Array<>(MAX_SAMPLES);
        long startTime;
        
        ProfileData(String name) {
            this.name = name;
        }
        
        void addSample(long duration) {
            samples.add(duration);
            if (samples.size > MAX_SAMPLES) {
                samples.removeIndex(0);
            }
        }
        
        ProfileStats getStats() {
            if (samples.size == 0) {
                return new ProfileStats(0, 0, 0, 0);
            }
            
            long last = samples.peek();
            long sum = 0;
            long min = Long.MAX_VALUE;
            long max = Long.MIN_VALUE;
            
            for (long sample : samples) {
                sum += sample;
                min = Math.min(min, sample);
                max = Math.max(max, sample);
            }
            
            long avg = sum / samples.size;
            
            return new ProfileStats(last, avg, min, max);
        }
    }
    
    /**
     * Statistiques de performance.
     */
    public static class ProfileStats {
        public final long lastTime;
        public final long avgTime;
        public final long minTime;
        public final long maxTime;
        
        ProfileStats(long lastTime, long avgTime, long minTime, long maxTime) {
            this.lastTime = lastTime;
            this.avgTime = avgTime;
            this.minTime = minTime;
            this.maxTime = maxTime;
        }
    }
}

