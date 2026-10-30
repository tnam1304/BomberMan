
package bomberman.entities;

import bomberman.GameConfig;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Item extends Entity {

    public enum ItemType {
        BOMB_UP,
        FLAME_UP,
        SPEED_UP
    }

    private ItemType type;

    public Item(int x, int y, ItemType type) {
        super(x, y, null);
        this.type = type;
    }

    public ItemType getType() {
        return type;
    }

    @Override
    public void update() {
    }

    @Override
    public void render(GraphicsContext gc) {

        int size = GameConfig.TILE_SIZE;

        switch (type) {
            case BOMB_UP -> gc.setFill(Color.DODGERBLUE);
            case FLAME_UP -> gc.setFill(Color.ORANGERED);
            case SPEED_UP -> gc.setFill(Color.LIMEGREEN);
        }

        gc.fillRoundRect(x + 5, y + 5, size - 10, size - 10, 10, 10);

        gc.setFill(Color.WHITE);

        switch (type) {
            case BOMB_UP -> gc.fillText("B", x + 12, y + 22);
            case FLAME_UP -> gc.fillText("F", x + 12, y + 22);
            case SPEED_UP -> gc.fillText("S", x + 12, y + 22);
        }
    }
}
