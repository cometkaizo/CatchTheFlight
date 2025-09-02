package com.cometkaizo.world;

import com.cometkaizo.Main;
import com.cometkaizo.game.Game;
import com.cometkaizo.io.DataSerializable;
import com.cometkaizo.io.data.CompoundData;
import com.cometkaizo.screen.Assets;
import com.cometkaizo.screen.Canvas;
import com.cometkaizo.screen.Renderable;
import com.cometkaizo.util.CollectionUtils;
import com.cometkaizo.util.MathUtils;
import com.cometkaizo.world.block.Block;
import com.cometkaizo.world.block.BlockTypes;
import com.cometkaizo.world.entity.*;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.cometkaizo.util.MathUtils.almostEquals;
import static java.lang.Math.*;

public class Room implements Tickable, Renderable, Resettable {

    public static final String SAVE_EXTENSION = ".csv";
    private static final double BLIP_AROUND_AMT = 0.4;
    public final Game game;
    public String namespace;
    public final World world;
    public String name;
    public ConnectionSet connectionSet = new ConnectionSet(null, null, null, null);
    public List<Checkpoint> checkpoints;
    public List<CameraLock> cameraLocks;
    public List<Trigger> triggers;
    public Player player;

    public Layer ground, walls, background, foreground;

    public Room(Game game, World world, Path path) throws IOException {
        this.game = game;
        this.world = world;
        this.name = path.getFileName().toString();
        this.namespace = name;

        this.ground = new Layer("ground", Main.getResource(path.resolve("ground" + SAVE_EXTENSION).toString()));
        this.walls = new Layer("walls", Main.getResource(path.resolve("walls" + SAVE_EXTENSION).toString()));
        this.background = new Layer("background", Main.getResource(path.resolve("background" + SAVE_EXTENSION).toString()));
        this.foreground = new Layer("foreground", Main.getResource(path.resolve("foreground" + SAVE_EXTENSION).toString()));

        checkpoints = walls.checkpoints;
        cameraLocks = background.cameraLocks;
        triggers = walls.triggers;
    }

    void onAddedTo(World world) {
    }

    public Connection getConnection(Direction direction) {
        return connectionSet.get(direction);
    }

    public void lockCamera(Vector.MutableDouble cameraPos) {
        var closestLockPos = CollectionUtils.findMin(cameraLocks, l ->
                l.isActive(player.getPosition()) ? Double.MIN_VALUE : // if force activate, return minimal value to override all other camera locks
                        l.restrict(cameraPos).distanceSqr(player.getPosition())
        );
        cameraPos.set(closestLockPos.restrict(cameraPos));
    }


    @Override
    public void tick() {
        if (player != null) player.tick(); // this must be first in order for certain movement things to work (e.g., moving platform ground motion)
        ground.tick();
        walls.tick();
        background.tick();
        foreground.tick();
    }

    @Override
    public void render(Canvas canvas) {
        background.render(canvas);
        ground.render(canvas);
        walls.render(canvas);
        if (player != null) player.render(canvas);
        foreground.render(canvas);
    }

