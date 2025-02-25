package com.smolka;

import com.smolka.backpack.Item;
import com.smolka.backpack.impl.Variant;
import com.smolka.backpack.impl.VariantService;
import org.junit.Test;

public class VariantTest {

    @Test
    public void testVariant() {
        int capacity = 14;
        Variant lower = new Variant(new Item("1", 1, 1));
        lower.putMutableElement(new Item("2", 2, 10), capacity);
        lower.putMutableElement(new Item("3", 3, 20), capacity);
        lower.putMutableElement(new Item("4", 4, 30), capacity);

        Variant higher = new Variant(new Item("1", 1, 1));
        higher.putMutableElement(new Item("5", 5, 30), capacity);
        higher.putMutableElement(new Item("6", 6, 20), capacity);

        VariantService variantService = new VariantService();
        Variant variant = variantService.mergeOptimally(lower, higher, capacity);

        variant = variant;
    }
}
