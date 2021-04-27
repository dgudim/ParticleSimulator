package com.deo.slime;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.util.ArrayList;

import static com.badlogic.gdx.math.MathUtils.clamp;
import static java.lang.Math.floor;
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
    final int numberOfAgents = 100000;
    int currentPalette = 0;
    int currentSimulationRule = 13;

    int frame = 0;
    boolean recording = false;

    OrthographicCamera camera;
    ScreenViewport viewport;

    final boolean randomSpawn = false;

    @Override
    public void create() {

        camera = new OrthographicCamera(WIDTH, HEIGHT);
        viewport = new ScreenViewport(camera);

        agents = new Agent[numberOfAgents];
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

        changeSimulationRules();
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);

        if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
            recording = !recording;
            frame = 0;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            for (Agent agent : agents) {
                agent.x = WIDTH / 2f;
                agent.y = HEIGHT / 2f;
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.H)) {
            double rand = random();
            double rand1 = random();
            double rand2 = random();
            double rand3 = random();
            double rand4 = random();
            for (Agent agent : agents) {
                agent.speed = (float) (rand * 10 - 5);
                agent.turnSpeed = (float) (rand1 * 160 - 80);
                agent.sensorLength = (int) (rand2 * 40 - 20);
                agent.sensorLengthOffset = (int) (rand3 * 60 - 30);
                agent.sensorAngleOffset = (int) (rand4 * 160 - 80);
                System.out.println(agent.speed + ", " + agent.turnSpeed + ", " + agent.sensorLength + ", " + agent.sensorLengthOffset + ", " + agent.sensorAngleOffset);
            }
        }

        if (Gdx.input.isKeyPressed(Input.Keys.F10)){
            boolean fullScreen = Gdx.graphics.isFullscreen();
            Graphics.DisplayMode currentMode = Gdx.graphics.getDisplayMode();
            if (fullScreen)
                Gdx.graphics.setWindowedMode(currentMode.width, currentMode.height);
            else
                Gdx.graphics.setFullscreenMode(currentMode);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            currentSimulationRule++;
            changeSimulationRules();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            currentSimulationRule--;
            changeSimulationRules();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.C)) {
            currentPalette++;
        }

        batch.begin();
        gridTexture.draw(gridPixmap, 0, 0);
        batch.draw(gridTexture, 0, 0);
        batch.end();
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (Agent agent : agents) {
                    agent.update(gridPixmapColors);
                }
            }
        }).start();
        fade();
        copyOver();
        if (recording) {
            makeAScreenShot(frame);
            frame++;
        }
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
                gridPixmapColors[x][y] = clamp(gridPixmapColors[x][y] - 0.035f, 0, 1);
            }
        }
    }

    void copyOver() {
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {

                switch (currentPalette) {
                    case (0):
                        float ratio1 = (gridPixmapColors[x][y] - 0.5f) * 2;
                        float ratio2 = (ratio1 - gridPixmapColors[x][y]) * 2;
                        Color c1 = new Color(mixTwoColors(Color.SLATE, Color.WHITE, ratio1));
                        gridPixmap.drawPixel(x, y, mixTwoColors(c1, Color.CORAL, ratio2));
                        break;
                    case (1):
                        gridPixmap.drawPixel(x, y, interpolate(gridPixmapColors[x][y], Color.BLACK, Color.ORANGE, Color.CYAN, Color.CORAL));
                        break;
                    case (2):
                        gridPixmap.drawPixel(x, y, interpolate(gridPixmapColors[x][y], Color.BLACK, Color.ORANGE, Color.RED, Color.GRAY));
                        break;
                    case (3):
                        gridPixmap.drawPixel(x, y, interpolate(gridPixmapColors[x][y], Color.BLACK, Color.LIME, Color.TEAL, Color.CLEAR));
                        break;
                    case (4):
                        gridPixmap.drawPixel(x, y,
                                mixTwoColors(
                                        new Color(
                                                interpolate(
                                                        gridPixmapColors[x][y],
                                                        Color.BLACK, Color.ROYAL,
                                                        Color.ROYAL, Color.CYAN,
                                                        Color.WHITE)),
                                        new Color(mixTwoColors(
                                                Color.LIGHT_GRAY,
                                                Color.BLACK, 1 - gridPixmapColors[x][y])), gridPixmapColors[x][y]));
                        break;
                    case (5):
                        gridPixmap.drawPixel(x, y, interpolate(gridPixmapColors[x][y], Color.BLACK, Color.SKY, Color.BLACK, Color.BLACK, Color.TEAL, Color.BLACK));
                        break;
                    default:
                        currentPalette = 0;
                        break;
                }
            }
        }
    }

    void changeSimulationRules() {
        switch (currentSimulationRule) {
            case (0):
                changeParamsForAllAgents(2f, 50, 8, 2, 50);
                break;
            case (1):
                changeParamsForAllAgents(2f, 50, 7, 2, 40);
                break;
            case (2):
                changeParamsForAllAgents(2f, 50, 9, 19, 50);
                break;
            case (3):
                changeParamsForAllAgents(3f, 92, 10, 102, 67);
                break;
            case (4):
                changeParamsForAllAgents(3f, 9, 10, 12, 67);
                break;
            case (5):
                changeParamsForAllAgents(1f, 9, 10, 12, 9);
                break;
            case (6):
                changeParamsForAllAgents(3f, 10, 30, 5, 45);
                break;
            case (7):
                changeParamsForAllAgents(3f, 10, 13, 15, -45);
                break;
            case (8):
                changeParamsForAllAgents(-3f, 14, 10, 15, -45);
                break;
            case (9):
                changeParamsForAllAgents(3f, 25, 17, 13, 15);
                break;
            case (10):
                changeParamsForAllAgents(3f, 50, 30, 13, 15);
                break;
            case (11):
                changeParamsForAllAgents(3f, 50, 12, 13, -15);
                break;
            case (12):
                changeParamsForAllAgents(-3f, -20, 12, -13, 15);
                break;
            case (13):
                changeParamsForAllAgents(3, 45, 3, 3, 3);
                break;
            case (14):
                changeParamsForAllAgents(3, 30, 3, 3, 45);
                break;
            default:
                currentSimulationRule = 0;
                changeSimulationRules();
                break;
        }
    }

    void changeParamsForAllAgents(float speed,
                                  float turnSpeed,
                                  int sensorLength,
                                  int sensorLengthOffset,
                                  int sensorAngleOffset) {
        for (Agent agent : agents) {
            agent.speed = speed;
            agent.turnSpeed = turnSpeed;
            agent.sensorLength = sensorLength;
            agent.sensorLengthOffset = sensorLengthOffset;
            agent.sensorAngleOffset = sensorAngleOffset;
        }
    }

    public int interpolate(float step, Color... colors) {
        step = Math.max(Math.min(step, 1.0f), 0.0f);

        switch (colors.length) {
            case 0:
                throw new IllegalArgumentException("At least one color required.");

            case 1:
                return Color.argb8888(colors[0]);

            case 2:
                return mixTwoColors(colors[0], colors[1], step);

            default:

                int firstColorIndex = (int) (step * (colors.length - 1));

                if (firstColorIndex == colors.length - 1) {
                    return Color.argb8888(colors[colors.length - 1]);
                }

                // stepAtFirstColorIndex will be a bit smaller than step
                float stepAtFirstColorIndex = (float) firstColorIndex
                        / (colors.length - 1);

                // multiply to increase values to range between 0.0f and 1.0f
                float localStep = (step - stepAtFirstColorIndex)
                        * (colors.length - 1);

                return mixTwoColors(colors[firstColorIndex],
                        colors[firstColorIndex + 1], localStep);
        }

    }

    int mixTwoColors(Color color1, Color color2, float ratio) {
        return Color.rgba8888(color1.r * (1f - ratio) + color2.r * ratio, color1.g * (1f - ratio) + color2.g * ratio, color1.b * (1f - ratio) + color2.b * ratio, 1f);
    }

    void makeAScreenShot(int recorderFrame) {

        byte[] pixels = ScreenUtils.getFrameBufferPixels(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight(), true);

        for (int i4 = 4; i4 < pixels.length; i4 += 4) {
            pixels[i4 - 1] = (byte) 255;
        }

        FileHandle file;
        file = Gdx.files.external("SlimeRender/pict" + recorderFrame + ".png");

        Pixmap pixmap = new Pixmap(Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight(), Pixmap.Format.RGBA8888);
        BufferUtils.copy(pixels, 0, pixmap.getPixels(), pixels.length);
        PixmapIO.writePNG(file, pixmap);
        pixmap.dispose();
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
