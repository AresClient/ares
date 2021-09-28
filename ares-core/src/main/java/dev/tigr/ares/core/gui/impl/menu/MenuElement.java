package dev.tigr.ares.core.gui.impl.menu;

/**
 * @author Makrennel 09/28/21
 */
public class MenuElement {
    private MenuElement parent;

    private int x = 0;
    private int y = 0;
    private int width = 0;
    private int height = 0;

    public MenuElement() {
        this.parent = null;
    }

    public MenuElement(MenuElement parent) {
        this.parent = parent;
    }

    public MenuElement getParent() {
        return parent;
    }

    public void setParent(MenuElement parent) {
        this.parent = parent;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public double getRenderX() {
        return getParent() == null ? getX() : getX() + getParent().getRenderX();
    }

    public double getRenderY() {
        return getParent() == null ? getY() : getY() + getParent().getRenderY();
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
