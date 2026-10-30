package bomberman.entities;

import javafx.scene.image.Image;

public abstract class Tile extends Entity {
    protected boolean collideable;
    protected boolean destroyable;

    public Tile(int x, int y, Image img, boolean collideable, boolean destroyable) {
        super(x, y, img);
        this.collideable = collideable;
        this.destroyable = destroyable;
    }

    public boolean isCollideable() { return collideable; }
    public boolean isDestroyable() { return destroyable; }
}