package bomberman.entities;

import bomberman.GameAssets;
import bomberman.GameConfig;
import bomberman.enums.Direction;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.List;

public class Enemy extends DynamicEntity {

    protected int moveCounter = 0;
    protected int changeDirectionAfter = 60;

    public Enemy(int x, int y) {
        super(x, y, null, 1);
        direction = Direction.LEFT;
    }

    @Override
    public void update() {
    }

    public void update(List<Entity> entities) {
        moveCounter++;

        if (moveCounter >= changeDirectionAfter) {
            changeDirection();
            moveCounter = 0;
        }

        int nextX = x;
        int nextY = y;

        switch (direction) {
            case LEFT -> nextX -= speed;
            case RIGHT -> nextX += speed;
            case UP -> nextY -= speed;
            case DOWN -> nextY += speed;
            default -> {
            }
        }

        if (!checkCollision(nextX, nextY, entities)) {
            x = nextX;
            y = nextY;
        } else {
            changeDirection();
        }
    }

    protected boolean checkCollision(
            int nextX,
            int nextY,
            List<Entity> entities
    ) {
        Rectangle2D nextBounds = new Rectangle2D(
                nextX,
                nextY,
                GameConfig.TILE_SIZE,
                GameConfig.TILE_SIZE
        );

        // Không cho Enemy đi ra ngoài bản đồ
        if (nextX < 0 || nextY < 0
                || nextX + GameConfig.TILE_SIZE > GameConfig.WIDTH
                || nextY + GameConfig.TILE_SIZE > GameConfig.HEIGHT) {
            return true;
        }

        for (Entity entity : entities) {
            if (entity instanceof Tile tile && tile.isCollideable()) {
                if (nextBounds.intersects(entity.getBoundary())) {
                    return true;
                }
            }
        }

        return false;
    }

    protected void changeDirection() {
        Direction[] directions = {
                Direction.UP,
                Direction.DOWN,
                Direction.LEFT,
                Direction.RIGHT
        };

        direction = directions[
                (int) (Math.random() * directions.length)
                ];
    }

    @Override
    public void move() {
    }

    @Override
public void render(GraphicsContext gc) {

    if (!alive) {
        return;
    }

    gc.drawImage(
            GameAssets.ENEMY_RED,
            x,
            y,
            30,
            32
    );
}
}
