package Prototype;

import javax.swing.*;

public abstract class Window {
    public int width, height;
    public JFrame frame;

    Window(int w, int h) {
        this.frame = new JFrame();
        this.width = w;
        this.height = h;

        this.frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.frame.setSize(this.width, this.height);
        this.initialize();
    }

    abstract void initialize();
}