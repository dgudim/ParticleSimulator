package com.deo.slime.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.deo.slime.Main;

import static com.deo.slime.Main.WIDTH;
import static com.deo.slime.Main.HEIGHT;

public class DesktopLauncher {
    public static void main(String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();

        config.foregroundFPS = 1010;
        config.width = WIDTH;
        config.height = HEIGHT;
        config.fullscreen = true;

        new LwjglApplication(new Main(), config);
    }
}
