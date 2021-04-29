package com.deo.slime.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.deo.slime.Launcher;

import static com.deo.slime.Launcher.HEIGHT;
import static com.deo.slime.Launcher.WIDTH;

public class DesktopLauncher {
    public static void main(String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();

        config.foregroundFPS = 60;
        config.width = WIDTH;
        config.height = HEIGHT;
        config.fullscreen = false;

        new LwjglApplication(new Launcher(), config);
    }
}
