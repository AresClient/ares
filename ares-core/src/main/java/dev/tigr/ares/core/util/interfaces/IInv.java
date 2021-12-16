package dev.tigr.ares.core.util.interfaces;

public interface IInv {
    int getCurrentSlot();

    void setCurrentSlot(int currentSlot);

    void setLastSelectedSlot(int lastSelectedSlot);

    int getItemInSlot(int slot);

    int getMainHandItem();

    int getOffHandItem();
}
