package com.deo.slime;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import static com.deo.slime.Launcher.HEIGHT;
import static com.deo.slime.Launcher.WIDTH;

public class SettingsScreen extends GenericScreen {

    final String fontChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890\"!`?'.,;:()[]{}<>|/@\\^$€-%+=#_&~*ёйцукенгшщзхъэждлорпавыфячсмитьбюЁЙЦУКЕНГШЩЗХЪЭЖДЛОРПАВЫФЯЧСМИТЬБЮ";

    Stage stage;
    ShapeRenderer shapeRenderer;
    float[] params;
    String paramsString = "";
    BitmapFont font;

    SettingsScreen(Game game, float[] params) {
        init(game, WIDTH, HEIGHT);
        this.params = params;

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("font.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 38;
        parameter.characters = fontChars;
        font = generator.generateFont(parameter);
        generator.dispose();
        font.getData().markupEnabled = true;

        stage = new Stage(viewport, batch);

        shapeRenderer = new ShapeRenderer();

        TextureAtlas uiAtlas = new TextureAtlas(Gdx.files.internal("ui.atlas"));
        Skin uiTextures = new Skin();
        uiTextures.addRegions(uiAtlas);

        Slider.SliderStyle sliderStyle = new Slider.SliderStyle();
        sliderStyle.background = uiTextures.getDrawable("progressBarBg");
        sliderStyle.knob = uiTextures.getDrawable("progressBarKnob");
        sliderStyle.knobDown = uiTextures.getDrawable("progressBarKnob_enabled");
        sliderStyle.knobOver = uiTextures.getDrawable("progressBarKnob_over");
        sliderStyle.background.setMinHeight(63);
        sliderStyle.knob.setMinHeight(30);
        sliderStyle.knobDown.setMinHeight(30);
        sliderStyle.knobOver.setMinHeight(30);

        Slider speed = new Slider(-10, 10, 0.5f, false, sliderStyle);
        Slider turnSpeed = new Slider(-100, 100, 1, false, sliderStyle);
        Slider sensorLength = new Slider(-100, 100, 1, false, sliderStyle);
        Slider sensorLengthOffset = new Slider(-100, 100, 1, false, sliderStyle);
        Slider sensorAngleOffset = new Slider(-180, 180, 1, false, sliderStyle);
        Slider sensorAngleOffset2 = new Slider(-180, 180, 1, false, sliderStyle);
        Slider maxPheromoneTrailConcentration = new Slider(1, 100, 1, false, sliderStyle);
        Slider pheromoneDepositRate = new Slider(0.01f, 10, 0.01f, false, sliderStyle);
        Slider evaporationRate = new Slider(0.01f, 10, 0.01f, false, sliderStyle);

        float start = 394;
        float step = 50;

        speed.setBounds(3, start, WIDTH / 2f - 6, 63);
        turnSpeed.setBounds(3, start - step, WIDTH / 2f - 6, 63);
        sensorLength.setBounds(3, start - step * 2, WIDTH / 2f - 6, 63);
        sensorLengthOffset.setBounds(3, start - step * 3, WIDTH / 2f - 6, 63);
        sensorAngleOffset.setBounds(3, start - step * 4, WIDTH / 2f - 6, 63);
        sensorAngleOffset2.setBounds(3, start - step * 5, WIDTH / 2f - 6, 63);
        maxPheromoneTrailConcentration.setBounds(3, start - step * 6, WIDTH / 2f - 6, 63);
        pheromoneDepositRate.setBounds(3, start - step * 7, WIDTH / 2f - 6, 63);
        evaporationRate.setBounds(3, start - step * 8, WIDTH / 2f - 6, 63);

        addListenerToSlider(speed, 0);
        addListenerToSlider(turnSpeed, 1);
        addListenerToSlider(sensorLength, 2);
        addListenerToSlider(sensorLengthOffset, 3);
        addListenerToSlider(sensorAngleOffset, 4);
        addListenerToSlider(sensorAngleOffset2, 5);
        addListenerToSlider(maxPheromoneTrailConcentration, 6);
        addListenerToSlider(pheromoneDepositRate, 7);
        addListenerToSlider(evaporationRate, 8);

        stage.addActor(speed);
        stage.addActor(turnSpeed);
        stage.addActor(sensorLength);
        stage.addActor(sensorLengthOffset);
        stage.addActor(sensorAngleOffset);
        stage.addActor(sensorAngleOffset2);
        stage.addActor(maxPheromoneTrailConcentration);
        stage.addActor(pheromoneDepositRate);
        stage.addActor(evaporationRate);

        convertParamsToString();
    }

    void addListenerToSlider(final Slider slider, final int paramIndex) {
        slider.setValue(params[paramIndex]);
        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                params[paramIndex] = (int) (slider.getValue() * 1000) / 1000f;
                convertParamsToString();
            }
        });
    }

    void convertParamsToString() {
        paramsString = "[#FFFF99]Speed: [#77FF77]" + params[0] +
                "\n[#FFFF99]Turning Speed: [#77FF77]" + params[1] +
                "\n[#FFFF99]Sensor Length: [#77FF77]" + params[2] +
                "\n[#FFFF99]Sensor Length Offset: [#77FF77]" + params[3] +
                "\n[#FFFF99]Sensor Angle Offset: [#77FF77]" + params[4] +
                "\n[#FFFF99]Sensor Angle Offset 2: [#77FF77]" + params[5] +
                "\n[#FFFF99]Max Pheromone Trail Concentration: [#77FF77]" + params[6] +
                "\n[#FFFF99]Pheromone Deposit Rate: [#77FF77]" + params[7] +
                "\n[#FFFF99]Evaporation Rate: [#77FF77]" + params[8];
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {

        super.render(delta);

        shapeRenderer.setProjectionMatrix(camera.combined);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.rectLine(WIDTH / 2f, 0f, WIDTH / 2f, HEIGHT, 5);

        shapeRenderer.setColor(Color.GRAY);
        for (int i = 0; i < HEIGHT; i += 30) {
            shapeRenderer.rectLine(0, i, WIDTH / 2f, i, 2 * camera.zoom);
        }
        for (int i = 0; i < WIDTH / 2f; i += 30) {
            shapeRenderer.rectLine(i + 15, 0, i + 15, HEIGHT, 2 * camera.zoom);
        }
        shapeRenderer.setColor(Color.WHITE);

        if (Gdx.input.isKeyPressed(Input.Keys.NUM_2)) {
            camera.zoom -= delta;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.NUM_1)) {
            camera.zoom += delta;
        }

        float xStart = WIDTH / 4f;
        float yStart = HEIGHT / 2f - 45;
        drawBox(xStart, yStart, 30);
        if (params[4] >= 0) {
            shapeRenderer.setColor(Color.CHARTREUSE);
        } else {
            shapeRenderer.setColor(Color.FIREBRICK);
        }

        for (float i = 0; i < 3; i++) {
            float yAStart = yStart + MathUtils.cosDeg(i * params[4] - params[4]) * (params[3] * 30 - 10);
            float xAStart = xStart + MathUtils.sinDeg(i * params[4] - params[4]) * (params[3] * 30 - 10);

            float yEnd = yAStart + MathUtils.cosDeg(i * params[4] - params[4] + i * params[5] - params[5]) * (params[2] * 30 - 10);
            float xEnd = xAStart + MathUtils.sinDeg(i * params[4] - params[4] + i * params[5] - params[5]) * (params[2] * 30 - 10);
            shapeRenderer.rectLine(xAStart, yAStart, xEnd, yEnd, 3 * camera.zoom);
        }
        shapeRenderer.setColor(Color.WHITE);

        shapeRenderer.end();
        batch.begin();
        font.draw(batch, paramsString, WIDTH / 2f, HEIGHT / 2f - 100, WIDTH, -1, false);
        batch.end();
        stage.draw();
        stage.act(delta);

        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            game.setScreen(new SimulationScreen(game, params));
            this.dispose();
        }
    }

    void drawBox(float x, float y, float a) {
        shapeRenderer.rect(x - a / 2f, y - a / 2f, a, a);
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

    }
}
