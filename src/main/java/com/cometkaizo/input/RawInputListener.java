package com.cometkaizo.input;

import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public interface RawInputListener extends MouseListener, MouseMotionListener, KeyListener {
    void addInputListener(InputListener listener);
    void removeInputListener(InputListener listener);
}
