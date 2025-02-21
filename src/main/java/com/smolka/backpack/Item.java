package com.smolka.backpack;

import java.util.Objects;

public class Item {

    private final String id;

    private final int weight;

    private final int cost;

    public Item(String id, int weight, int cost) {
        assert weight > 0;
        assert cost > 0;

        this.id = id;
        this.weight = weight;
        this.cost = cost;
    }

    public String getId() {
        return id;
    }

    public int getWeight() {
        return weight;
    }

    public int getCost() {
        return cost;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return Objects.equals(id, item.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
