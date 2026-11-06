
package bomberman.entities;

import bomberman.GameAssets;
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
    int size = 40;

    switch (type) {
        case BOMB_UP -> gc.drawImage(
            GameAssets.ITEM_BOMB,
            x + 4, y + 4, size, size
        );

        case FLAME_UP -> gc.drawImage(
            GameAssets.ITEM_FLAME,
            x + 4, y + 4, size, size
        );

        case SPEED_UP -> gc.drawImage(
            GameAssets.ITEM_SPEED,
            x + 4, y + 4, size, size
        );
    }
}
}
