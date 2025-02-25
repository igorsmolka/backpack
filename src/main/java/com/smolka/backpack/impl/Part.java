package com.smolka.backpack.impl;

import com.smolka.backpack.Item;

import java.util.ArrayList;
import java.util.List;

public class Part {

    private final List<Item> items;

    private int variantWeight;

    private int variantCost;

    private Integer minWeight;

    public Part() {
        items = new ArrayList<>();
    }

    public List<Item> getItems() {
        return items;
    }

    public int getVariantWeight() {
        return variantWeight;
    }

    public int getVariantCost() {
        return variantCost;
    }

    public Integer getMinWeight() {
        return minWeight;
    }

    public boolean putElement(Item item, int capacity) {
        if (variantWeight + item.getWeight() > capacity) {
            return false;
        }

        items.add(item);

        variantWeight += item.getWeight();
        variantCost += item.getCost();
        if (minWeight == null) {
            minWeight = item.getWeight();
            return true;
        }

        if (item.getWeight() < minWeight) {
            minWeight = item.getWeight();
        }

        return true;
    }
}
