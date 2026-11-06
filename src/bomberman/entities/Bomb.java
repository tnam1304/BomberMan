
package bomberman.entities;

import bomberman.GameAssets;
import bomberman.GameConfig;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Bomb extends Entity {

    private static final long FUSE_TIME = 2_000_000_000L;
    private static final long FLAME_TIME = 600_000_000L;

    private final int explosionRange;

    private long startTime;
    private long explosionTime;

    private boolean exploded = false;

    private final List<Rectangle2D> flameAreas = new ArrayList<>();
    private final List<Item> droppedItems = new ArrayList<>();

    private final Random random = new Random();

    private int destroyedBrickCount = 0;

    public Bomb(int x, int y, int explosionRange) {
        super(x, y, null);

        this.explosionRange = explosionRange;
        startTime = System.nanoTime();
    }

    public void update(List<Entity> entities) {

        if (!exploded) {

            long currentTime = System.nanoTime();

            if (currentTime - startTime >= FUSE_TIME) {
                explode(entities);
            }
        }
    }

    // Kích hoạt bom ngay lập tức khi bị chain reaction
    public void explodeNow(List<Entity> entities) {

        if (!exploded) {
            explode(entities);
        }
    }

    private void explode(List<Entity> entities) {

        if (exploded) {
            return;
        }

        exploded = true;
        explosionTime = System.nanoTime();

        int tileSize = GameConfig.TILE_SIZE;

        // Lửa tại vị trí bom
        flameAreas.add(
                new Rectangle2D(x, y, tileSize, tileSize)
        );

        int[][] directions = {
                {-1, 0},
                {1, 0},
                {0, -1},
                {0, 1}
        };

        List<Entity> bricksToRemove = new ArrayList<>();

        for (int[] direction : directions) {

            int dx = direction[0];
            int dy = direction[1];

            for (int step = 1; step <= explosionRange; step++) {

                int flameX = x + dx * tileSize * step;
                int flameY = y + dy * tileSize * step;

                Rectangle2D flameBounds = new Rectangle2D(
                        flameX,
                        flameY,
                        tileSize,
                        tileSize
                );

                boolean blocked = false;

                for (Entity entity : entities) {

                    if (!entity.isAlive()) {
                        continue;
                    }

                    // Tường chặn lửa
                    if (entity instanceof Wall
                            && flameBounds.intersects(entity.getBoundary())) {

                        blocked = true;
                        break;
                    }

                    // Gạch bị phá
                    if (entity instanceof Brick
                            && flameBounds.intersects(entity.getBoundary())) {

                        flameAreas.add(flameBounds);
                        bricksToRemove.add(entity);
                        destroyedBrickCount++;

                        // 50% cơ hội rơi vật phẩm
                        if (random.nextDouble() < 0.5) {

                            Item.ItemType[] types = Item.ItemType.values();

                            Item.ItemType type =
                                    types[random.nextInt(types.length)];

                            droppedItems.add(
                                    new Item(flameX, flameY, type)
                            );
                        }

                        blocked = true;
                        break;
                    }
                }

                if (blocked) {
                    break;
                }

                flameAreas.add(flameBounds);
            }
        }

        // Xóa gạch sau khi duyệt xong
        entities.removeAll(bricksToRemove);
    }

    // Lấy số gạch đã phá và reset bộ đếm
    public int takeDestroyedBrickCount() {

        int result = destroyedBrickCount;
        destroyedBrickCount = 0;

        return result;
    }

    // Lấy vật phẩm bom làm rơi
    public List<Item> takeDroppedItems() {

        List<Item> result = new ArrayList<>(droppedItems);

        droppedItems.clear();

        return result;
    }

    public boolean hits(Rectangle2D target) {

        if (!exploded) {
            return false;
        }

        for (Rectangle2D flame : flameAreas) {

            if (flame.intersects(target)) {
                return true;
            }
        }

        return false;
    }

    public boolean isFinished() {

        if (!exploded) {
            return false;
        }

        return System.nanoTime() - explosionTime >= FLAME_TIME;
    }

    public boolean isExploded() {
        return exploded;
    }

    @Override
    public void update() {
    }

   
   @Override
public void render(GraphicsContext gc) {

    if (!exploded) {

        // Vẽ ảnh quả bom
        gc.drawImage(
                GameAssets.BOMB,
                x - 4,
                y - 4,
                40,
                40
        );

    } else {

        double size = 40;
        double offset = (GameConfig.TILE_SIZE - size) / 2.0;

        for (Rectangle2D flame : flameAreas) {

            double flameX = flame.getMinX();
            double flameY = flame.getMinY();

            // Tâm vụ nổ
            if (flameX == x && flameY == y) {

                gc.drawImage(
                        GameAssets.EXPLOSION_CENTER,
                        flameX + offset,
                        flameY + offset,
                        size,
                        size
                );

            }

            // Lửa theo chiều ngang
            else if (flameY == y) {

                gc.drawImage(
                        GameAssets.EXPLOSION_HORIZONTAL,
                        flameX + offset,
                        flameY + offset,
                        size,
                        size
                );

            }

            // Lửa theo chiều dọc
            else if (flameX == x) {

                gc.drawImage(
                        GameAssets.EXPLOSION_VERTICAL,
                        flameX + offset,
                        flameY + offset,
                        size,
                        size
                );

            }
        }

        // Vẽ phần cuối của tia lửa theo 4 hướng
        drawFlameEnd(gc, -1, 0, GameAssets.EXPLOSION_HORIZONTAL);
        drawFlameEnd(gc, 1, 0, GameAssets.EXPLOSION_HORIZONTAL);
        drawFlameEnd(gc, 0, -1, GameAssets.EXPLOSION_VERTICAL);
        drawFlameEnd(gc, 0, 1, GameAssets.EXPLOSION_VERTICAL);
    }
}

private void drawFlameEnd(
        GraphicsContext gc,
        int dx,
        int dy,
        javafx.scene.image.Image image) {

    Rectangle2D lastFlame = null;

    for (Rectangle2D flame : flameAreas) {

        if (dx != 0 && flame.getMinY() == y) {

            if (lastFlame == null ||
                    (dx < 0
                    ? flame.getMinX() < lastFlame.getMinX()
                    : flame.getMinX() > lastFlame.getMinX())) {

                lastFlame = flame;
            }
        }

        if (dy != 0 && flame.getMinX() == x) {

            if (lastFlame == null ||
                    (dy < 0
                    ? flame.getMinY() < lastFlame.getMinY()
                    : flame.getMinY() > lastFlame.getMinY())) {

                lastFlame = flame;
            }
        }
    }

    if (lastFlame != null) {

        double size = 40;
        double offset = (GameConfig.TILE_SIZE - size) / 2.0;

        gc.drawImage(
                GameAssets.EXPLOSION_END,
                lastFlame.getMinX() + offset,
                lastFlame.getMinY() + offset,
                size,
                size
        );
    }
}
}
