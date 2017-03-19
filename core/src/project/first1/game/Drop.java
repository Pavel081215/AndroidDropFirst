package project.first1.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Iterator;

public class Drop extends ApplicationAdapter {
    OrthographicCamera camera;
    SpriteBatch batch;
    Texture dropImage;
    Texture bucketImage;
    Sound dropSound;
    Music rainMusic;
    Rectangle bucket;
    Vector3 touchPos;
    Array<Rectangle> raindrops; // для хранения капель Array - аналог JAVA колекций только плодит меньше мусора
    long lastDropTime;  // последнее появление капли, храним время

    @Override
    public void create() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);
        batch = new SpriteBatch();
        dropImage = new Texture("droplet.png");
        bucketImage = new Texture("bucket.png");
        dropSound = Gdx.audio.newSound(Gdx.files.internal("waterdrop.wav"));
        rainMusic = Gdx.audio.newMusic(Gdx.files.internal("undertreeinrain.mp3"));
        touchPos = new Vector3();


        rainMusic.setLooping(true);
        rainMusic.play();

        bucket = new Rectangle();
        bucket.x = 800 / 2 - 64 / 2;
        bucket.y = 64;
        bucket.width = 64;
        bucket.height = 64;

        // капли

        raindrops = new Array<Rectangle>();
        spawnRaindrops();

    }

    // создаем дождь .. spawn - порождать
    public void spawnRaindrops() {
        Rectangle rainDrop = new Rectangle();
        rainDrop.x = MathUtils.random(0, 800 - 64);
        rainDrop.y = 480;
        rainDrop.width = 64;
        rainDrop.height = 64;
        raindrops.add(rainDrop);
        // записываем время в нано секундах  следуя из того надо порождать новую каплю или нет
        lastDropTime = TimeUtils.nanoTime();


    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0.2f, 1);   // установит цвет очистки в синий цвет
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);  // очистить экран

        camera.update(); // обновить камеру хорошая практика обновлять 1 раз за кадр

        batch.setProjectionMatrix(camera.combined); // начинаем рисовать ведро и собщаем SpriteBatch batch что рисуем в системе координат camera = поле camera.combined - являеться матрицев в которой рисуеться
        batch.begin(); // начинаем новую batch-серии. SpriteBatch - класс который все рисует
        batch.draw(bucketImage, bucket.x, bucket.y);
        for (Rectangle raindrop: raindrops){
            batch.draw(dropImage, raindrop.x, raindrop.y);
        }
        batch.end();// конец серии

        if (Gdx.input.isTouched()) {   // опрашиваем модуль ввода есть ли прикосновение к экрану или нажатие на клавишу
            // преоброзование координат мыши или кнопок в систему координт camera
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos); // для преобразование наших координат в координаты camera - вызываем  метод unproject в который передаеться 3-х мерный вектор
            bucket.x = touchPos.x - 64 / 2; // передаем новые координаты

            // двигаем ведро вправо и влево

            // (скорость 200 пикселей в секунду)
            // Input.Keys.LEFT - содержит все коды клавишь которые поддерживает LibGDX (фреймверк)
            if (Gdx.input.isKeyPressed(Input.Keys.LEFT))
                bucket.x -= 200 * Gdx.graphics.getDeltaTime(); //  расщитываем куда и  на сколько мы должны сдвинуться. Метод  isKeyPressed сообщает о нажатитт определенной клавиши , getDeltaTime - возвращает время между последним кадром и передыдущим
            if (Gdx.input.isKeyPressed(Input.Keys.RIGHT))
                bucket.x += 200 * Gdx.graphics.getDeltaTime();

            // проверка чтобы ведро было в пределах экрана
            if (bucket.x < 0) bucket.x = 0;
            if (bucket.x > 800 - 64) bucket.x = 800 - 64;

            //движение капли
            if (TimeUtils.nanoTime() - lastDropTime > 1000000000) spawnRaindrops();
            Iterator<Rectangle> iter = raindrops.iterator();
            while (iter.hasNext()) {
                Rectangle raindrop = iter.next();
                raindrop.y -= 200 * Gdx.graphics.getDeltaTime();
                if (raindrop.y + 64 < 0) iter.remove();
                if (raindrop.overlaps(bucket)) {
                    dropSound.play();
                    iter.remove();
                }
            }

        }

    }

    //очистка памяти уничтожение всех ресурсов
    @Override
    public void dispose() {
        super.dispose();
        dropImage.dispose();
        bucketImage.dispose();
        dropSound.dispose();
        rainMusic.dispose();
        batch.dispose();

    }
}
