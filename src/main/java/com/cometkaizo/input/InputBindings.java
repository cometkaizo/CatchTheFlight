package com.cometkaizo.input;

import com.cometkaizo.registry.Registry;

import java.util.function.Supplier;

import static java.awt.event.KeyEvent.*;
import static java.awt.event.MouseEvent.BUTTON1;

public class InputBindings {

    public static final Registry<InputBinding> INPUT_BINDINGS = new Registry<>();

    public static final Supplier<KeyBinding> JUMP = INPUT_BINDINGS.register("jump", app -> new KeyBinding("Jump", VK_SPACE));
    public static final Supplier<KeyBinding> RIGHT = INPUT_BINDINGS.register("right", app -> new KeyBinding("Right", VK_D));
    public static final Supplier<KeyBinding> LEFT = INPUT_BINDINGS.register("left", app -> new KeyBinding("Left", VK_A));
    public static final Supplier<KeyBinding> UP = INPUT_BINDINGS.register("up", app -> new KeyBinding("Up", VK_W));
    public static final Supplier<KeyBinding> DOWN = INPUT_BINDINGS.register("down", app -> new KeyBinding("Down", VK_S));
    public static final Supplier<KeyBinding> INTERACT = INPUT_BINDINGS.register("interact", app -> new KeyBinding("Interact", VK_E));
    public static final Supplier<MouseButtonBinding> THROW = INPUT_BINDINGS.register("throw", app -> new MouseButtonBinding("Throw", BUTTON1));

}
