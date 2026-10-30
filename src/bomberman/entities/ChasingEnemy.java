
package bomberman.entities;

import bomberman.GameConfig;
import bomberman.enums.Direction;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.List;

public class ChasingEnemy extends Enemy {

    private static final int DETECTION_RANGE =
            5 * GameConfig.TILE_SIZE;

    public ChasingEnemy(int x, int y) {
        super(x, y);
        speed = 2;
    }

    public void update(List<Entity> entities, Bomber player) {

        int distanceX = player.getX() - x;
        int distanceY = player.getY() - y;

        double distance = Math.sqrt(
                distanceX * distanceX + distanceY * distanceY
        );

        // Nếu Bomber ở gần, tìm cách đuổi theo
        if (player.isAlive() && distance <= DETECTION_RANGE) {

            int nextX = x;
            int nextY = y;

            // Ưu tiên di chuyển theo trục có khoảng cách lớn hơn
            if (Math.abs(distanceX) >= Math.abs(distanceY)) {
                if (distanceX < 0) {
                    nextX -= speed;
                    direction = Direction.LEFT;
                } else if (distanceX > 0) {
                    nextX += speed;
                    direction = Direction.RIGHT;
                }
            } else {
                if (distanceY < 0) {
                    nextY -= speed;
                    direction = Direction.UP;
                } else if (distanceY > 0) {
                    nextY += speed;
                    direction = Direction.DOWN;
                }
            }

            if (!checkCollision(nextX, nextY, entities)) {
                x = nextX;
                y = nextY;
            } else {
                // Nếu đường đuổi bị chặn thì đổi sang hướng khác
                super.update(entities);
            }

        } else {
            // Không thấy Bomber thì di chuyển ngẫu nhiên
            super.update(entities);
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        gc.setFill(Color.DARKORANGE);
        gc.fillOval(x + 2, y + 2, 28, 28);

        gc.setFill(Color.WHITE);
        gc.fillOval(x + 8, y + 8, 5, 5);
        gc.fillOval(x + 19, y + 8, 5, 5);
    }
}
