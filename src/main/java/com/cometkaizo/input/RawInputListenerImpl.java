package com.cometkaizo.input;

import com.cometkaizo.registry.Registry;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;

public class RawInputListenerImpl implements RawInputListener {
    private final Set<InputListener> inputListeners = new HashSet<>(1);
    private final Registry<InputBinding> keyBindings;

    public RawInputListenerImpl(Registry<InputBinding> keyBindings) {
        this.keyBindings = keyBindings;
    }

    @Override
    public void addInputListener(InputListener listener) {
        inputListeners.add(listener);
    }
    @Override
    public void removeInputListener(InputListener listener) {
        inputListeners.remove(listener);
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        keyBindings.values().forEach(binding -> {
            if (binding instanceof KeyBinding keyBinding &&
                    e.getKeyCode() == keyBinding.key) {
                boolean prevIsDown = keyBinding.isDown;
                keyBinding.isDown = true;
                if (!prevIsDown) inputListeners.forEach(l -> l.keyPressed(keyBinding));
                inputListeners.forEach(l -> l.keyDown(keyBinding));
            }
        });
    }

    @Override
    public void keyReleased(KeyEvent e) {
        keyBindings.values().forEach(binding -> {
            if (binding instanceof KeyBinding keyBinding && e.getKeyCode() == keyBinding.key) {
                keyBinding.isDown = false;
                inputListeners.forEach(l -> l.keyReleased(keyBinding));
            }
        });
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        keyBindings.values().forEach(binding -> {
            if (binding instanceof MouseButtonBinding buttonBinding &&
                    e.getButton() == buttonBinding.button) {
                boolean prevIsDown = buttonBinding.isDown;
                buttonBinding.isDown = true;
                if (!prevIsDown) inputListeners.forEach(l -> l.mousePressed(buttonBinding, e.getX(), e.getY()));
                inputListeners.forEach(l -> l.mouseDown(buttonBinding, e.getX(), e.getY()));
            }
        });
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        keyBindings.values().forEach(binding -> {
            if (binding instanceof MouseButtonBinding buttonBinding && e.getButton() == buttonBinding.button) {
                buttonBinding.isDown = false;
                inputListeners.forEach(l -> l.mouseReleased(buttonBinding, e.getX(), e.getY()));
            }
        });
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }
}
