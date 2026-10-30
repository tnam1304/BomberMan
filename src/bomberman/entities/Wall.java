package bomberman.entities;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class Wall extends Tile {
    public Wall(int x, int y, Image img) {
        super(x, y, img, true, false);
    }

    @Override
    public void update() {}

    @Override
    public void render(GraphicsContext gc) {
        if (img != null) {
            gc.drawImage(img, x, y);
        } else {
            gc.strokeRect(x, y, 32, 32); // Vẽ ô vuông rỗng tạm thời
        }
    }
}