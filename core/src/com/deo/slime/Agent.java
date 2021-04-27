package com.deo.slime;

import com.badlogic.gdx.math.MathUtils;

import static com.badlogic.gdx.math.MathUtils.clamp;
import static com.badlogic.gdx.math.MathUtils.floor;
import static com.badlogic.gdx.math.MathUtils.random;
import static com.deo.slime.Main.HEIGHT;
import static com.deo.slime.Main.WIDTH;

public class Agent {
    float x;
    float y;
    float rotation;

    float speed = 3f;
    float turnSpeed = 50;
    int sensorLength = 12;
    int sensorLengthOffset = 13;
    int sensorAngleOffset = -15;

    boolean variableSteering = false;
    int variableSteeringStrength = 1;
    int variableSteeringAngle = 360;

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
        if (!variableSteering) {

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

            float randomSteerStrength = random() + 0.01f;

            if (valueForward > valueLeft && valueForward > valueRight) {
                rotation += 0;
            } else if (valueForward < valueLeft && valueForward < valueRight) {
                rotation += (randomSteerStrength - 0.5) * 2 * turnSpeed;
            } else if (valueRight > valueLeft) {
                rotation += randomSteerStrength * turnSpeed;
            } else if (valueLeft > valueRight) {
                rotation -= randomSteerStrength * turnSpeed;
            }
        } else {

            int currentStrongestAngle = 0;
            float currentStrongestDirSum = 0;

            for (int i = -variableSteeringAngle; i < variableSteeringAngle; i++) {
                float sum = 0;
                for (int l = 0; l < sensorLength; l++) {
                    int senseX = (int) (x - MathUtils.cosDeg(rotation + i) * (sensorLengthOffset + l));
                    int senseY = (int) (y - MathUtils.sinDeg(rotation + i) * (sensorLengthOffset + l));
                    senseX = clamp(senseX, 0, WIDTH - 1);
                    senseY = clamp(senseY, 0, HEIGHT - 1);
                    sum += colors[senseX][senseY];
                }
                if (sum > currentStrongestDirSum) {
                    currentStrongestAngle = i;
                    currentStrongestDirSum = sum;
                } else if (sum == currentStrongestDirSum) {
                    if (random() > 0.49f) {
                        currentStrongestAngle = i;
                    }
                }
            }

            rotation = (rotation + currentStrongestAngle * variableSteeringStrength) / (variableSteeringStrength + 1);

        }

    }

}
