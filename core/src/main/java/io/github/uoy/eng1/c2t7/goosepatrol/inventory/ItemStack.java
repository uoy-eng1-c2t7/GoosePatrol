package io.github.uoy.eng1.c2t7.goosepatrol.inventory;

public class ItemStack {
    private final Item item;
    private int count;

    public ItemStack(Item item, int count) {
        this.item = item;
        this.count = count;
    }

    public Item getItem() {
        return item;
    }

    public int getCount() {
        return count;
    }

    public void add(int amount) {
        this.count += amount;
    }

    public boolean shrink(int amount) {
        this.count -= amount;
        return this.count <= 0;
    }
}
