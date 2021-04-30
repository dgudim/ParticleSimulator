package com.deo.slime;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.deo.slime.postprocessing.PostProcessor;
import com.deo.slime.postprocessing.effects.Bloom;

import static com.deo.slime.Launcher.HEIGHT;
import static com.deo.slime.Launcher.WIDTH;
import static com.deo.slime.Utils.interpolate;
import static com.deo.slime.Utils.makeAScreenShot;
import static com.deo.slime.Utils.mixTwoColors;
import static java.lang.Math.log10;
import static java.lang.Math.max;
import static java.lang.Math.pow;
import static java.lang.Math.random;

public class SimulationScreen extends GenericScreen {

    Agent[] agents;

    Pixmap gridPixmap;
    static volatile float[][] gridPixmapColors;
    Texture gridTexture;
    final int numberOfAgents = 100000;
    int currentPalette = 0;
    int currentSimulationRule = 13;

    static float mapDivider = 1;

    static int trailMapHeight = (int) (HEIGHT / mapDivider);
    static int trailMapWidth = (int) (WIDTH / mapDivider);

    int frame = 0;
    boolean recording = false;
    boolean bloom = true;
    boolean showAgents = false;

    final boolean randomSpawn = true;
    float evaporationRate = 0.3f;

    PostProcessor blurProcessor;

    final float[][] availableRules = new float[][]{
            {2f, 50, 7, 2, 40, 0, 1, 1, 0.035f},
            {2f, 50, 9, 19, 50, 0, 1, 1, 0.035f},
            {3f, 92, 10, 102, 67, 0, 1, 1, 0.035f},
            {3f, 9, 10, 12, 67, 0, 1, 1, 0.035f},
            {1f, 9, 10, 12, 9, 0, 1, 1, 0.035f},
            {3f, 10, 30, 5, 45, 0, 1, 1, 0.035f},
            {3f, 10, 13, 15, -45, 0, 1, 1, 0.035f},
            {-3f, 14, 10, 15, -45, 0, 1, 1, 0.035f},
            {3f, 25, 17, 13, 15, 0, 1, 1, 0.035f},
            {3f, 50, 30, 13, 15, 0, 1, 1, 0.035f},
            {3f, 50, 12, 13, -15, 0, 1, 1, 0.035f},
            {-3f, -20, 12, -13, 0, 15, 1, 1, 0.035f},
            {3, 45, 3, 3, 3, 0, 1, 1, 0.035f},
            {3, 30, 3, 3, 45, 0, 1, 1, 0.035f}};

    float[] params;

    Color[][] availablePalettes = new Color[][]{
            {Color.BLACK, Color.ORANGE, Color.CYAN, Color.CORAL},
            {Color.BLACK, Color.valueOf("#662341"), Color.valueOf("#ffe240"), Color.FIREBRICK},
            {Color.BLACK, Color.ORANGE, Color.RED, Color.GRAY},
            {Color.BLACK, Color.LIME, Color.TEAL, Color.CLEAR},
            {Color.BLACK, Color.SKY, Color.BLACK, Color.BLACK, Color.TEAL, Color.BLACK}};

    SimulationScreen(Game game, float[] params) {
        init(game, trailMapWidth, trailMapHeight);

        ShaderLoader.BasePath = "shaders/";
        blurProcessor = new PostProcessor(WIDTH, HEIGHT, false, false, true);
        Bloom bloom = new Bloom(WIDTH, HEIGHT);
        bloom.setBlurPasses(3);
        bloom.setBloomIntensity(1f);
        bloom.setBloomSaturation(1f);
        blurProcessor.addEffect(bloom);

        agents = new Agent[numberOfAgents];
        for (int i = 0; i < agents.length; i++) {
            Agent newAgent = new Agent(false);
            if (randomSpawn) {
                newAgent.x = (float) (trailMapWidth * random());
                newAgent.y = (float) (trailMapHeight * random());
            } else {
                newAgent.x = trailMapWidth / 2f;
                newAgent.y = trailMapHeight / 2f;
            }
            newAgent.rotation = (float) (random() * 360);
            agents[i] = newAgent;
        }

        this.params = params;

        if (params == null) {
            this.params = availableRules[currentSimulationRule];
        }
        changeParamsForAllAgents(this.params);

        gridPixmap = new Pixmap(trailMapWidth, trailMapHeight, Format.RGBA8888);
        gridPixmapColors = new float[trailMapWidth][trailMapHeight];
        gridTexture = new Texture(gridPixmap);
    }

    @Override
    public void show() {

    }

    void spreadInCirce(int type) {

        for (Agent agent : agents) {
            double deg = random();
            double dist = random();
            agent.x = trailMapWidth / 2f - (float) (MathUtils.sinDeg((float) (deg * 360)) * trailMapHeight / 4f * dist);
            agent.y = trailMapHeight / 2f - (float) (MathUtils.cosDeg((float) (deg * 360)) * trailMapHeight / 4f * dist);
            switch (type) {
                case (0):
                    agent.rotation = (float) (deg * 360);
                    break;
                case (1):
                    agent.rotation = (float) (pow(dist + 1, deg + 1) * 360);
                    break;
                case (2):
                    agent.rotation = (float) ((deg + 1) * log10(deg * 9 + 1)) * 360;
                    break;
                case (3):
                    agent.rotation = -(float) (dist * 360);
                    break;
                case (4):
                    agent.rotation = (float) (deg / dist * 360);
                    break;
                case (5):
                    agent.rotation = (float) -(deg * dist * 360);
                    break;
                case (6):
                    agent.rotation = 90 - (float) (deg * 360);
                    break;
                case (7):
                    agent.rotation = (float) (pow(deg + 1, dist + 1) * 360);
                    break;
            }
        }

    }

