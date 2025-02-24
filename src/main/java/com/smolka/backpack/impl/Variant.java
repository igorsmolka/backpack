package com.smolka.backpack.impl;

import com.smolka.backpack.Item;

import java.util.ArrayList;
import java.util.List;

public class Variant {

    private final List<Item> immutablePart;

    private final List<Item> mutablePart;

    private int variantWeight;

    private int variantCost;

    private Integer minMutableElementWeight;

    public Variant(Item immutableElement) {
        this.immutablePart = new ArrayList<>();
        this.immutablePart.add(immutableElement);
        this.mutablePart = new ArrayList<>();
        this.minMutableElementWeight = null;
        this.variantWeight = immutableElement.getWeight();
        this.variantCost = immutableElement.getCost();

    }

    public Variant(List<Item> immutablePart) {
        this.immutablePart = new ArrayList<>(immutablePart);
        this.mutablePart = new ArrayList<>();
        this.minMutableElementWeight = null;
        this.variantWeight = immutablePart.stream().map(Item::getWeight).reduce(Integer::sum).orElse(0);
        this.variantCost = immutablePart.stream().map(Item::getCost).reduce(Integer::sum).orElse(0);
    }

    public List<Item> getImmutablePart() {
        return immutablePart;
    }

    public List<Item> getMutablePart() {
        return mutablePart;
    }

    public int getVariantWeight() {
        return variantWeight;
    }

    public int getVariantCost() {
        return variantCost;
    }

    public Integer getMinMutableElementWeight() {
        return minMutableElementWeight;
    }

    public boolean putMutableElement(Item item, int capacity) {
        if (variantWeight + item.getWeight() > capacity) {
            return false;
        }

        mutablePart.add(item);

        variantWeight += item.getWeight();
        variantCost += item.getCost();
        if (minMutableElementWeight == null) {
            minMutableElementWeight = item.getWeight();
            return true;
        }

        if (item.getWeight() < minMutableElementWeight) {
            minMutableElementWeight = item.getWeight();
        }

        return true;
    }
}
