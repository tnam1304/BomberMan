package bomberman.entities;

import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import bomberman.GameConfig;

public abstract class Entity {
    protected int x;
    protected int y;
    protected Image img;
    protected boolean alive = true;

    public Entity(int x, int y, Image img) {
        this.x = x;
        this.y = y;
        this.img = img;
    }

    public abstract void update();
    public abstract void render(GraphicsContext gc);

    // Khung va chạm hình chữ nhật 32x32 pixel
    public Rectangle2D getBoundary() {
        return new Rectangle2D(
                x,
                y,
                GameConfig.TILE_SIZE,
                GameConfig.TILE_SIZE
        );
    }

    public boolean isAlive() { return alive; }
    public void setAlive(boolean alive) { this.alive = alive; }
    public int getX() { return x; }
    public int getY() { return y; }
}