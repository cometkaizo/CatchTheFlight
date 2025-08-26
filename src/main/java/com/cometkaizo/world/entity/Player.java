package com.cometkaizo.world.entity;

import com.cometkaizo.game.event.KeyPressedEvent;
import com.cometkaizo.game.event.MousePressedEvent;
import com.cometkaizo.game.event.PlayerDeathEvent;
import com.cometkaizo.game.event.RoomSwitchEvent;
import com.cometkaizo.input.InputBindings;
import com.cometkaizo.input.KeyBinding;
import com.cometkaizo.input.MouseButtonBinding;
import com.cometkaizo.screen.Assets;
import com.cometkaizo.screen.Canvas;
import com.cometkaizo.screen.Dialogue;
import com.cometkaizo.world.*;

import java.awt.*;
import java.util.Objects;

public class Player extends CollidableEntity {

    protected double jumpAccel = 1, diagJumpAccel = jumpAccel * Math.cos(Math.toRadians(45));
    protected double maxJumpVelocity = 1, maxDiagJumpVelocity = maxJumpVelocity * Math.cos(Math.toRadians(45));
    protected int jumpDirection = -1;
    protected double walkAccel = 0.3, diagWalkAccel = walkAccel * Math.cos(Math.toRadians(45));
    protected double maxWalkVelocity = 0.4, maxDiagWalkVelocity = maxWalkVelocity * Math.cos(Math.toRadians(45));
    protected double maxDownwardsVelocity = 0.7;
    protected double groundFriction = 0.1;
    protected double airFriction = 0.5;
    protected ThrowableEntity held;
    protected int deathRecoveryDuration = 5, killDuration = 20, interactDuration = 5, dialogueInteractDuration = 2;
    // jumpTime = -2 means jump not reset, -1 means jump reset
    protected int deathTime = -1, jumpTime = -1, throwTime = -1, walkTime = -1, interactTime = -1, dialogueInteractTime = -1;
    protected int prevWalkTime = -1;
    protected double throwStrength = 3;
    protected boolean facingRight = true;
    protected double deathAngleMul;

    public Player(Room.Layer layer, Vector.MutableDouble position, Args args) {
        super(layer, position, args);
        this.boundingBox = new BoundingBox(Vector.mutable(0D, 0D), Vector.immutable(0.6D, 0.6D));
        game.getEventBus().register(KeyPressedEvent.class, this::onKeyPressed);
        game.getEventBus().register(MousePressedEvent.class, this::onMousePressed);
    }

    @Override
    protected String getTexturePath() {
        return "player/" + ((jumpTime >= 0 && jumpTime < 5) ? "jump" : "normal");
    }

    public void kill() {
        game.getEventBus().post(new PlayerDeathEvent(this));

        deathAngleMul = Math.random() * 2 - 1;
        if (deathAngleMul >= 0 && deathAngleMul < 0.5) deathAngleMul = 0.5;
        else if (deathAngleMul < 0 && deathAngleMul > -0.5) deathAngleMul = -0.5;
    }
    public void killDueToLostItem() {
        deathTime = deathRecoveryDuration;
    }

    @Override
    public void reset() {
        super.reset();
        deathTime = -1;
    }

    @Override
    public void tick() {
        tickMotion();
        tickCheckpoint();
        super.tick();
        trySwitchRoom();

        tickJumpTime();
        tickDeathTime();
        tickThrowTime();
        tickInteractTime();
        tickDialogueInteractTime();
    }

