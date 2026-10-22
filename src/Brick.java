import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Brick extends Tile {
    private boolean destroyed = false;

    public Brick(int x, int y) {
        super(x, y, false); // Lúc chưa phá thì không đi qua được
    }

    public void destroy() {
        this.destroyed = true;
        this.walkable = true; // Phá xong thì cho nhân vật đi qua
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    @Override
    public void render(GraphicsContext gc) {
        if (!destroyed) {
            gc.setFill(Color.SADDLEBROWN);
            gc.fillRect(x * 32, y * 32, 32, 32);

            gc.setStroke(Color.BLACK);
            gc.strokeRect(x * 32, y * 32, 32, 32);
        }
    }
}