    public Object getBlockOrEntity(String name) {
        Object result;
        if ((result = ground.getBlockOrEntity(name)) != null) return result;
        if ((result = walls.getBlockOrEntity(name)) != null) return result;
        if ((result = background.getBlockOrEntity(name)) != null) return result;
        if ((result = foreground.getBlockOrEntity(name)) != null) return result;
        return null;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getName() {
        return name;
    }

    public List<Checkpoint> getCheckpoints() {
        return checkpoints;
    }
    public Checkpoint getFirstCheckpoint() {
        return (Checkpoint) walls.named.get("first");
    }

    @Override
    public void reset() {
        if (player != null) player.reset();
        ground.reset();
        walls.reset();
        background.reset();
        foreground.reset();
    }

    public List<Block> getGroundBeneath(CollidableEntity entity) {
        return ground.getBlocksWithin(entity.getBoundingBox(), b -> b.isSolid(entity));
    }


    public record Atmosphere(Color darknessColor) {
        public static final String DARKNESS_COLOR_KEY = "darknessColor";

        public static Atmosphere of(String data) {
            CompoundData compoundData = new CompoundData();
            String[] parts = data.split(",");

            int r = Integer.parseInt(parts[0]);
            int g = Integer.parseInt(parts[1]);
            int b = Integer.parseInt(parts[2]);
            int a = Integer.parseInt(parts[3]);
            compoundData.putInt(DARKNESS_COLOR_KEY, new Color(r, g, b, a).getRGB());

            return Atmosphere.of(compoundData);
        }

        public CompoundData write() {
            CompoundData data = new CompoundData();
            data.putInt(DARKNESS_COLOR_KEY, darknessColor.getRGB());
            return data;
        }

        public static Atmosphere of(CompoundData data) {
            Color darknessColor = new Color(data.getInt(DARKNESS_COLOR_KEY), true);
            return new Atmosphere(darknessColor);
        }
    }

    public static class ConnectionSet {
        private final Map<Direction, Connection> connections = new HashMap<>(4);
        public ConnectionSet(Connection upConnection,
                             Connection downConnection,
                             Connection leftConnection,
                             Connection rightConnection) {
            if (upConnection != null) connections.put(Direction.UP, upConnection);
            if (downConnection != null) connections.put(Direction.DOWN, downConnection);
            if (leftConnection != null) connections.put(Direction.LEFT, leftConnection);
            if (rightConnection != null) connections.put(Direction.RIGHT, rightConnection);
        }
        private ConnectionSet(Map<Direction, Connection> connections) {
            this.connections.putAll(connections);
        }

        public static ConnectionSet of(CompoundData data, Map<String, Room> rooms) {
            Map<Direction, Connection> connections = new HashMap<>(4);

            for (String key : data.asMap().keySet()) {
                var direction = Direction.valueOf(key);
                var connection = Connection.of(data.getCompound(key), rooms);
                connections.put(direction, connection);
            }
            return new ConnectionSet(connections);
        }

        public static ConnectionSet of(String data, Map<String, Room> rooms) {
            CompoundData compound = new CompoundData();

            Stream<String> lines = data.lines();
            for (String line : lines.toList()) {
                String[] parts = line.split(",");

                String direction = parts[0];
                int start = Integer.parseInt(parts[1]);
                int length = Integer.parseInt(parts[2]);
                String destination = parts[3];

                compound.put(direction, new Connection(start, length, destination, rooms).write());
            }

            return of(compound, rooms);
        }

        public Connection get(Direction direction) {
            return connections.get(direction);
        }

        public CompoundData write() {
            CompoundData data = new CompoundData();
            for (Direction key : connections.keySet()) {
                var respawnPos = connections.get(key);
                if (respawnPos != null) data.put(key.name(), respawnPos.write());
            }
            return data;
        }

    }

    public record Connection(int start, int length, Supplier<Room> destination) {
        public static final String DESTINATION_KEY = "destination";
        public static final String START_KEY = "start";
        public static final String LENGTH_KEY = "length";

        public Connection(int start, int length, String destination, Map<String, Room> rooms) {
            this(start, length, () -> getRoom(destination, rooms));
        }

        private static Room getRoom(String namespace, Map<String, Room> rooms) {
            Room room = rooms.get(namespace);
            if (room == null) throw new NoSuchElementException("Unknown room with namespace '" + namespace + "'; available rooms are: " + rooms);
            return room;
        }

        public CompoundData write() {
            CompoundData data = new CompoundData();
            data.putString(DESTINATION_KEY, destination.get().getNamespace());
            data.putInt(START_KEY, start);
            data.putInt(LENGTH_KEY, length);
            return data;
        }

        public static Connection of(CompoundData data, Map<String, Room> rooms) {
            String destination = data.getString(DESTINATION_KEY);
            int start = data.getInt(START_KEY);
            int length = data.getInt(LENGTH_KEY);

            return new Connection(start, length, destination, rooms);
        }
    }

    public static class RespawnSet implements DataSerializable {
        private final Map<Direction, Vector.ImmutableDouble> respawnPositions = new HashMap<>(4);
        public RespawnSet(Vector.ImmutableDouble upRespawnPos,
                          Vector.ImmutableDouble downRespawnPos,
                          Vector.ImmutableDouble leftRespawnPos,
                          Vector.ImmutableDouble rightRespawnPos) {
            respawnPositions.put(Direction.UP, upRespawnPos);
            respawnPositions.put(Direction.DOWN, downRespawnPos);
            respawnPositions.put(Direction.LEFT, leftRespawnPos);
            respawnPositions.put(Direction.RIGHT, rightRespawnPos);
        }
        public RespawnSet(CompoundData data) {
            read(data);
        }

        public static RespawnSet of(String data) {
            CompoundData compound = new CompoundData();

            Stream<String> lines = data.lines();
            for (String line : lines.toList()) {
                String[] parts = line.split(",");

                String direction = parts[0];
                double x = Double.parseDouble(parts[1]);
                double y = Double.parseDouble(parts[2]);
                compound.put(direction, Vector.immutable(x, y).write());
            }

            return new RespawnSet(compound);
        }

        public Vector.ImmutableDouble get(Direction lastEntrySide) {
            return respawnPositions.get(lastEntrySide);
        }

        @Override
        public CompoundData write() {
            CompoundData data = new CompoundData();
            for (Direction key : respawnPositions.keySet()) {
                var respawnPos = respawnPositions.get(key);
                if (respawnPos != null) data.put(key.name(), respawnPos.write());
            }
            return data;
        }

        @Override
        public void read(CompoundData data) {
            Map<Direction, Vector.ImmutableDouble> respawnPositions = new HashMap<>(4);
            for (String key : data.asMap().keySet()) {
                var direction = Direction.valueOf(key);
                var respawnPos = Vector.immutableDouble(data.getCompound(key));
                respawnPositions.put(direction, respawnPos);
            }
            this.respawnPositions.clear();
            this.respawnPositions.putAll(respawnPositions);
        }
    }

    public class Layer implements Tickable, Renderable, Resettable {
        public static final String RESPAWN_ID = "R", CAMERA_LOCK_ID = "CL", TRIGGER_ID = "T";
        public final Room room = Room.this;
        public final Block[][] blocks;
        public final List<Entity> entities = new ArrayList<>();
        public final Map<String, Object> named = new HashMap<>();
        public final List<Checkpoint> checkpoints;
        public final List<CameraLock> cameraLocks;
        public final List<Trigger> triggers;
        public final String name;
        public final Image baseImage;

        public Layer(String name, InputStream is) throws IOException {
            this.name = name;
            baseImage = Assets.texture("layer/" + name);

            var in = new BufferedReader(new InputStreamReader(is));
            var lines = in.lines().toList().reversed(); // reverse y

            var blocks = new ArrayList<List<Block>>();
            checkpoints = new ArrayList<>();
            cameraLocks = new ArrayList<>();
            triggers = new ArrayList<>();

            {
                for (int r = 0; r < lines.size(); r ++) {
                    var line = lines.get(r);
                    var row = new ArrayList<Block>();

                    int c = -1;
                    String[] split = line.split(",", -1);
                    for (String s : split) {
                        c ++;
                        Args args = new Args(s);
                        String id = args.id();

                        // special values
                        if (RESPAWN_ID.equals(id)) {
                            var checkpoint = new Checkpoint(r, c, args.nextInt(0), args.nextInt(0), args.nextInt(4), args.nextInt(4), args.next());
                            checkpoints.add(checkpoint);
                            if (!checkpoint.name.isEmpty()) named.put(checkpoint.name, checkpoint);
                            row.add(BlockTypes.BLOCKS.get("").apply(this, Vector.immutable(c, r), Args.EMPTY));
                            continue;
                        } else if (CAMERA_LOCK_ID.equals(id)) {
                            cameraLocks.add(CameraLock.of(args, r, c));
                            row.add(BlockTypes.BLOCKS.get("").apply(this, Vector.immutable(c, r), Args.EMPTY));
                            continue;
                        } else if (TRIGGER_ID.equals(id)) {
                            triggers.add(new Trigger(r, c, args.nextInt(0), args.nextInt(0), args.nextInt(4), args.nextInt(4), args.nextInt(-1)));
                            row.add(BlockTypes.BLOCKS.get("").apply(this, Vector.immutable(c, r), Args.EMPTY));
                            continue;
                        }

                        // blocks and entities
                        if (BlockTypes.BLOCKS.containsKey(id)) {
                            var b = BlockTypes.BLOCKS.get(id).apply(this, Vector.immutable(c, r), args);
                            row.add(b);
                            if (b.hasName()) named.put(b.getName(), b);
                        } else {
                            row.add(BlockTypes.BLOCKS.get("").apply(this, Vector.immutable(c, r), Args.EMPTY));

                            if (EntityTypes.ENTITIES.containsKey(id)) {
                                var e = EntityTypes.ENTITIES.get(id).apply(this, Vector.mutable((double) c, r), args);
                                entities.add(e);
                                if (e.hasName()) named.put(e.getName(), e);
                            } else {
                                throw new IllegalArgumentException("No such block or entity: " + id + " at (" + (lines.size() - r) + ":" + (c + 1) + ") (r:c, not ctrl + g)");
                            }
                        }
                    }

                    blocks.add(row);
                }
            }

            this.blocks = new Block[blocks.size()][];
            for (int r = 0; r < blocks.size(); r++) {
                var row = blocks.get(r);
                this.blocks[r] = row.toArray(Block[]::new);
            }

            in.close();
        }

        public void calcAllowedMovement(Vector.Double from, Vector.Double to, CollidableEntity entity, Vector.MutableDouble result, boolean canBlip) {
            if (entity == null) return;
            var boundingBox = entity.getBoundingBox();
            if (boundingBox == null) return;

            double bbOffsetX = from.getX() - boundingBox.getX();
            double bbOffsetY = from.getY() - boundingBox.getY();

            boolean xAxisAligned = almostEquals(from.getY(), to.getY());
            boolean yAxisAligned = almostEquals(from.getX(), to.getX());
            boolean axisAligned = xAxisAligned || yAxisAligned;

//            if (!axisAligned) {
                calcAllowedOnlyYMovement(from, to, entity, result, canBlip && yAxisAligned);
                boundingBox.position.y = result.y - bbOffsetY;
                calcAllowedOnlyXMovement(from, to, entity, result, canBlip && xAxisAligned);
                boundingBox.position.x = result.x - bbOffsetX;
//            } else if (xAxisAligned) {
//                calcAllowedOnlyXMovement(from, to, entity, result, canBlip);
//            } else if (yAxisAligned) {
//                calcAllowedOnlyYMovement(from, to, entity, result, canBlip);
//            }
//            boundingBox.position.x = result.x - bbOffsetX;
//            boundingBox.position.y = result.y - bbOffsetY;
        }

        private void calcAllowedOnlyYMovement(Vector.Double f, Vector.Double t, CollidableEntity entity, Vector.MutableDouble result, boolean canBlip) {
            double from = f.getY(), to = t.getY();
            var boundingBox = entity.getBoundingBox();
            int direction = (int) signum(to - from);
            boolean isMovingUp = direction == 1;
            double originalBoundingBoxY = boundingBox.getY();
            double bbOffsetY = from - originalBoundingBoxY;
            double bbOffsetX = f.getX() - boundingBox.getX();

            for (int y = (int) from; y != (int) to + direction; y += direction) {
                boundingBox.position.y = y != (int) to ?
                        (y - (int) from) + originalBoundingBoxY :
                        to - bbOffsetY;

                var solidBlocks = getBlocksWithin(boundingBox, block -> block.isSolid(entity));
                var solidEntities = getEntitiesWithin(boundingBox, e -> e != entity && e instanceof CollidableEntity c && c.isSolid(entity));
                if (!solidBlocks.isEmpty() || !solidEntities.isEmpty()) {
                    boundingBox.position.y = result.y = getTruncatedYMovement(boundingBox, solidBlocks, solidEntities, isMovingUp, bbOffsetY);
                    if (canBlip) { // try blip
                        if (solidBlocks.size() + solidEntities.size() == 1) {
                            double xBeforeBlip = result.x;
                            double left, right;
                            if (!solidBlocks.isEmpty()) {
                                left = solidBlocks.getFirst().getX();
                                right = left + 1;
                            } else {
                                var c = (CollidableEntity) solidEntities.getFirst();
                                left = c.getBoundingBox().getLeft();
                                right = c.getBoundingBox().getRight();
                            }
                            if (boundingBox.getRight() - left < BLIP_AROUND_AMT) result.x -= boundingBox.getRight() - left;
                            else if (right - boundingBox.getLeft() < BLIP_AROUND_AMT) result.x += right - boundingBox.getLeft();
                            else return;
                            if (containsSolid(boundingBox, entity)) {
                                result.x = xBeforeBlip;
                                return;
                            }
                            boundingBox.position.x = result.x - bbOffsetX;
                        } else return;
                    } else return;
                }
            }

            result.y = to;
        }

        private void calcAllowedOnlyXMovement(Vector.Double f, Vector.Double t, CollidableEntity entity, Vector.MutableDouble result, boolean canBlip) {
            double from = f.getX(), to = t.getX();
            var boundingBox = entity.getBoundingBox();
            int direction = (int) signum(to - from);
            boolean isMovingRight = direction == 1;
            double originalBoundingBoxX = boundingBox.getX();
            double bbOffsetX = from - originalBoundingBoxX;
            double bbOffsetY = f.getY() - boundingBox.getY();

            for (int x = (int) from; x != (int) to + direction; x += direction) {
                boundingBox.position.x = x != (int) to ?
                        (x - (int) from) + originalBoundingBoxX :
                        to - bbOffsetX;

                var solidBlocks = getBlocksWithin(boundingBox, block -> block.isSolid(entity));
                var solidEntities = getEntitiesWithin(boundingBox, e -> e != entity && e instanceof CollidableEntity c && c.isSolid(entity));
                if (!solidBlocks.isEmpty() || !solidEntities.isEmpty()) {
                    result.x = getTruncatedXMovement(boundingBox, solidBlocks, solidEntities, isMovingRight, bbOffsetX);
                    boundingBox.position.x = result.x - bbOffsetX;
                    if (canBlip) { // try blip
                        if (solidBlocks.size() + solidEntities.size() == 1) {
                            double yBeforeBlip = result.y;
                            double top, bottom;
                            if (!solidBlocks.isEmpty()) {
                                bottom = solidBlocks.getFirst().getY();
                                top = bottom + 1;
                            } else {
                                var c = (CollidableEntity) solidEntities.getFirst();
                                bottom = c.getBoundingBox().getBottom();
                                top = c.getBoundingBox().getTop();
                            }
                            if (top - boundingBox.getBottom() < BLIP_AROUND_AMT) result.y += top - boundingBox.getBottom();
                            else if (boundingBox.getTop() - bottom < BLIP_AROUND_AMT) result.y -= boundingBox.getTop() - bottom;
                            else return;
                            if (containsSolid(boundingBox, entity)) {
                                result.y = yBeforeBlip;
                                return;
                            }
                            boundingBox.position.y = result.y - bbOffsetY;
                        } else return;
                    } else return;
                }
            }

            result.x = to;
        }

        /*private double calcAllowedYMovement(double from, double to, CollidableEntity entity) {
            var boundingBox = entity.getBoundingBox();
            int direction = (int) signum(to - from);
            boolean isMovingUp = direction == 1;
            double originalBoundingBoxY = boundingBox.getY();
            double bbOffset = from - originalBoundingBoxY;

            for (int y = (int) from; y != (int) to + direction; y += direction) {
                boundingBox.position.y = y != (int) to ?
                        (y - (int) from) + originalBoundingBoxY :
                        to - bbOffset;

                var solidBlocks = getBlocksWithin(boundingBox, b -> b.isSolid(entity));
                var solidEntities = getEntitiesWithin(boundingBox, e -> e != entity && e instanceof CollidableEntity c && c.isSolid(entity));
                if (!solidBlocks.isEmpty() || !solidEntities.isEmpty()) {
                    boundingBox.position.y = originalBoundingBoxY;
                    return getTruncatedYMovement(boundingBox, solidBlocks, solidEntities, isMovingUp, bbOffset);
                }
            }

            boundingBox.position.y = originalBoundingBoxY;
            return to;
        }

        private double calcAllowedXMovement(double from, double to, CollidableEntity entity) {
            var boundingBox = entity.getBoundingBox();
            int direction = (int) signum(to - from);
            boolean isMovingRight = direction == 1;
            double originalBoundingBoxX = boundingBox.getX();
            double bbOffset = from - originalBoundingBoxX;

            for (int x = (int) from; x != (int) to + direction; x += direction) {
                boundingBox.position.x = x != (int) to ?
                        (x - (int) from) + originalBoundingBoxX :
                        to - bbOffset;

                var solidBlocks = getBlocksWithin(boundingBox, block -> block.isSolid(entity));
                var solidEntities = getEntitiesWithin(boundingBox, e -> e != entity && e instanceof CollidableEntity c && c.isSolid(entity));
                if (!solidBlocks.isEmpty() || !solidEntities.isEmpty()) {
                    boundingBox.position.x = originalBoundingBoxX;
                    return getTruncatedXMovement(boundingBox, solidBlocks, solidEntities, isMovingRight, bbOffset);
                }
            }

            boundingBox.position.x = originalBoundingBoxX;
            return to;
        }*/

        private static double getTruncatedYMovement(BoundingBox boundingBox, List<Block> solidBlocks, List<Entity> solidEntities, boolean truncateUnder, double bbOffset) {
            if (solidBlocks.isEmpty() && solidEntities.isEmpty()) return boundingBox.position.y;
            if (truncateUnder) {
                double result = Double.MAX_VALUE;
                var bottomMostBlock = CollectionUtils.findMin(solidBlocks, Block::getY);
                if (bottomMostBlock != null) result = min(result, bottomMostBlock.getY() - bbOffset);
                var bottomMostEntity = (CollidableEntity) CollectionUtils.findMin(solidEntities, e -> ((CollidableEntity) e).getBoundingBox().getBottom());
                if (bottomMostEntity != null) result = min(result, bottomMostEntity.getBoundingBox().getBottom());
                return result - boundingBox.getHeight();
            } else {
                double result = -Double.MAX_VALUE;
                Block topMostBlock = CollectionUtils.findMax(solidBlocks, Block::getY);
                if (topMostBlock != null) result = max(result, topMostBlock.getY() + 1 + bbOffset);
                var topMostEntity = (CollidableEntity) CollectionUtils.findMin(solidEntities, e -> ((CollidableEntity) e).getBoundingBox().getTop());
                if (topMostEntity != null) result = max(result, topMostEntity.getBoundingBox().getTop());
                return result;
            }
        }

        private static double getTruncatedXMovement(BoundingBox boundingBox, List<Block> solidBlocks, List<Entity> solidEntities, boolean truncateToLeft, double bbOffset) {
            if (solidBlocks.isEmpty() && solidEntities.isEmpty()) return boundingBox.position.x;
            if (truncateToLeft) {
                double result = Double.MAX_VALUE;
                var leftMostBlock = CollectionUtils.findMin(solidBlocks, Block::getX);
                if (leftMostBlock != null) result = min(result, leftMostBlock.getX() - boundingBox.getWidth() + bbOffset);
                var leftMostEntity = (CollidableEntity) CollectionUtils.findMin(solidEntities, e -> ((CollidableEntity) e).getBoundingBox().getLeft());
                if (leftMostEntity != null) result = min(result, leftMostEntity.getBoundingBox().getLeft() - bbOffset);
                return result;
            } else {
                double result = -Double.MAX_VALUE;
                Block rightMostBlock = CollectionUtils.findMax(solidBlocks, Block::getX);
                if (rightMostBlock != null) result = max(result, rightMostBlock.getX() + 1 + bbOffset);
                var rightMostEntity = (CollidableEntity) CollectionUtils.findMin(solidEntities, e -> ((CollidableEntity) e).getBoundingBox().getRight());
                if (rightMostEntity != null) result = max(result, rightMostEntity.getBoundingBox().getRight() + bbOffset);
                return result;
            }
//                Block leftMostBlock = CollectionUtils.findMin(solidBlocks, Block::getX);
//                return leftMostBlock.getX() - boundingBox.getWidth() + bbOffset;
//            } else {
//                Block rightMostBlock = CollectionUtils.findMax(solidBlocks, Block::getX);
//                return rightMostBlock.getX() + 1 + bbOffset;
//            }
        }

        public List<Block> getBlocksWithin(BoundingBox boundingBox) {
            return getBlocksWithin(boundingBox, b -> true);
        }

        public List<Block> getBlocksWithin(BoundingBox boundingBox, Predicate<? super Block> condition) {
            int fromX = (int) floor(boundingBox.getLeft());
            int fromY = (int) floor(boundingBox.getBottom());
            int toX = (int) floor(boundingBox.getRight() - 10E-5);
            int toY = (int) floor(boundingBox.getTop() - 10E-5);

            List<Block> result = new ArrayList<>((abs(toX - fromX) + 1) * (abs(toY - fromY) + 1));

            for (int y = fromY; y <= toY; y ++) {
                for (int x = fromX; x <= toX; x ++) {
                    getBlock(x, y).ifPresent(block -> {
                        if (condition.test(block)) result.add(block);
                    });
                }
            }

            return result;
        }

        public List<Entity> getEntitiesWithin(BoundingBox boundingBox) {
            return getEntitiesWithin(boundingBox, b -> true);
        }

        public List<Entity> getEntitiesWithin(BoundingBox boundingBox, Predicate<? super Entity> condition) {
            var result = new ArrayList<Entity>();
            for (var e : entities) {
                if (!condition.test(e)) continue;
                if (boundingBox.contains(e.getPosition()) ||
                        e instanceof CollidableEntity c && c.getBoundingBox().intersects(boundingBox)) {
                    result.add(e);
                }
            }
            return result;
        }

        public Optional<Block> getBlock(Vector.Int position) {
            return getBlock(position.getX(), position.getY());
        }

        public Optional<Block> getBlock(int x, int y) {
            if (y < 0 || y >= blocks.length) return Optional.empty();
            var row = blocks[y];
            if (x < 0 || x >= row.length) return Optional.empty();
            return Optional.of(row[x]);
        }

        public boolean containsSolid(BoundingBox boundingBox, Entity entity) {
            return !getBlocksWithin(boundingBox, block -> block.isSolid(entity)).isEmpty() ||
                    !getEntitiesWithin(boundingBox, e -> e instanceof CollidableEntity c && c.isSolid(entity)).isEmpty();
        }

        public Object getBlockOrEntity(String name) {
            return name == null ? null : named.get(name);
        }

        @Override
        public void tick() {
            entities.forEach(Tickable::tick);
            for (var row : blocks) for (var b : row) b.tick();
        }

        @Override
        public void render(Canvas canvas) {
            canvas.renderImage(baseImage, -14D, -10D, 0, -1);
            for (int r = blocks.length - 1; r >= 0; r--) {
                for (var b : blocks[r]) b.render(canvas);
            }
            entities.forEach((entity) -> entity.render(canvas));
        }

        public void reset() {
            entities.forEach(Entity::reset);
            for (var row : blocks) for (var b : row) b.reset();
        }

        public <T extends Entity> T addEntity(T entity) {
            entities.add(entity);
            if (entity.hasName()) named.put(entity.getName(), entity);
            return entity;
        }

        public class Renderer implements Renderable, DataSerializable {
            public static final String OPTIONS_KEY = "darknessColor";
            private final Rectangle2D screen = new Rectangle2D.Float();
            private boolean emittedLight;
            public Area darkness;
            public Player player;
            public List<Entity> entities = new ArrayList<>(1);
            public Options options;

            public Renderer(Options options) {
                this.options = options;
            }
            public Renderer(CompoundData data) {
                read(data);
            }

            private void addEntity(Entity entity) {
                if (entity instanceof Player p) player = p;
                else entities.add(entity);
            }
            private void removeEntity(Entity entity) {
                if (entity == player) player = null;
                else entities.remove(entity);
            }

            public void emitLight(Vector.Double position, double radius, Canvas canvas) {
                emitLight(position.getX(), position.getY(), radius, canvas);
            }

            public void emitLight(double x, double y, double radius, Canvas canvas) {
                if (darkness != null) {
                    darkness.subtract(getLightArea(x, y, radius, canvas));
                    emittedLight = true;
                }
            }

            private Area getLightArea(double x, double y, double radius, Canvas canvas) {
                double screenX = canvas.toScreenX(x - radius);
                double screenY = canvas.toScreenY(y + radius);
                int screenDiameter = canvas.toScreenLength(radius * 2);
                return new Area(new Ellipse2D.Float((float) screenX, (float) screenY, screenDiameter, screenDiameter));
            }

            @Override
            public void render(Canvas canvas) {
                for (var row : blocks) for (var b : row) b.render(canvas);
                entities.forEach((entity) -> entity.render(canvas));
                player.render(canvas);

                renderDarkness(canvas);
            }

            private void renderDarkness(Canvas canvas) {
                Color darknessColor = options.atmosphere.darknessColor;
                if (darknessColor == null || darknessColor.getAlpha() == 0) return;
                updateScreenSize(canvas);
                if (darkness == null) darkness = new Area(screen);

                canvas.getGraphics().setColor(darknessColor);
                canvas.getGraphics().fill(darkness);

                if (emittedLight) darkness = new Area(screen);
                emittedLight = false;
            }

            private void updateScreenSize(Canvas canvas) {
                screen.setRect(0, 0, canvas.getWidth(), canvas.getHeight());
            }

            @Override
            public CompoundData write() {
                CompoundData data = new CompoundData();
                data.put(OPTIONS_KEY, options.write());
                return data;
            }

            @Override
            public void read(CompoundData data) {
                options = Options.of(data.getCompound(OPTIONS_KEY));
            }

            public record Options(Atmosphere atmosphere) {

                public static final String ATMOSPHERE_KEY = "atmosphere";

                public static Options of(String data) {
                    return new Options(Atmosphere.of(data));
                }

                public CompoundData write() {
                    CompoundData data = new CompoundData();
                    data.put(ATMOSPHERE_KEY, atmosphere.write());
                    return data;
                }

                public static Options of(CompoundData data) {
                    Atmosphere atmosphere = Atmosphere.of(data.getCompound(ATMOSPHERE_KEY));
                    return new Options(atmosphere);
                }
            }
        }
    }

    public record Checkpoint(Vector.ImmutableDouble pos, BoundingBox activationArea, String name) {
        public Checkpoint(int r, int c, int left, int right, int up, int down, String name) {
            // its mutable and I don't think records are supposed to be mutable but whatever
            this(Vector.immutable(c + 0.5, r), new BoundingBox(Vector.mutable((double) c - left, r - down), Vector.immutable((double) left + 1 + right, up + 1 + down)), name);
        }
    }

    public record Trigger(BoundingBox activationArea, int id) {
        public static final int LUGGAGE_CP0 = 0, WIN = 1; // cp0 haha
        public Trigger(int r, int c, int left, int right, int up, int down, int id) {
            // its mutable and I don't think records are supposed to be mutable but whatever
            this(new BoundingBox(Vector.mutable((double) c - left, r - down), Vector.immutable((double) left + 1 + right, up + 1 + down)), id);
        }
        public void activate(Player player) { // should this be called the other way, where Trigger is ticked?
            switch (id) {
                case LUGGAGE_CP0 -> luggageCP0(player);
                case WIN -> win(player);
                default -> throw new IllegalStateException("Unexpected trigger id: " + id);
            }
        }

        private void luggageCP0(Player player) {
            if (!player.isHolding()) return;
            player.setPosition(player.getRoom().getFirstCheckpoint().pos());
            player.getGame().teleportCamera();
            player.getGame().hasLuggage = true;
            Assets.sound("bump").play();
        }

        private void win(Player player) {
            if (!player.isHolding()) return;
            player.getGame().end();
        }
    }

    public abstract static class CameraLock {
        protected final BoundingBox activationArea;

        protected CameraLock(int x, int y, int left, int right, int up, int down) {
            this.activationArea = new BoundingBox(Vector.mutable((double) x - left, y - down), Vector.immutable((double) left + 1 + right, up + 1 + down));
        }

        public abstract Vector.ImmutableDouble restrict(Vector.Double pos);
        public boolean isActive(Vector.Double pos) {
            return activationArea.contains(pos);
        }

        public static CameraLock of(Args args, int r, int c) {
            return switch (args.next()) {
                case "p" -> new Point(c, r, args.nextInt(0), args.nextInt(0), args.nextInt(0), args.nextInt(0));
                case "h" -> new Horizontal(c, r, args.nextSheetCol(0), args.nextSheetCol(0), args.nextInt(0), args.nextInt(0), args.nextInt(0), args.nextInt(0));
                case "v" -> new Vertical(c, r, args.nextInt(0), args.nextInt(0), args.nextInt(0), args.nextInt(0), args.nextInt(0), args.nextInt(0));
                default -> throw new IllegalStateException("Unexpected value: " + args.next());
            };
        }

        public static class Point extends CameraLock {
            protected final Vector.ImmutableDouble point;
            public Point(int x, int y, int left, int right, int up, int down) {
                super(x, y, left, right, up, down);
                this.point = Vector.immutable((double)x, y);
            }
            @Override
            public Vector.ImmutableDouble restrict(Vector.Double pos) {
                return point;
            }
        }
        public static class Horizontal extends CameraLock {
            protected final double y, leftEndpoint, rightEndpoint;
            public Horizontal(int x, int y, int leftEndpoint, int rightEndpoint, int left, int right, int up, int down) {
                super(x, y, left, right, up, down);
                this.y = y + 0.5;
                this.leftEndpoint = leftEndpoint;
                this.rightEndpoint = rightEndpoint;
            }
            @Override
            public Vector.ImmutableDouble restrict(Vector.Double pos) {
                return Vector.immutable(MathUtils.clamp(pos.getX(), leftEndpoint, rightEndpoint), y);
            }
        }
        public static class Vertical extends CameraLock {
            protected final int x, bottomEndpoint, topEndpoint;
            public Vertical(int x, int y, int bottomEndpoint, int topEndpoint, int left, int right, int up, int down) {
                super(x, y, left, right, up, down);
                this.x = x;
                this.bottomEndpoint = bottomEndpoint;
                this.topEndpoint = topEndpoint;
            }
            @Override
            public Vector.ImmutableDouble restrict(Vector.Double pos) {
                return Vector.immutable(x, MathUtils.clamp(pos.getY(), bottomEndpoint, topEndpoint));
            }
        }
    }
}
