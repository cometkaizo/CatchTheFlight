package com.cometkaizo.world.entity;

import com.cometkaizo.game.event.KeyPressedEvent;
import com.cometkaizo.game.event.MousePressedEvent;
import com.cometkaizo.game.event.PlayerDeathEvent;
import com.cometkaizo.game.event.RoomSwitchEvent;
import com.cometkaizo.input.InputBindings;
import com.cometkaizo.input.KeyBinding;
import com.cometkaizo.input.MouseButtonBinding;
import com.cometkaizo.screen.Canvas;
import com.cometkaizo.util.CollectionUtils;
import com.cometkaizo.world.*;
import com.cometkaizo.world.block.Block;

public class Player extends CollidableEntity {

    public double jumpAccel = 0.62;
    public double walkAccel = 0.03;
    public double diagWalkAccel = walkAccel * Math.cos(Math.toRadians(45));
    public double maxWalkVelocity = 0.07;
    public double maxDiagWalkVelocity = maxWalkVelocity * Math.cos(Math.toRadians(45));
    public double maxDownwardsVelocity = 0.7;
    public double groundFriction = 0.3;
    public double airFriction = 0.5;
    protected ThrowableEntity heldEntity;
    public double throwUpBoost = 3;
    protected boolean interacted;
    protected int deathTime = -1;

    public Player(Room room, Vector.MutableDouble position, Args args) {
        super(room, position, args);
        this.boundingBox = new BoundingBox(Vector.mutable(0D, 0D), Vector.immutable(0.6D, 0.6D));
        game.getEventBus().register(KeyPressedEvent.class, this::onKeyPressed);
        game.getEventBus().register(MousePressedEvent.class, this::onMousePressed);
    }

    @Override
    protected String getTexturePath() {
        return "player";
    }

    public void kill() {
        game.getEventBus().post(new PlayerDeathEvent(this));
    }

    @Override
    public void reset() {
        super.reset();
        deathTime = -1;
    }

    @Override
    public void tick() {
        updateMotion();
        super.tick();
        trySwitchRoom();

        if (isTooFarOffscreen()) kill();
        if (!isOnGround()) {
            deathTime ++;
            if (deathTime >= 80) kill();
        } else deathTime = -1;
        interacted = false;
    }

    private boolean isOnGround() {
        return CollectionUtils.anyMatch(room.ground.getBlocksWithin(boundingBox), Block::isSolid);
    }

    private void updateMotion() {
        motion.x *= isOnGround ? groundFriction : airFriction;
        motion.y *= isOnGround ? groundFriction : airFriction;

        if (deathTime >= 7) return;

        boolean right = InputBindings.RIGHT.get().isDown;
        boolean left = InputBindings.LEFT.get().isDown;
        boolean up = InputBindings.UP.get().isDown;
        boolean down = InputBindings.DOWN.get().isDown;

        boolean diagonal = (right || left) && (up || down);
        double walkAccel = diagonal ? this.diagWalkAccel : this.walkAccel;
        double maxWalkVelocity = diagonal ? this.maxDiagWalkVelocity : this.maxWalkVelocity;

        if (right) {
            motion.x += walkAccel;
        } if (left) {
            motion.x -= walkAccel;
        } if (up) {
            motion.y += walkAccel;
        } if (down) {
            motion.y -= walkAccel;
        }

        motion.x = Math.min(Math.max(motion.x, -maxWalkVelocity), maxWalkVelocity);
        motion.y = Math.min(Math.max(motion.y, -maxWalkVelocity), maxWalkVelocity);
    }

    private boolean isTooFarOffscreen() {
        return boundingBox.getTop() <= game.getCameraPosition().y;
    }

    @Override
    protected void updateBoundingBox() {
        double width = boundingBox.getWidth();
        boundingBox.position.x = position.x - width / 2;
        boundingBox.position.y = position.y;
    }

    private void onKeyPressed(KeyPressedEvent event) {
        KeyBinding input = event.input();
        if (isOnGround && input == InputBindings.JUMP.get()) {
            jump();
        }
    }

    private void onMousePressed(MousePressedEvent event) {
        MouseButtonBinding input = event.input();
        double x = event.x();
        double y = event.y();
        if (heldEntity != null && canThrowHeldEntity() && input == InputBindings.INTERACT.get()) {
            heldEntity.launch(Vector.mutable(x, y).subtract(position).add(0D, throwUpBoost).add(motion));
            heldEntity = null;
        }
    }

    private boolean canThrowHeldEntity() {
        return !interacted;
    }

    public void jump() {
        motion.y = jumpAccel;
    }

    public void respawnAt(Vector.Double position) {
        respawnAt(position.getX(), position.getY());
    }

    public void respawnAt(double x, double y) {
        setPosition(x, y);
        setMotion(0, 0);
    }

    private void trySwitchRoom() {
        double centerX = boundingBox.getCenterX();
        double centerY = boundingBox.getCenterY();

        if (centerX <= 0 || centerX >= room.getWidth()) {
            Direction switchDirection;
            Room.Connection connection;
            if (centerX <= 0) switchDirection = Direction.LEFT;
            else switchDirection = Direction.RIGHT;
            connection = room.getConnection(switchDirection);

            if (connection != null) {
                if (boundingBox.getTop() <= connection.start() + connection.length() &&
                        boundingBox.getBottom() >= connection.start()) {
                    switchToRoom(connection.destination().get(), connection, switchDirection);
                }
            }
        } else if (centerY <= 0 || centerY >= room.getHeight()) {
            Direction switchDirection;
            Room.Connection connection;
            if (centerY <= 0) switchDirection = Direction.DOWN;
            else switchDirection = Direction.UP;
            connection = room.getConnection(switchDirection);

            if (connection != null) {
                if (boundingBox.getRight() <= connection.start() + connection.length() &&
                        boundingBox.getLeft() >= connection.start()) {
                    switchToRoom(connection.destination().get(), connection, switchDirection);
                }
            }
        }
    }

    private void switchToRoom(Room destination, Room.Connection connection, Direction direction) {
        room.player = null;

        setPositionInNewRoom(destination, connection, direction);

        game.getEventBus().post(new RoomSwitchEvent(this, room, destination));
        destination.player = this;
    }

    private void setPositionInNewRoom(Room destination, Room.Connection connection, Direction direction) {
        if (direction.axis() == Axis.X) {
            double deltaY = position.y - connection.start();
            position.y = destination.getConnection(direction.opposite()).start() + deltaY;
        } else {
            double deltaX = position.x - connection.start();
            position.x = destination.getConnection(direction.opposite()).start() + deltaX;
        }
    }

    public ThrowableEntity getHeldEntity() {
        return heldEntity;
    }

    public void setHeldEntity(ThrowableEntity entity) {
        this.heldEntity = entity;
        if (this.heldEntity != null) this.heldEntity.onHeldBy(this);
    }

    @Override
    public void render(Canvas canvas) {
        var g = canvas.getGraphics();
        var oT = g.getTransform();

        if (deathTime >= 30) {
            g.rotate(Math.toRadians((deathTime - 20 + canvas.partialTick()) * 15), canvas.toScreenX(getX()), canvas.toScreenY(getY() + 0.3));
        } else if (deathTime >= 0) {
            g.rotate(Math.toRadians(Math.sin((deathTime + canvas.partialTick())) * 15), canvas.toScreenX(getX()), canvas.toScreenY(getY() + 0.3));
        }
        super.render(canvas);

        g.setTransform(oT);
    }
}
