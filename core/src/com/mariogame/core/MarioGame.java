package com.mariogame.core;

import static com.mariogame.core.GameConfig.*;

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
    // Configuration du jeu

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
        
        // Initialisation de Box2D avec la configuration
        world = new World(new Vector2(0, World.GRAVITY), true);
        b2dr = new Box2DDebugRenderer(
            true,  // dessiner les formes
            true,  // dessiner les joints
            true,  // dessiner les aabbs
            true,  // dessiner les points de masse
            false, // dessiner les articulations
            true   // dessiner les centres de masse
        );
        
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
        
        // Ajout de plateformes de test avec la configuration
        float[] platforms = {
            // x, y, width, height
            5, 3, 3, 0.5f,
            12, 5, 2, 0.5f,
            20, 7, 4, 0.5f,
            30, 4, 3, 0.5f
        };
        
        for (int i = 0; i < platforms.length; i += 4) {
            createPlatform(
                platforms[i], 
                platforms[i + 1], 
                platforms[i + 2], 
                platforms[i + 3]
            );
        }
        
        // Ajout de briques avec pièces
        for (int i = 0; i < 5; i++) {
            createBrick(8 + i * 2, 6);
        }
    }
    
    private void createGround(float x, float y, float width, float height) {
        // Création du corps statique
        BodyDef bdef = new BodyDef();
        bdef.position.set(x + width/2, y + height/2);
        bdef.type = BodyType.StaticBody;
        
        // Création de la forme de collision
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width/2, height/2);
        
        // Configuration du fixture
        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;
        fdef.friction = Player.FRICTION;
        fdef.restitution = Player.RESTITUTION;
        fdef.density = Player.DENSITY;
        
        // Configuration des filtres de collision
        Filter filter = new Filter();
        filter.categoryBits = CollisionLayers.GROUND_BIT;
        filter.maskBits = CollisionLayers.GROUND_MASK;
        
        // Création du corps et application du fixture
        Body body = world.createBody(bdef);
        Fixture fixture = body.createFixture(fdef);
        fixture.setFilterData(filter);
        fixture.setUserData("ground");
        
        // Nettoyage
        shape.dispose();
    }
    
    private void createPlatform(float x, float y, float width, float height) {
        createGround(x, y, width, height);
    }
    
    private void createBrick(float x, float y) {
        // Création du corps statique pour la brique
        BodyDef bdef = new BodyDef();
        bdef.position.set(x, y);
        bdef.type = BodyType.StaticBody;
        
        // Création de la forme de collision
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(0.5f, 0.5f);
        
        // Configuration du fixture
        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;
        fdef.friction = Player.FRICTION;
        fdef.restitution = Player.RESTITUTION;
        fdef.density = Player.DENSITY;
        
        // Configuration des filtres de collision
        Filter filter = new Filter();
        filter.categoryBits = CollisionLayers.BRICK_BIT;
        filter.maskBits = CollisionLayers.PLAYER_BIT | CollisionLayers.ENEMY_BIT;
        
        // Création du corps et application du fixture
        Body body = world.createBody(bdef);
        Fixture fixture = body.createFixture(fdef);
        fixture.setFilterData(filter);
        fixture.setUserData("brick");
        
        // Nettoyage
        shape.dispose();
    }
    
    @Override
    public void render() {
        // Effacer l'écran
        Gdx.gl.glClearColor(0.53f, 0.81f, 0.92f, 1); // Ciel bleu clair
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        // Mise à jour du monde physique avec la configuration
        float deltaTime = Math.min(Gdx.graphics.getDeltaTime(), 0.25f);
        world.step(
            World.TIME_STEP, 
            World.VELOCITY_ITERATIONS, 
            World.POSITION_ITERATIONS
        );
        
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
        if (player == null) return;
        
        // Gestion des déplacements horizontaux
        float targetVelocityX = 0;
        
        if (moveLeft) {
            targetVelocityX = -Player.MAX_SPEED;
            player.setFacingRight(false);
        } else if (moveRight) {
            targetVelocityX = Player.MAX_SPEED;
            player.setFacingRight(true);
        }
        
        // Application de la vitesse avec accélération et décélération
        Vector2 velocity = player.getBody().getLinearVelocity();
        float velocityChangeX = targetVelocityX - velocity.x;
        float impulseX = player.getBody().getMass() * velocityChangeX * Player.ACCELERATION * deltaTime;
        
        // Limiter l'impulsion pour éviter des accélérations trop brutales
        impulseX = Math.signum(impulseX) * Math.min(Math.abs(impulseX), Math.abs(velocityChangeX) * player.getBody().getMass());
        
        player.getBody().applyLinearImpulse(
            new Vector2(impulseX, 0), 
            player.getBody().getWorldCenter(), 
            true
        );
        
        // Gestion du saut
        if (jumpPressed) {
            player.jump();
        }
        
        // Gestion de l'accroupissement
        player.setCrouching(isCrouching);
    }
    
    private void updateCamera(float deltaTime) {
        if (player == null) return;
        
        // Position cible de la caméra (centrée sur le joueur)
        Vector2 target = new Vector2(
            player.getBody().getPosition().x,
            player.getBody().getPosition().y
        );
        
        // Limites de la caméra
        float viewportWidth = gamePort.getWorldWidth() * gamecam.zoom;
        float viewportHeight = gamePort.getWorldHeight() * gamecam.zoom;
        
        // Limiter la position X de la caméra
        target.x = Math.max(viewportWidth / 2, Math.min(levelWidth - viewportWidth / 2, target.x));
        
        // Centrer verticalement dans la moitié inférieure de l'écran
        target.y = Math.max(viewportHeight / 3, Math.min(levelHeight - viewportHeight / 2, target.y));
        
        // Lissage du mouvement de la caméra
        Vector2 position = new Vector2(gamecam.position.x, gamecam.position.y);
        position.lerp(target, Camera.FOLLOW_SPEED * deltaTime * 60f);
        
        // Mise à jour de la position de la caméra
        gamecam.position.set(position.x, position.y, 0);
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
        // Mise à jour de la vue avec le nouveau format d'écran
        gamePort.update(width, height, true);
        
        // Ajuster le zoom de la caméra si nécessaire
        float aspectRatio = (float)width / (float)height;
        float targetWidth = World.V_WIDTH;
        float targetHeight = targetWidth / aspectRatio;
        
        gamecam.viewportWidth = targetWidth;
        gamecam.viewportHeight = targetHeight;
        
        // Forcer la mise à jour de la caméra
        gamecam.update();
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
        // Gestion des touches selon la configuration
        if (keycode == Keys.MOVE_LEFT) moveLeft = true;
        if (keycode == Keys.MOVE_RIGHT) moveRight = true;
        if (keycode == Keys.JUMP || keycode == Keys.JUMP_ALT || keycode == Keys.JUMP_ALT2) jumpPressed = true;
        if (keycode == Keys.RUN || keycode == Keys.RUN_ALT) runPressed = true;
        if (keycode == Keys.CROUCH) isCrouching = true;
        
        // Touches système
        if (keycode == Keys.DEBUG) debug = !debug;
        if (keycode == Keys.PAUSE) Gdx.app.exit();
        
        return true;
    }
    
    @Override
    public boolean keyUp(int keycode) {
        // Gestion du relâchement des touches
        if (keycode == Keys.MOVE_LEFT) moveLeft = false;
        if (keycode == Keys.MOVE_RIGHT) moveRight = false;
        if (keycode == Keys.JUMP || keycode == Keys.JUMP_ALT || keycode == Keys.JUMP_ALT2) jumpPressed = false;
        if (keycode == Keys.RUN || keycode == Keys.RUN_ALT) runPressed = false;
        if (keycode == Keys.CROUCH) isCrouching = false;
        
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
