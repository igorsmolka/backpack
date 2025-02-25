package com.smolka.backpack.impl;

import com.smolka.backpack.Item;

import java.util.List;

public class PartUtils {

    public Part optimize(Part partOne, Part partTwo, int capacity) {
        Part lower = partOne.getMinWeight() > partTwo.getMinWeight() ? partTwo : partOne;
        Part higher = partOne.getMinWeight() > partTwo.getMinWeight() ? partOne : partTwo;

        List<Item> itemsFromHigher = higher.getItems();
        List<Item> itemsFromLower = lower.getItems();


        return null;
    }


}
