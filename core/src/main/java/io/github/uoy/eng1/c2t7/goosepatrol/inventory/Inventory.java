package io.github.uoy.eng1.c2t7.goosepatrol.inventory;

public class Inventory {
    private final ItemStack[] slots;
    private int selectedSlot = 0;

    public Inventory(int slotCount) {
        this.slots = new ItemStack[slotCount];
    }

    public ItemStack getActiveStack() {
        return slots[selectedSlot];
    }

    public void selectSlot(int index) {
        if (index >= 0 && index < slots.length) {
            this.selectedSlot = index;
        }
    }

    public boolean addItem(Item item, int count) {
        // 1. Try stacking into existing non-full slot
        for (ItemStack stack : slots) {
            if (stack != null && stack.getItem().getId().equals(item.getId())
                    && stack.getCount() < item.getMaxStack()) {
                int space = item.getMaxStack() - stack.getCount();
                int toAdd = Math.min(space, count);
                stack.add(toAdd);
                count -= toAdd;
                if (count == 0)
                    return true;
            }
        }
        // 2. Put remainder into first empty slot
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] == null) {
                int toAdd = Math.min(item.getMaxStack(), count);
                slots[i] = new ItemStack(item, toAdd);
                count -= toAdd;
                if (count == 0)
                    return true;
            }
        }
        return count == 0; // false if inventory was full
    }

    public void consumeActiveItem() {
        ItemStack active = getActiveStack();
        if (active != null && active.shrink(1)) {
            slots[selectedSlot] = null;
        }
    }

    public ItemStack[] getSlots() {
        return slots;
    }

    public int getSelectedSlot() {
        return selectedSlot;
    }

    public int getSlotCount() {
        return slots.length;
    }
}