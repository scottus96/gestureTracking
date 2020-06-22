package com.example.bettergesturetest;

public class Marker {
    Marker(int x, int y, int radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;
    }

    int getX() {
        return this.x;
    }

    int getY() {
        return this.y;
    }

    int getRadius() {
        return this.radius;
    }

    void setRadius(int x) {
        this.radius = x;
    }

    private int x = 0;
    private int y = 0;
    private int radius = 0;

}
