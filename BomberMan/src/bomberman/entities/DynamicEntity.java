package bomberman.entities;

import bomberman.enums.Direction;
import javafx.scene.image.Image;

public abstract class DynamicEntity extends Entity {
    protected int speed;
    protected Direction direction = Direction.NONE;

    public DynamicEntity(int x, int y, Image img, int speed) {
        super(x, y, img);
        this.speed = speed;
    }

    public abstract void move();
}