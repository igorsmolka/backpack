package com.smolka.backpack;

import java.util.Set;

public interface Backpack {

    void fillBackpack(Set<Item> items);

    Set<Item> getItemsInBackpack();

    int getCapacity();

    int getCostOfContent();

    int getWeightOfContent();
}
