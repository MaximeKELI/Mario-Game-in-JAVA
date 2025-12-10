package com.mariogame.core;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class MarioGame extends ApplicationAdapter implements InputProcessor {
    // Constantes du jeu
    public static final float PPM = 100f; // Pixels Per Meter
    public static final float V_WIDTH = 16f; // Largeur virtuelle en mètres
    public static final float V_HEIGHT = 9f; // Hauteur virtuelle en mètres
    
    // Bits de collision
    public static final short NOTHING_BIT = 0;
    public static final short GROUND_BIT = 1;
    public static final short PLAYER_BIT = 2;
    public static final short BRICK_BIT = 4;
    public static final short COIN_BIT = 8;
    public static final short DESTROYED_BIT = 16;
    public static final short OBJECT_BIT = 32;
    public static final short ENEMY_BIT = 64;
    public static final short ENEMY_HEAD_BIT = 128;
    public static final short ITEM_BIT = 256;
    public static final short PLAYER_HEAD_BIT = 512;

    // Variables graphiques
    private OrthographicCamera gamecam;
    private Viewport gamePort;
    private SpriteBatch batch;
    private Texture background;
    
    // Variables Box2D
    private World world;
    private Box2DDebugRenderer b2dr;
    private boolean debug = true;
    
    // Joueur
    private Player player;
    
    // Niveau
    private int levelWidth = 100; // Largeur du niveau en mètres
    private int levelHeight = 20; // Hauteur du niveau en mètres
    
    // HUD
    private Hud hud;
    
    // Gestion des entrées
    private boolean moveLeft = false;
    private boolean moveRight = false;
    private boolean jumpPressed = false;
    private boolean runPressed = false;
    
    @Override
    public void create() {
        // Initialisation de la caméra et de la vue
        gamecam = new OrthographicCamera();
        gamePort = new FitViewport(V_WIDTH, V_HEIGHT, gamecam);
        
        // Initialisation du batch
        batch = new SpriteBatch();
        
        // Initialisation de Box2D
        world = new World(new Vector2(0, -30f), true);
        b2dr = new Box2DDebugRenderer();
        
        // Initialisation du HUD
        hud = new Hud(batch);
        
        // Chargement des textures
        background = new Texture("background.png");
        
        // Création du joueur
        player = new Player(world, 2, 5);
        
        // Création du niveau
        createWorld();
        
        // Configuration des entrées
        Gdx.input.setInputProcessor(this);
    }
    
    private void createWorld() {
        // Création du sol principal
        createGround(0, 0, levelWidth, 1);
        
        // Ajout de quelques plateformes
        createPlatform(5, 3, 3, 0.5f);
        createPlatform(12, 5, 2, 0.5f);
        createPlatform(20, 7, 4, 0.5f);
        createPlatform(30, 4, 3, 0.5f);
        
        // Ajout de quelques briques avec pièces
        for (int i = 0; i < 5; i++) {
            createBrick(8 + i * 2, 6);
        }
    }
    
    private void createGround(float x, float y, float width, float height) {
        BodyDef bdef = new BodyDef();
        bdef.position.set(x + width/2, y + height/2);
        bdef.type = BodyType.StaticBody;
        
        Body body = world.createBody(bdef);
        
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width/2, height/2);
        
        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;
        fdef.filter.categoryBits = GROUND_BIT;
        fdef.filter.maskBits = PLAYER_BIT | ENEMY_BIT | OBJECT_BIT;
        
        body.createFixture(fdef).setUserData("ground");
        shape.dispose();
    }
    
    private void createPlatform(float x, float y, float width, float height) {
        createGround(x, y, width, height);
    }
    
    private void createBrick(float x, float y) {
        BodyDef bdef = new BodyDef();
        bdef.position.set(x, y);
        bdef.type = BodyType.StaticBody;
        
        Body body = world.createBody(bdef);
        
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(0.5f, 0.5f);
        
        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;
        fdef.filter.categoryBits = BRICK_BIT;
        fdef.filter.maskBits = PLAYER_BIT | ENEMY_BIT;
        
        body.createFixture(fdef).setUserData("brick");
        shape.dispose();
    }
    
    @Override
    public void render() {
        // Effacer l'écran
        Gdx.gl.glClearColor(0.53f, 0.81f, 0.92f, 1); // Ciel bleu clair
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        // Mise à jour du monde physique
        float deltaTime = Math.min(Gdx.graphics.getDeltaTime(), 0.25f);
        world.step(1/60f, 6, 2);
        
        // Gestion des entrées
        handleInput(deltaTime);
        
        // Mise à jour du joueur
        player.update(deltaTime);
        
        // Mise à jour de la caméra
        updateCamera(deltaTime);
        
        // Début du rendu
        batch.setProjectionMatrix(gamecam.combined);
        batch.begin();
        
        // Dessiner l'arrière-plan
        drawBackground();
        
        // Dessiner le niveau et le joueur
        player.render(batch);
        
        batch.end();
        
        // Afficher le debug de Box2D si activé
        if (debug) {
            b2dr.render(world, gamecam.combined);
        }
        
        // Mise à jour et affichage du HUD
        hud.update(deltaTime);
        hud.render();
        
        // Gestion des collisions
        checkCollisions();
    }
    
    private void handleInput(float deltaTime) {
        // Gestion des déplacements
        if (moveLeft) {
            player.moveLeft();
            if (runPressed) {
                // Le joueur court
                player.runLeft();
            }
        } else if (moveRight) {
            player.moveRight();
            if (runPressed) {
                // Le joueur court
                player.runRight();
            }
        } else {
            player.stop();
        }
        
        // Gestion du saut
        if (jumpPressed) {
            player.jump();
        }
    }
    
    private void updateCamera(float deltaTime) {
        // Suivi du joueur avec la caméra
        float targetX = player.getBody().getPosition().x;
        float targetY = player.getBody().getPosition().y;
        
        // Limiter la caméra aux bords du niveau
        targetX = Math.max(targetX, gamePort.getWorldWidth() / 2);
        targetX = Math.min(targetX, levelWidth - gamePort.getWorldWidth() / 2);
        
        // Centrer verticalement le joueur dans la moitié inférieure de l'écran
        targetY = Math.max(targetY, gamePort.getWorldHeight() / 3);
        targetY = Math.min(targetY, levelHeight - gamePort.getWorldHeight() / 2);
        
        // Lissage du mouvement de la caméra
        float lerp = 0.1f;
        float newX = gamecam.position.x + (targetX - gamecam.position.x) * lerp;
        float newY = gamecam.position.y + (targetY - gamecam.position.y) * lerp;
        
        gamecam.position.set(newX, newY, 0);
        gamecam.update();
    }
    
    private void drawBackground() {
        // Position de la caméra pour le défilement parallaxe
        float x = gamecam.position.x - gamecam.viewportWidth / 2;
        float y = gamecam.position.y - gamecam.viewportHeight / 2;
        
        // Taille de l'arrière-plan (peut être ajustée selon les besoins)
        float width = gamecam.viewportWidth * 1.5f;
        float height = gamecam.viewportHeight * 1.5f;
        
        // Dessiner l'arrière-plan
        batch.draw(background, 
                  x - (width - gamecam.viewportWidth) / 2, 
                  y - (height - gamecam.viewportHeight) / 2,
                  width, height);
    }
    
    private void checkCollisions() {
        // Vérifier les contacts dans le monde
        world.getContactList().forEach(contact -> {
            Fixture fixtureA = contact.getFixtureA();
            Fixture fixtureB = contact.getFixtureB();
            
            Object userDataA = fixtureA.getUserData();
            Object userDataB = fixtureB.getUserData();
            
            // Vérifier les collisions avec le sol
            if ((userDataA == player.getBody().getFixtureList().first() && "ground".equals(userDataB)) ||
                (userDataB == player.getBody().getFixtureList().first() && "ground".equals(userDataA))) {
                player.landed();
            }
            
            // Vérifier les collisions avec les briques
            if ((userDataA == player.getBody().getFixtureList().first() && "brick".equals(userDataB)) ||
                (userDataB == player.getBody().getFixtureList().first() && "brick".equals(userDataA))) {
                // Le joueur a touché une brique par en dessous
                if (isPlayerHittingBelow(fixtureA, fixtureB)) {
                    // Détruire la brique et faire sauter le joueur
                    if (userDataA == player.getBody().getFixtureList().first()) {
                        world.destroyBody(fixtureB.getBody());
                    } else {
                        world.destroyBody(fixtureA.getBody());
                    }
                    player.bounce();
                    player.addScore(100);
                }
            }
        });
    }
    
    private boolean isPlayerHittingBelow(Fixture fixtureA, Fixture fixtureB) {
        // Vérifie si le joueur frappe par en dessous
        if (fixtureA.getUserData() == player.getBody().getFixtureList().first()) {
            return fixtureA.getBody().getLinearVelocity().y < 0;
        } else {
            return fixtureB.getBody().getLinearVelocity().y < 0;
        }
    }
    
    @Override
    public void resize(int width, int height) {
        gamePort.update(width, height);
    }
    
    @Override
    public void dispose() {
        batch.dispose();
        world.dispose();
        b2dr.dispose();
        player.dispose();
        background.dispose();
        hud.dispose();
    }
    
    // Méthodes de l'interface InputProcessor
    @Override
    public boolean keyDown(int keycode) {
        switch (keycode) {
            case Input.Keys.LEFT:
                moveLeft = true;
                break;
            case Input.Keys.RIGHT:
                moveRight = true;
                break;
            case Input.Keys.UP:
            case Input.Keys.SPACE:
            case Input.Keys.Z:
                jumpPressed = true;
                break;
            case Input.Keys.SHIFT_LEFT:
            case Input.Keys.SHIFT_RIGHT:
                runPressed = true;
                break;
            case Input.Keys.ESCAPE:
                Gdx.app.exit();
                break;
            case Input.Keys.F1:
                debug = !debug; // Basculer le mode debug
                break;
        }
        return true;
    }
    
    @Override
    public boolean keyUp(int keycode) {
        switch (keycode) {
            case Input.Keys.LEFT:
                moveLeft = false;
                break;
            case Input.Keys.RIGHT:
                moveRight = false;
                break;
            case Input.Keys.UP:
            case Input.Keys.SPACE:
            case Input.Keys.Z:
                jumpPressed = false;
                break;
            case Input.Keys.SHIFT_LEFT:
            case Input.Keys.SHIFT_RIGHT:
                runPressed = false;
                break;
        }
        return true;
    }
    
    @Override
    public boolean keyTyped(char character) {
        return false;
    }
    
    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }
    
    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }
    
    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }
    
    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }
    
    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }
}
