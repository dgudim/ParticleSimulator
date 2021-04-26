package com.deo.slime;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import static com.badlogic.gdx.math.MathUtils.clamp;
import static java.lang.Math.min;
import static java.lang.Math.random;

public class Main extends ApplicationAdapter {

    public static final int WIDTH = 1920;
    public static final int HEIGHT = 1080;

    SpriteBatch batch;
    Agent[] agents;

    Pixmap gridPixmap;
    volatile float[][] gridPixmapColors;
    Texture gridTexture;

    OrthographicCamera camera;
    ScreenViewport viewport;

    final boolean randomSpawn = false;

    @Override
    public void create() {

        camera = new OrthographicCamera(WIDTH, HEIGHT);
        viewport = new ScreenViewport(camera);

        agents = new Agent[1000000];
        for (int i = 0; i < agents.length; i++) {
            Agent newAgent = new Agent();
            if (randomSpawn) {
                newAgent.x = (float) (WIDTH * random());
                newAgent.y = (float) (HEIGHT * random());
            } else {
                newAgent.x = WIDTH / 2f;
                newAgent.y = HEIGHT / 2f;
            }
            newAgent.rotation = (float) (random() * 360);
            agents[i] = newAgent;
        }
        batch = new SpriteBatch();
        gridPixmap = new Pixmap(WIDTH, HEIGHT, Format.RGBA8888);
        gridPixmapColors = new float[WIDTH][HEIGHT];
        gridTexture = new Texture(gridPixmap);
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        gridTexture.draw(gridPixmap, 0, 0);
        batch.draw(gridTexture, 0, 0);
        batch.end();
        new Thread(new Runnable() {
            @Override
            public void run() {
                fade();
                for (Agent agent : agents) {
                    agent.update(gridPixmapColors);
                }
            }
        }).start();
        copyOver();
    }

    void fade() {

        for (int x = 1; x < WIDTH - 1; x++) {
            for (int y = 1; y < HEIGHT - 1; y++) {

                float sum = 0;

                sum += gridPixmapColors[x][y];

                sum += gridPixmapColors[x - 1][y];
                sum += gridPixmapColors[x + 1][y];

                sum += gridPixmapColors[x - 1][y - 1];
                sum += gridPixmapColors[x + 1][y + 1];

                sum += gridPixmapColors[x][y + 1];
                sum += gridPixmapColors[x][y - 1];

                sum += gridPixmapColors[x - 1][y + 1];
                sum += gridPixmapColors[x + 1][y - 1];

                sum /= 9f;

                gridPixmapColors[x][y] = clamp(sum, 0, 1);
            }
        }

        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                gridPixmapColors[x][y] = clamp(gridPixmapColors[x][y] - 0.05f, 0, 1);
            }
        }
    }

    void copyOver() {
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                float ratio1 = (gridPixmapColors[x][y] - 0.5f) * 2;
                float ratio2 = (ratio1 - gridPixmapColors[x][y]) * 2;
                Color c1 = new Color(mixTwoColors(Color.SLATE, Color.WHITE, ratio1));
                gridPixmap.drawPixel(x, y, mixTwoColors(c1, Color.CORAL, ratio2));
            }
        }
    }

    int mixTwoColors(Color color1, Color color2, float ratio) {
        return Color.rgba8888(color1.r * (1 - ratio) + color2.r * ratio, color1.g * (1 - ratio) + color2.g * ratio, color1.b * (1 - ratio) + color2.b * ratio, 1);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        camera.position.set(WIDTH / 2f, HEIGHT / 2f, 0);
        float tempScaleH = height / (float) HEIGHT;
        float tempScaleW = width / (float) WIDTH;
        float zoom = min(tempScaleH, tempScaleW);
        camera.zoom = 1 / zoom;
        camera.update();
    }

    @Override
    public void dispose() {
        batch.dispose();
        gridTexture.dispose();
    }
}
