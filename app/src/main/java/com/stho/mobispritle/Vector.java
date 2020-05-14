package com.stho.mobispritle;

import org.jetbrains.annotations.NotNull;

/**
 * Created by shoedtke on 23.12.2015.
 */
@SuppressWarnings("WeakerAccess")
public class Vector {
    public float x;
    public float y;
    public float z;


    public Vector() {
        this(0, 0, 0);
    }

    public Vector(float[] values) {
        this(values[0], values[1], values[2]);
    }

    public Vector(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void setValues(float[] values) {
        this.x = values[0];
        this.y = values[1];
        this.z = values[2];
    }

    @NotNull
    public Vector clone() {
        return new Vector(this.x, this.y, this.z);
    }

    public float getLength() {
        return (float)Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
    }

    public void reset() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }
}
