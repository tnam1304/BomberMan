
package bomberman;

import javafx.scene.image.Image;
import java.io.File;

public class GameAssets {

    private static Image load(String name) {
        File file = new File("res/images/" + name);

        if (!file.exists()) {
            throw new RuntimeException(
                "Không tìm thấy hình ảnh: " + file.getAbsolutePath()
            );
        }

        return new Image(file.toURI().toString());
    }

    // Player
    public static final Image PLAYER =
            load("player.png");

    // Enemy
    public static final Image ENEMY_RED =
            load("enemy_red.png");

    public static final Image ENEMY_BLUE =
            load("enemy_blue.png");

    public static final Image ENEMY_PINK =
            load("enemy_pink.png");

    // Map
    public static final Image WALL =
            load("wall.png");

    public static final Image BRICK =
            load("brick.png");

    public static final Image FLOOR =
            load("floor.png");

    public static final Image EXIT =
            load("exit.png");

    // Bomb
    public static final Image BOMB =
            load("bomb.png");

    // Items
    public static final Image ITEM_BOMB =
            load("item_bomb.png");

    public static final Image ITEM_FLAME =
            load("item_flame.png");

    public static final Image ITEM_SPEED =
            load("item_speed.png");

    // Explosion
    public static final Image EXPLOSION_CENTER =
            load("explosion_center.png");

    public static final Image EXPLOSION_HORIZONTAL =
            load("explosion_horizontal.png");

    public static final Image EXPLOSION_VERTICAL =
            load("explosion_vertical.png");

    public static final Image EXPLOSION_END =
            load("explosion_end.png");
}
