package com.mariogame.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * Système de shaders personnalisés pour effets visuels avancés.
 * Supporte les shaders de post-processing et d'effets spéciaux.
 */
public class ShaderSystem {
    private final ObjectMap<String, ShaderProgram> shaders = new ObjectMap<>();
    private ShaderProgram currentShader;
    
    /**
     * Charge un shader depuis les fichiers.
     */
    public boolean loadShader(String name, String vertexShaderPath, String fragmentShaderPath) {
        try {
            String vertexShader = Gdx.files.internal(vertexShaderPath).readString();
            String fragmentShader = Gdx.files.internal(fragmentShaderPath).readString();
            
            ShaderProgram shader = new ShaderProgram(vertexShader, fragmentShader);
            
            if (!shader.isCompiled()) {
                Gdx.app.error("ShaderSystem", "Shader compilation failed: " + shader.getLog());
                return false;
            }
            
            shaders.put(name, shader);
            Gdx.app.log("ShaderSystem", "Shader loaded: " + name);
            return true;
        } catch (Exception e) {
            Gdx.app.error("ShaderSystem", "Error loading shader: " + name, e);
            return false;
        }
    }
    
    /**
     * Crée un shader par défaut pour les effets de base.
     */
    public void createDefaultShaders() {
        // Shader de distorsion
        loadShader("distortion", "shaders/distortion.vert", "shaders/distortion.frag");
        
        // Shader de bloom
        loadShader("bloom", "shaders/bloom.vert", "shaders/bloom.frag");
        
        // Shader de chromatic aberration
        loadShader("chromatic", "shaders/chromatic.vert", "shaders/chromatic.frag");
    }
    
    /**
     * Active un shader.
     */
    public void useShader(String name) {
        ShaderProgram shader = shaders.get(name);
        if (shader != null) {
            currentShader = shader;
            shader.bind();
        }
    }
    
    /**
     * Désactive le shader actuel.
     */
    public void disableShader() {
        if (currentShader != null) {
            currentShader.end();
            currentShader = null;
        }
    }
    
    /**
     * Obtient le shader actuel.
     */
    public ShaderProgram getCurrentShader() {
        return currentShader;
    }
    
    /**
     * Obtient un shader par nom.
     */
    public ShaderProgram getShader(String name) {
        return shaders.get(name);
    }
    
    /**
     * Libère toutes les ressources.
     */
    public void dispose() {
        for (ShaderProgram shader : shaders.values()) {
            shader.dispose();
        }
        shaders.clear();
    }
}

