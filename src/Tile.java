import javafx.scene.canvas.GraphicsContext;

public abstract class Tile extends GameObject {
    protected boolean walkable;

    public Tile(int x, int y, boolean walkable) {
        super(x, y);
        this.walkable = walkable;
    }

    public boolean isWalkable() {
        return walkable;
    }

    public void setWalkable(boolean walkable) {
        this.walkable = walkable;
    }

    @Override
    public void update(double deltaTime) {
        // Mặc định ô bản đồ tĩnh không cần update logic
    }
}