    private void tickCheckpoint() {
        var prevCheckpoint = originalPosition;
        for (var checkpoint : room.checkpoints) {
            if (Math.abs(getX() - checkpoint.x) < 1) originalPosition = checkpoint;
        }
        if (!Objects.equals(prevCheckpoint, originalPosition)) {
            // todo: This sound effect gets a bit annoying, so I commented it out
            //Assets.sound("notify").play();
        }
    }
    private void tickJumpTime() {
        if (jumpTime >= 2 || collidedHorizontally && collidedVertically) {
            jumpTime = -2;
            jumpDirection = -1;
        }
        if (jumpTime == -2 && isAboveGround()) jumpTime = -1;
        if (jumpTime >= 0) jumpTime ++;
    }
    private void tickDeathTime() {
        if (isFalling() || !isDeathRecoverable()) {
            if (deathTime == deathRecoveryDuration) Assets.sound("fall").play();
            deathTime ++;
            if (deathTime >= killDuration) kill();
        } else deathTime = -1;
    }
    private void tickThrowTime() {
        if (throwTime > -1) throwTime --;
    }
    private void tickInteractTime() {
        if (game.getDialogue() != null) interactTime = interactDuration;
        else if (interactTime >= 0) interactTime --;
    }
    private void tickDialogueInteractTime() {
        if (dialogueInteractTime >= 0) dialogueInteractTime --;
    }


    private boolean isDeathRecoverable() {
        return deathTime < deathRecoveryDuration;
    }

    private boolean isFalling() {
        return !isJumping() && !isAboveGround();
    }

    private boolean isAboveGround() {
        return room.ground.containsSolid(boundingBox, this);
    }

    private boolean isJumping() {
        return jumpTime > -1;
    }

    private boolean isJumpAvailable() {
        return jumpTime == -1;
    }

    private void tickMotion() {
        motion.x *= groundFriction;
        motion.y *= groundFriction;

        boolean right = InputBindings.RIGHT.get().isDown;
        boolean left = InputBindings.LEFT.get().isDown;
        boolean up = InputBindings.UP.get().isDown;
        boolean down = InputBindings.DOWN.get().isDown;
        boolean diagonal = (right || left) && (up || down);

        prevWalkTime = walkTime;
        if (right || left || up || down) walkTime = walkTime + 1;
        else {
            if (walkTime > 7) {
                walkTime %= 7;
                prevWalkTime = walkTime;
            }
            if (walkTime >= 0) walkTime --;
        }

        double accel, maxVelocity;

        if (!isJumping()) {
            if (walkDisabled()) return;

            accel = diagonal ? this.diagWalkAccel : this.walkAccel;
            maxVelocity = diagonal ? this.maxDiagWalkVelocity : this.maxWalkVelocity;

            if (right) {
                motion.x += accel;
            }
            if (left) {
                motion.x -= accel;
            }
            if (up) {
                motion.y += accel;
            }
            if (down) {
                motion.y -= accel;
            }
        } else {
            accel = diagonal ? this.diagJumpAccel : this.jumpAccel;
            maxVelocity = diagonal ? this.maxDiagJumpVelocity : this.maxJumpVelocity;

            switch (jumpDirection) {
                case 0 -> motion.x += accel;
                case 1 -> {
                    motion.x += accel;
                    motion.y -= accel;
                }
                case 2 -> motion.y -= accel;
                case 3 -> {
                    motion.x -= accel;
                    motion.y -= accel;
                }
                case 4 -> motion.x -= accel;
                case 5 -> {
                    motion.x -= accel;
                    motion.y += accel;
                }
                case 6 -> motion.y += accel;
                case 7 -> {
                    motion.x += accel;
                    motion.y += accel;
                }
            }
        }

        motion.x = Math.min(Math.max(motion.x, -maxVelocity), maxVelocity);
        motion.y = Math.min(Math.max(motion.y, -maxVelocity), maxVelocity);

        facingRight = motion.x >= 0;

        if (walkTime % 5 == 0) Assets.sound("step").play();
    }

    private boolean walkDisabled() {
        return deathTime >= 1;
    }
    private boolean jumpDisabled() {
        return deathTime >= deathRecoveryDuration;
    }

    @Override
    protected void updateBoundingBox() {
        double width = boundingBox.getWidth();
        boundingBox.position.x = position.x - width / 2;
        boundingBox.position.y = position.y;
    }

    private void onKeyPressed(KeyPressedEvent event) {
        KeyBinding input = event.input();
        if (!jumpDisabled() && input == InputBindings.JUMP.get()) {
            jump();
        }
        if (input == InputBindings.INTERACT.get() && canInteractDialogue()) {
            game.advanceDialogue();
        }
    }
    public boolean canInteractDialogue() {
        return dialogueInteractTime == -1;
    }
    public boolean canInteract() {
        return interactTime == -1;
    }

