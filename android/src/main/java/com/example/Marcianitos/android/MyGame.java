package com.example.Marcianitos.android;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import java.util.Iterator;

public class MyGame extends ApplicationAdapter {
    SpriteBatch batch;
    Texture spaceshipTexture, bulletTexture, alienTexture;
    Spaceship spaceship;
    Array<Bullet> bullets;
    Array<Alien> aliens;

    // Variables para disparo automático
    float timeSinceLastShot = 0;
    float shootInterval = 0.5f; // dispara cada 0.5 segundos

    // Variables para generar marcianos
    float alienSpawnTimer = 0;
    float alienSpawnInterval = 2.0f; // genera un marciano cada 2 segundos

    boolean gameOver = false;

    @Override
    public void create() {
        batch = new SpriteBatch();
        spaceshipTexture = new Texture("PNG/Sprites/Ships/spaceShips_001.png");
        bulletTexture = new Texture("PNG/Sprites/Missiles/spaceMissiles_001.png");
        alienTexture = new Texture("PNG/Sprites/Meteors/spaceMeteors_001.png");

        // Inicia la nave en el centro, a 50 píxeles del borde inferior
        spaceship = new Spaceship(spaceshipTexture, Gdx.graphics.getWidth() / 2, 50);
        bullets = new Array<Bullet>();
        aliens = new Array<Alien>();
    }

    @Override
    public void render() {
        float deltaTime = Gdx.graphics.getDeltaTime();
        update(deltaTime);

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        spaceship.draw(batch);
        for (Bullet bullet : bullets) {
            bullet.draw(batch);
        }
        for (Alien alien : aliens) {
            alien.draw(batch);
        }
        batch.end();
    }

    public void update(float deltaTime) {
        if (gameOver) return;

        // Movimiento de la nave y disparo automático cuando la pantalla está tocada
        if (Gdx.input.isTouched()) {
            // Convertir la posición de la pantalla a coordenadas del juego
            Vector3 touchPos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            touchPos.y = Gdx.graphics.getHeight() - touchPos.y;
            spaceship.updatePosition(touchPos.x);

            timeSinceLastShot += deltaTime;
            if (timeSinceLastShot >= shootInterval) {
                Bullet bullet = new Bullet(bulletTexture,
                    spaceship.getX() + spaceship.getWidth() / 2,
                    spaceship.getY() + spaceship.getHeight());
                bullets.add(bullet);
                timeSinceLastShot = 0;
            }
        } else {
            // Si se suelta la pantalla, reiniciamos el contador para disparar inmediatamente al tocar de nuevo
            timeSinceLastShot = shootInterval;
        }

        // Actualizar la posición de los disparos
        Iterator<Bullet> bulletIter = bullets.iterator();
        while (bulletIter.hasNext()) {
            Bullet bullet = bulletIter.next();
            bullet.update(deltaTime);
            if (bullet.getY() > Gdx.graphics.getHeight()) {
                bulletIter.remove();
            }
        }

        // Generar marcianos en la parte superior
        alienSpawnTimer += deltaTime;
        if (alienSpawnTimer >= alienSpawnInterval) {
            alienSpawnTimer = 0;
            float xPos = MathUtils.random(0, Gdx.graphics.getWidth() - alienTexture.getWidth());
            Alien alien = new Alien(alienTexture, xPos, Gdx.graphics.getHeight());
            aliens.add(alien);
        }

        // Actualizar la posición de los marcianos
        Iterator<Alien> alienIter = aliens.iterator();
        while (alienIter.hasNext()) {
            Alien alien = alienIter.next();
            alien.update(deltaTime);
            // Si un marciano llega al fondo, el juego termina
            if (alien.getY() + alien.getHeight() < 0) {
                gameOver = true;
                System.out.println("Game Over: un marciano llegó al fondo.");
            }
        }

        // Detección de colisiones entre la nave y los marcianos
        for (Alien alien : aliens) {
            if (alien.getBoundingRectangle().overlaps(spaceship.getBoundingRectangle())) {
                gameOver = true;
                System.out.println("Game Over: la nave chocó con un marciano.");
            }
        }

        // Detección de colisiones entre disparos y marcianos
        Iterator<Bullet> bIter = bullets.iterator();
        while (bIter.hasNext()) {
            Bullet bullet = bIter.next();
            Iterator<Alien> aIter = aliens.iterator();
            while (aIter.hasNext()) {
                Alien alien = aIter.next();
                if (bullet.getBoundingRectangle().overlaps(alien.getBoundingRectangle())) {
                    bIter.remove();
                    aIter.remove();
                    break;
                }
            }
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        spaceshipTexture.dispose();
        bulletTexture.dispose();
        alienTexture.dispose();
    }

    // Clase para la nave
    public class Spaceship extends Sprite {
        public Spaceship(Texture texture, float x, float y) {
            super(texture);
            setPosition(x - getWidth() / 2, y);
        }
        // Actualiza la posición horizontal de la nave según el toque
        public void updatePosition(float x) {
            setX(x - getWidth() / 2);
        }
    }

    // Clase para los disparos
    public class Bullet extends Sprite {
        float speed = 300; // velocidad en píxeles por segundo
        public Bullet(Texture texture, float x, float y) {
            super(texture);
            setPosition(x - getWidth() / 2, y);
        }
        public void update(float deltaTime) {
            setY(getY() + speed * deltaTime);
        }
    }

    // Clase para los marcianos
    public class Alien extends Sprite {
        float speed = 100; // velocidad en píxeles por segundo
        public Alien(Texture texture, float x, float y) {
            super(texture);
            setPosition(x, y);
        }
        public void update(float deltaTime) {
            setY(getY() - speed * deltaTime);
        }
    }
}