    @Override
    public void render(float delta) {

        super.render(delta);

        final float relativeDelta;
        if (recording) {
            relativeDelta = 0.8f;
        } else {
            relativeDelta = delta / 0.16f;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            recording = !recording;
            frame = 0;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.F1)) {
            for (Agent agent : agents) {
                agent.x = trailMapWidth / 2f;
                agent.y = trailMapHeight / 2f;
                agent.rotation = (float) (random() * 360);
            }
        }

        for (int i = 2; i < 10; i++) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.valueOf("F" + i))) {
                spreadInCirce(i - 2);
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.H)) {
            for (int x = 0; x < trailMapWidth; x++) {
                for (int y = 0; y < trailMapHeight; y++) {
                    gridPixmapColors[x][y] = 0;
                }
            }
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

        if (Gdx.input.isKeyJustPressed(Input.Keys.B)) {
            bloom = !bloom;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.J)) {
            showAgents = !showAgents;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.F)) {
            for (int i = 0; i < 250; i++) {
                for (Agent agent : agents) {
                    agent.update(relativeDelta);
                }
                fade();
            }
        }

        if (bloom) {
            blurProcessor.captureNoClear();
        }
        batch.begin();
        gridTexture.draw(gridPixmap, 0, 0);
        batch.draw(gridTexture, 0, 0);
        batch.end();
        if (bloom) {
            blurProcessor.render();
        }

        if (showAgents) {
            fade();
        }
        for (Agent agent : agents) {
            agent.update(relativeDelta);
        }
        if (!showAgents) {
            fade();
        }
        copyOver();

        if (recording) {
            if (bloom) {
                makeAScreenShot(frame);
            } else {
                makeAScreenShot(frame, gridPixmap);
            }
            frame++;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            game.setScreen(new SettingsScreen(game, params));
            this.dispose();
        }
    }

    public static float getFromMap(int x, int y) {
        try {
            return gridPixmapColors[x][y];
        } catch (Exception e) {
            return 0;
        }

    }

    void fade() {
        for (int x = 1; x < trailMapWidth - 1; x++) {
            for (int y = 1; y < trailMapHeight - 1; y++) {
                float sum = 0;
                for (int xOffset = -1; xOffset <= 1; ++xOffset) {
                    for (int yOffset = -1; yOffset <= 1; ++yOffset) {
                        sum += getFromMap(x + xOffset, y + yOffset);
                    }
                }
                gridPixmapColors[x][y] = sum / 9f;
            }
        }

        for (int x = 0; x < trailMapWidth; x++) {
            for (int y = 0; y < trailMapHeight; y++) {
                gridPixmapColors[x][y] = max(gridPixmapColors[x][y] - evaporationRate, 0);
            }
        }
    }

    void copyOver() {
        for (int x = 0; x < trailMapWidth; x++) {
            for (int y = 0; y < trailMapHeight; y++) {

                switch (currentPalette) {
                    case (0):
                        float ratio1 = (gridPixmapColors[x][y] - params[6] / 2f) * 2;
                        float ratio2 = (ratio1 - gridPixmapColors[x][y]) * 2;
                        Color c1 = new Color(mixTwoColors(Color.SLATE, Color.WHITE, ratio1));
                        gridPixmap.drawPixel(x, y, mixTwoColors(c1, Color.CORAL, ratio2));
                        break;
                    case (1):
                        gridPixmap.drawPixel(x, y,
                                mixTwoColors(
                                        new Color(interpolate(
                                                gridPixmapColors[x][y], params[6],
                                                Color.BLACK, Color.ROYAL,
                                                Color.ROYAL, Color.CYAN,
                                                Color.WHITE)),
                                        new Color(mixTwoColors(
                                                Color.LIGHT_GRAY,
                                                Color.BLACK, 1 - gridPixmapColors[x][y])), gridPixmapColors[x][y]));
                        break;
                    default:
                        if (currentPalette == -1) {
                            currentPalette = availablePalettes.length - 1;
                        }
                        if (currentPalette - 2 == availablePalettes.length) {
                            currentPalette = 0;
                        } else {
                            gridPixmap.drawPixel(x, y, interpolate(gridPixmapColors[x][y], params[6], availablePalettes[currentPalette - 2]));
                        }
                        break;
                }
            }
        }
    }

    void changeSimulationRules() {
        if (currentSimulationRule == availableRules.length) {
            currentSimulationRule = 0;
        }
        if (currentSimulationRule == -1) {
            currentSimulationRule = availableRules.length - 1;
        }
        params = availableRules[currentSimulationRule];
        changeParamsForAllAgents(availableRules[currentSimulationRule]);
    }

    void changeParamsForAllAgents(float[] params) {
        for (Agent agent : agents) {
            agent.speed = params[0];
            agent.turnSpeed = params[1];
            agent.sensorLength = (int) params[2];
            agent.sensorLengthOffset = (int) params[3];
            agent.sensorAngleOffset = (int) params[4];
            agent.sensorAngleOffset2 = (int) params[5];
            agent.maxPheromoneTrailConcentration = params[6];
            agent.pheromoneDepositRate = params[7];
        }
        evaporationRate = params[8];
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        batch.dispose();
        gridTexture.dispose();
        gridPixmap.dispose();
    }
}
