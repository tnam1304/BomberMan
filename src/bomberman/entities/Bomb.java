
package bomberman.entities;

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

            gc.setFill(Color.BLACK);
            gc.fillOval(x + 4, y + 4, 24, 24);

            gc.setFill(Color.ORANGE);
            gc.fillOval(x + 14, y, 5, 7);

        } else {

            // Hiệu ứng đổi màu lửa
            boolean bright =
                    (System.nanoTime() / 100_000_000L) % 2 == 0;

            for (Rectangle2D flame : flameAreas) {

                gc.setFill(
                        bright ? Color.ORANGERED : Color.ORANGE
                );

                gc.fillRect(
                        flame.getMinX(),
                        flame.getMinY(),
                        flame.getWidth(),
                        flame.getHeight()
                );

                gc.setFill(
                        bright ? Color.YELLOW : Color.GOLD
                );

                gc.fillRect(
                        flame.getMinX() + 5,
                        flame.getMinY() + 5,
                        flame.getWidth() - 10,
                        flame.getHeight() - 10
                );
            }
        }
    }
}
