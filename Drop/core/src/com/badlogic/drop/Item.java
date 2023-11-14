package com.badlogic.drop;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class Item {
    Rectangle bounds;
    Texture texture;
    float speedY;

    public Item(Texture texture, float x, float y, float width, float height, float speedY) {
        this.texture = texture;
        this.bounds = new Rectangle(x, y, width, height);
        this.speedY = speedY;

    }

    public void update(float deltaTime) {
        bounds.y -= speedY * deltaTime;
    }

    public void draw(SpriteBatch batch) {
        batch.draw(texture, bounds.x, bounds.y, bounds.width, bounds.height);
    }


}
