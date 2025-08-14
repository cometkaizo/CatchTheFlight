package com.cometkaizo.input;

public interface InputListener {

    void keyPressed(KeyBinding key);
    void keyDown(KeyBinding key);
    void keyReleased(KeyBinding key);
    void mousePressed(MouseButtonBinding button, int x, int y);
    void mouseDown(MouseButtonBinding button, int x, int y);
    void mouseReleased(MouseButtonBinding button, int x, int y);

}
