import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Wall extends Tile {
    public Wall(int x, int y) {
        super(x, y, false); // false = không đi qua được
    }

    @Override
    public void render(GraphicsContext gc) {
        gc.setFill(Color.GRAY);
        gc.fillRect(x * 32, y * 32, 32, 32);

        // Vẽ viền cho đẹp mắt
        gc.setStroke(Color.DARKGRAY);
        gc.strokeRect(x * 32, y * 32, 32, 32);
    }
}