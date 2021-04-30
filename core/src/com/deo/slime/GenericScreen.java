package com.deo.slime;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import static java.lang.Math.min;

public class GenericScreen implements Screen {

    OrthographicCamera camera;
    ScreenViewport viewport;
    SpriteBatch batch;
    BitmapFont font;
    final String fontChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890\"!`?'.,;:()[]{}<>|/@\\^$€-%+=#_&~*ёйцукенгшщзхъэждлорпавыфячсмитьбюЁЙЦУКЕНГШЩЗХЪЭЖДЛОРПАВЫФЯЧСМИТЬБЮ";

    float width, height;

    Game game;

    void init(Game game, float width, float height) {
        this.game = game;
        this.width = width;
        this.height = height;
        camera = new OrthographicCamera(width, height);
        viewport = new ScreenViewport(camera);
        batch = new SpriteBatch();

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("font.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 38;
        parameter.characters = fontChars;
        font = generator.generateFont(parameter);
        generator.dispose();
        font.getData().markupEnabled = true;
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyPressed(Input.Keys.F10)) {
            boolean fullScreen = Gdx.graphics.isFullscreen();
            Graphics.DisplayMode currentMode = Gdx.graphics.getDisplayMode();
            if (fullScreen)
                Gdx.graphics.setWindowedMode(currentMode.width, currentMode.height);
            else
                Gdx.graphics.setFullscreenMode(currentMode);
        }
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.setProjectionMatrix(camera.combined);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        camera.position.set(this.width / 2f, this.height / 2f, 0);
        float tempScaleH = height / (float) this.height;
        float tempScaleW = width / (float) this.width;
        float zoom = min(tempScaleH, tempScaleW);
        camera.zoom = 1 / zoom;
        camera.update();

        Gdx.graphics.setUndecorated(true);
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
        font.dispose();
    }
}