    private void onMousePressed(MousePressedEvent event) {
        MouseButtonBinding input = event.input();
        double x = event.x();
        double y = event.y();
        if (input == InputBindings.THROW.get()) {
            throwHeld(x, y);
        }
    }
    public void throwHeld(double x, double y) {
        if (held != null) {
            held.setPosition(position);
            held.launch(Vector.mutable(x, y).subtract(position).normalize().scale(throwStrength).add(motion));
            held = null;
            throwTime = 3;
        }
    }

    // todo: buffer the direction input like in celeste
    public void jump() {
        if (!canJump()) return;

        jumpTime = 0;

        boolean right = InputBindings.RIGHT.get().isDown;
        boolean left = InputBindings.LEFT.get().isDown;
        boolean up = InputBindings.UP.get().isDown;
        boolean down = InputBindings.DOWN.get().isDown;
        if (right) {
            if (down) jumpDirection = 1;
            else if (up) jumpDirection = 7;
            else jumpDirection = 0;
        }
        else if (down) {
            if (left) jumpDirection = 3;
            else jumpDirection = 2;
        }
        else if (left) {
            if (up) jumpDirection = 5;
            else jumpDirection = 4;
        }
        else if (up) {
            jumpDirection = 6;
        }
        else jumpDirection = facingRight ? 0 : 4; // if not holding any direction

        Assets.sound("jump").play();
    }

    public boolean canJump() {
        return !isHolding() && throwTime == -1 && isJumpAvailable()/* && !isFloating()*/;
    }

    public boolean isHolding() {
        return held != null;
    }

    public void onInteract() {
        interactTime = interactDuration;
    }
    public void onInteractDialogue() {
        dialogueInteractTime = dialogueInteractDuration;
    }

    @Override
    protected boolean canMoveOffLedges() {
        return true;//isJumping(); // todo: preventing moving off ledges is still janky when you dash into a wall with no floor in front of it
    }

    private void trySwitchRoom() {
        /*double centerX = boundingBox.getCenterX();
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
        }*/
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

    public ThrowableEntity getHeld() {
        return held;
    }

    public void setHeld(ThrowableEntity entity) {
        this.held = entity;
    }

    @Override
    public void render(Canvas canvas) {
        var g = canvas.getGraphics();
        var oT = g.getTransform();
        var oC = g.getComposite();

        int screenX = canvas.toScreenX(canvas.lerp(getOldX(), getX()));
        int screenY = canvas.toScreenY(canvas.lerp(getOldY(), getY()) + 0.3);

        double angle = 0;
        double translateX = 0, translateY = 0;
        double alpha = 1;
        if (!isDeathRecoverable()) {
            angle = Math.toRadians(Math.pow((deathTime + canvas.partialTick() - deathRecoveryDuration) * 0.2, 2.5) * 15 * deathAngleMul);
            translateY = Math.pow((deathTime + canvas.partialTick() - deathRecoveryDuration) * 0.2, 2.5) * 25;
            alpha = Math.pow(1 - (deathTime + canvas.partialTick() - deathRecoveryDuration) / (killDuration - deathRecoveryDuration) * 1, 5);
        } else if (deathTime >= 0) {
//            angle = Math.sin((deathTime + canvas.partialTick()) * 1.3) * 0.2;
        } else if (walkTime >= 0) {
            angle = Math.sin((canvas.lerp(prevWalkTime, walkTime))) * 0.2;
            translateY = -Math.abs(Math.sin(canvas.lerp(prevWalkTime, walkTime)) * 25);
        }

        {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) alpha));
            g.translate(translateX, translateY);
            g.translate(screenX, screenY);
            if (angle != 0)
                g.rotate(angle);
            if (!facingRight) g.scale(-1, 1);
            g.translate(-screenX, -screenY);
        }

        super.render(canvas);

        g.setTransform(oT);
        g.setComposite(oC);
    }

    public static Dialogue dialogue(String msg, String textureVariation, Dialogue next) {
        return new Dialogue(msg, "gui/player/" + textureVariation, next);
    }
}
