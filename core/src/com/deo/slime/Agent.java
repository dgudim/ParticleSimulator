package com.deo.slime;

import com.badlogic.gdx.math.MathUtils;

import static com.badlogic.gdx.math.MathUtils.clamp;
import static com.badlogic.gdx.math.MathUtils.random;
import static com.deo.slime.Main.HEIGHT;
import static com.deo.slime.Main.WIDTH;

public class Agent {
    float x;
    float y;
    float rotation;

    /*
    final float speed = 2f;
    final float turnSpeed = 50;
    final int sensorLength = 8;
    final int sensorLengthOffset = 2;
    final int sensorAngleOffset = 50;

    final float speed = 2f;
    final float turnSpeed = 50;
    final int sensorLength = 7;
    final int sensorLengthOffset = 2;
    final int sensorAngleOffset = 40;
     */

    final float speed = 2f;
    final float turnSpeed = 50;
    final int sensorLength = 9;
    final int sensorLengthOffset = 19;
    final int sensorAngleOffset = 50;

    void update(float[][] colors) {
        x -= MathUtils.cosDeg(rotation) * speed;
        y -= MathUtils.sinDeg(rotation) * speed;
        x = clamp(x, 0, WIDTH - 1);
        y = clamp(y, 0, HEIGHT - 1);
        if (x <= sensorLength || y <= sensorLength || y >= HEIGHT - 1 - sensorLength || x >= WIDTH - 1 - sensorLength) {
            rotation = 360 * random() - 180;
        }
        senseAndRotate(colors);
        colors[(int) x][(int) y] = 1;
    }

    void senseAndRotate(float[][] colors) {

        float valueForward = 0;
        float valueRight = 0;
        float valueLeft = 0;

        for (int i = 0; i < sensorLength; i++) {

            int xForward = (int) (x - MathUtils.cosDeg(rotation) * (sensorLengthOffset + i));
            int yForward = (int) (y - MathUtils.sinDeg(rotation) * (sensorLengthOffset + i));

            int xRight = (int) (x - MathUtils.cosDeg(rotation + sensorAngleOffset) * (sensorLengthOffset + i));
            int yRight = (int) (y - MathUtils.sinDeg(rotation + sensorAngleOffset) * (sensorLengthOffset + i));

            int xLeft = (int) (x - MathUtils.cosDeg(rotation - sensorAngleOffset) * (sensorLengthOffset + i));
            int yLeft = (int) (y - MathUtils.sinDeg(rotation - sensorAngleOffset) * (sensorLengthOffset + i));

            xForward = clamp(xForward, 0, WIDTH - 1);
            xRight = clamp(xRight, 0, WIDTH - 1);
            xLeft = clamp(xLeft, 0, WIDTH - 1);

            yForward = clamp(yForward, 0, HEIGHT - 1);
            yRight = clamp(yRight, 0, HEIGHT - 1);
            yLeft = clamp(yLeft, 0, HEIGHT - 1);

            valueForward += colors[xForward][yForward];
            valueRight += colors[xRight][yRight];
            valueLeft += colors[xLeft][yLeft];
        }

        float randomSteerStrength = random() + 0.1f;

        if (valueForward > valueLeft && valueForward > valueRight) {
            rotation += 0;
        } else if (valueForward < valueLeft && valueForward < valueRight) {
            rotation += (randomSteerStrength - 0.5) * 2 * turnSpeed;
        } else if (valueRight > valueLeft) {
            rotation += randomSteerStrength * turnSpeed;
        } else if (valueLeft > valueRight) {
            rotation -= randomSteerStrength * turnSpeed;
        }

    }

}
