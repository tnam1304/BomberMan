import javafx.scene.canvas.GraphicsContext;

public abstract class GameObject {
    protected int x;
    protected int y;
    protected boolean alive = true;

    public GameObject(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public abstract void update(double deltaTime);
    public abstract void render(GraphicsContext gc);

    public int getX() { return x; }
    public void setX(int x) { this.x = x; }

    public int getY() { return y; }
    public void setY(int y) { this.y = y; }

    public boolean isAlive() { return alive; }
    public void setAlive(boolean alive) { this.alive = alive; }
}