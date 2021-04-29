package com.deo.slime;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import static com.badlogic.gdx.math.MathUtils.clamp;
import static com.badlogic.gdx.math.MathUtils.random;
import static com.deo.slime.SimulationScreen.getFromMap;
import static com.deo.slime.SimulationScreen.gridPixmapColors;
import static com.deo.slime.SimulationScreen.trailMapHeight;
import static com.deo.slime.SimulationScreen.trailMapWidth;

public class Agent {
    float x;
    float y;
    float rotation;

    float speed = 3f;
    float turnSpeed = 50;
    int sensorLength = 12;
    int sensorLengthOffset = 13;
    int sensorAngleOffset = -15;
    int sensorAngleOffset2 = -15;
    float maxPheromoneTrailConcentration = 1;
    float pheromoneDepositRate = 1;

    void update(float deltaTime) {

        x -= MathUtils.cosDeg(rotation) * speed * deltaTime;
        y -= MathUtils.sinDeg(rotation) * speed * deltaTime;
        x = clamp(x, 0, trailMapWidth - 1);
        y = clamp(y, 0, trailMapHeight - 1);
        senseAndRotate(deltaTime);

        gridPixmapColors[(int) x][(int) y] = clamp(gridPixmapColors[(int) x][(int) y] + pheromoneDepositRate, 0, maxPheromoneTrailConcentration);
    }

    void senseAndRotate(float deltaTime) {

        float valueForward = 0;
        float valueRight = 0;
        float valueLeft = 0;

        Vector2 sensorStartLeft = new Vector2(
                x - MathUtils.cosDeg(rotation - sensorAngleOffset) * sensorLengthOffset,
                y - MathUtils.sinDeg(rotation - sensorAngleOffset) * sensorLengthOffset);
        Vector2 sensorStartRight = new Vector2(
                x - MathUtils.cosDeg(rotation + sensorAngleOffset) * sensorLengthOffset,
                y - MathUtils.sinDeg(rotation + sensorAngleOffset) * sensorLengthOffset);

        for (int i = 0; i < sensorLength; i++) {

            int xForward = (int) (x - MathUtils.cosDeg(rotation) * (sensorLengthOffset + i));
            int yForward = (int) (y - MathUtils.sinDeg(rotation) * (sensorLengthOffset + i));

            int xRight = (int) (sensorStartRight.x - MathUtils.cosDeg(rotation + sensorAngleOffset + sensorAngleOffset2) * i);
            int yRight = (int) (sensorStartRight.y - MathUtils.sinDeg(rotation + sensorAngleOffset + sensorAngleOffset2) * i);

            int xLeft = (int) (sensorStartLeft.x - MathUtils.cosDeg(rotation - sensorAngleOffset - sensorAngleOffset2) * i);
            int yLeft = (int) (sensorStartLeft.y - MathUtils.sinDeg(rotation - sensorAngleOffset - sensorAngleOffset2) * i);

            xForward = clamp(xForward, 0, trailMapWidth - 1);
            xRight = clamp(xRight, 0, trailMapWidth - 1);
            xLeft = clamp(xLeft, 0, trailMapWidth - 1);

            yForward = clamp(yForward, 0, trailMapHeight - 1);
            yRight = clamp(yRight, 0, trailMapHeight - 1);
            yLeft = clamp(yLeft, 0, trailMapHeight - 1);

            valueForward += getFromMap(xForward, yForward);
            valueRight += getFromMap(xRight, yRight);
            valueLeft += getFromMap(xLeft, yLeft);
        }

        float randomSteerStrength = random() + 0.01f;

        if (x <= sensorLength || y <= sensorLength || y >= trailMapHeight - 1 - sensorLength || x >= trailMapWidth - 1 - sensorLength) {
            rotation = 360 * random() - 180;
        }
        if (valueForward > valueLeft && valueForward > valueRight) {
            rotation += 0;
        } else if (valueForward < valueLeft && valueForward < valueRight) {
            rotation += (randomSteerStrength - 0.5) * 2 * turnSpeed * deltaTime;
        } else if (valueRight > valueLeft) {
            rotation += randomSteerStrength * turnSpeed * deltaTime;
        } else if (valueLeft > valueRight) {
            rotation -= randomSteerStrength * turnSpeed * deltaTime;
        }

    }

}
