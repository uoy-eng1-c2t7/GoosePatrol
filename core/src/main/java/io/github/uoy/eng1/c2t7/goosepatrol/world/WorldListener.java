package io.github.uoy.eng1.c2t7.goosepatrol.world;

/** Things the world tells the outside (UI, audio...). The world never imports UI code. */
public interface WorldListener {
    /** Player reached the bus with geese still following. */
    void onBusRefused();

    /** Player triggered a hidden event. */
    void onEventTriggered(String eventName, String message);

    /** Player used an item from the inventory. */
    void onItemUsed(String itemName);

    /** Player picked an item up off the map. */
    void onItemPickedUp(String itemName);
}
