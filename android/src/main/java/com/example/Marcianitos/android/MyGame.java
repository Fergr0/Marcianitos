package com.example.Marcianitos.android;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound; // Importamos Sound
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;

import java.util.Iterator;

public class MyGame extends ApplicationAdapter {
    SpriteBatch batch;
    Texture spaceshipTexture, bulletTexture, alienTexture, backgroundTexture;
    Spaceship spaceship;
    Array<Bullet> bullets;
    Array<Alien> aliens;
    BitmapFont font;
    int score = 0;

    float timeSinceLastShot = 0;
    float shootInterval = 0.3f;
    float alienSpawnTimer = 0;
    float alienSpawnInterval = 1.5f;
    float meteorSpeed = 100;

    boolean gameOver = false;

    Stage stage;
    TextButton restartButton;

    ParticleEffect explosionEffect;
    Sound explosionSound; // Sonido de explosión

    float backgroundY = 0;

    @Override
    public void create() {
        batch = new SpriteBatch();
        backgroundTexture = new Texture("PNG/spaceBackground.png");
        spaceshipTexture = new Texture("PNG/Sprites/Ships/spaceShips_001.png");
        bulletTexture = new Texture("PNG/Sprites/Missiles/spaceMissiles_001.png");
        alienTexture = new Texture("PNG/Sprites/Meteors/spaceMeteors_001.png");

        spaceship = new Spaceship(spaceshipTexture, Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
        bullets = new Array<>();
        aliens = new Array<>();
        font = new BitmapFont();

        explosionEffect = new ParticleEffect();
        explosionEffect.load(Gdx.files.internal("PNG/explosion.p"), Gdx.files.internal("PNG/"));

        explosionSound = Gdx.audio.newSound(Gdx.files.internal("sounds/explosion.mp3")); // Cargar sonido

        stage = new Stage();
        Gdx.input.setInputProcessor(stage);
        TextButtonStyle buttonStyle = new TextButtonStyle();
        buttonStyle.font = font;
        restartButton = new TextButton("Reiniciar", buttonStyle);
        restartButton.setPosition(Gdx.graphics.getWidth() / 2 - 50, Gdx.graphics.getHeight() / 2);
        restartButton.setSize(200, 80);
        restartButton.setVisible(false);

        restartButton.addListener(new ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                resetGame();
            }
        });

        stage.addActor(restartButton);
    }

    @Override
    public void render() {
        float deltaTime = Gdx.graphics.getDeltaTime();
        update(deltaTime);

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        batch.draw(backgroundTexture, 0, backgroundY, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.draw(backgroundTexture, 0, backgroundY + Gdx.graphics.getHeight(), Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        spaceship.draw(batch);
        for (Bullet bullet : bullets) {
            bullet.draw(batch);
        }
        for (Alien alien : aliens) {
            alien.draw(batch);
        }

        font.draw(batch, "Puntos: " + score, 20, Gdx.graphics.getHeight() - 20);

        explosionEffect.draw(batch, deltaTime);

        batch.end();

        stage.act();
        stage.draw();
    }

    public void update(float deltaTime) {
        if (gameOver) return;

        float backgroundSpeed = 300;
        backgroundY -= backgroundSpeed * deltaTime;
        if (backgroundY <= -Gdx.graphics.getHeight()) {
            backgroundY = 0;
        }

        // 🚀 Movimiento de la nave en TODA la pantalla (X e Y)
        if (Gdx.input.isTouched()) {
            Vector3 touchPos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            touchPos.y = Gdx.graphics.getHeight() - touchPos.y;
            spaceship.updatePosition(touchPos.x, touchPos.y);

            timeSinceLastShot += deltaTime;
            if (timeSinceLastShot >= shootInterval) {
                bullets.add(new Bullet(bulletTexture, spaceship.getX() + spaceship.getWidth() / 2, spaceship.getY() + spaceship.getHeight()));
                timeSinceLastShot = 0;
            }
        }

        Iterator<Bullet> bulletIter = bullets.iterator();
        while (bulletIter.hasNext()) {
            Bullet bullet = bulletIter.next();
            bullet.update(deltaTime);
            if (bullet.getY() > Gdx.graphics.getHeight()) {
                bulletIter.remove();
            }
        }

        alienSpawnTimer += deltaTime;
        if (alienSpawnTimer >= alienSpawnInterval) {
            alienSpawnTimer = 0;
            float xPos = MathUtils.random(0, Gdx.graphics.getWidth() - alienTexture.getWidth());
            aliens.add(new Alien(alienTexture, xPos, Gdx.graphics.getHeight(), meteorSpeed));
            meteorSpeed += 5;
        }

        Iterator<Alien> alienIter = aliens.iterator();
        while (alienIter.hasNext()) {
            Alien alien = alienIter.next();
            alien.update(deltaTime);

            if (alien.getY() + alien.getHeight() < 0) {
                gameOver = true;
                restartButton.setVisible(true);
                break;
            }

            // 🚨 **Colisión con la nave: Fin del juego**
            if (alien.getBoundingRectangle().overlaps(spaceship.getBoundingRectangle())) {
                gameOver = true;
                restartButton.setVisible(true);
                break;
            }
        }

        bulletIter = bullets.iterator();
        while (bulletIter.hasNext()) {
            Bullet bullet = bulletIter.next();
            alienIter = aliens.iterator();
            while (alienIter.hasNext()) {
                Alien alien = alienIter.next();
                if (bullet.getBoundingRectangle().overlaps(alien.getBoundingRectangle())) {
                    explosionEffect.setPosition(alien.getX(), alien.getY());
                    explosionEffect.start();
                    explosionSound.play(); // 🔊💥 Sonido de explosión
                    bulletIter.remove();
                    alienIter.remove();
                    score += 50;
                    break;
                }
            }
        }
    }

    private void resetGame() {
        gameOver = false;
        score = 0;
        bullets.clear();
        aliens.clear();
        meteorSpeed = 100;
        spaceship.setPosition(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
        restartButton.setVisible(false);
    }

    @Override
    public void dispose() {
        batch.dispose();
        spaceshipTexture.dispose();
        bulletTexture.dispose();
        alienTexture.dispose();
        backgroundTexture.dispose();
        font.dispose();
        explosionEffect.dispose();
        stage.dispose();
        explosionSound.dispose(); // 🔊 Liberamos el sonido
    }

    // Clases faltantes corregidas 🚀
    public class Spaceship extends Sprite {
        public Spaceship(Texture texture, float x, float y) {
            super(texture);
            setPosition(x - getWidth() / 2, y - getHeight() / 2);
        }
        public void updatePosition(float x, float y) {
            setPosition(x - getWidth() / 2, y - getHeight() / 2);
        }
    }

    public class Bullet extends Sprite {
        float speed = 300;
        public Bullet(Texture texture, float x, float y) {
            super(texture);
            setPosition(x - getWidth() / 2, y);
        }
        public void update(float deltaTime) {
            setY(getY() + speed * deltaTime);
        }
    }

    public class Alien extends Sprite {
        float speed;
        public Alien(Texture texture, float x, float y, float speed) {
            super(texture);
            setPosition(x, y);
            this.speed = speed;
        }
        public void update(float deltaTime) {
            setY(getY() - speed * deltaTime);
        }
    }
}
