package com.smolka;

import com.smolka.backpack.Backpack;
import com.smolka.backpack.Item;
import com.smolka.backpack.impl.BackpackImpl;
import org.junit.Test;

import java.util.Set;

public class BackpackTest {

    @Test
    public void test() {
        Set<Item> items = Set.of(
                new Item("Часы", 1, 4),
                new Item("Пакет сока", 2, 3),
                new Item("Банка помидоров", 7, 2),
                new Item("Ржавый портсигар", 3, 1),
                new Item("Планшет", 8, 6)
        );

        int check = 10;

        Backpack backpack = new BackpackImpl(9);
        backpack.fillBackpack(items);

        assert backpack.getCostOfContent() == check;
        assert backpack.getWeightOfContent() <= backpack.getCapacity();
    }

    @Test
    public void test2() {
        Set<Item> items = Set.of(
                new Item("Консервы", 5, 3),
                new Item("Гиря", 10, 5),
                new Item("Банка гвоздей", 6, 4),
                new Item("Кулон", 5, 2)
        );

        int check = 7;

        Backpack backpack = new BackpackImpl(14);
        backpack.fillBackpack(items);

        assert backpack.getCostOfContent() == check;
        assert backpack.getWeightOfContent() <= backpack.getCapacity();
    }

    @Test
    public void test3() {
        Set<Item> items = Set.of(
                new Item("Коробок кубинских сигар", 15, 60),
                new Item("Платиновое ожерелье", 30, 90),
                new Item("Золотой слиток", 50, 100)
        );

        int check = 190;

        Backpack backpack = new BackpackImpl(80);
        backpack.fillBackpack(items);

        assert backpack.getCostOfContent() == check;
        assert backpack.getWeightOfContent() <= backpack.getCapacity();
    }

    @Test
    public void test4() {
        Set<Item> items = Set.of(
                new Item("Брат", 10, 100),
                new Item("Сестра", 20, 80),
                new Item("Первая научная история войны 1812 года, 3 издание", 50, 1000)
        );

        int check = 1000;

        Backpack backpack = new BackpackImpl(50);
        backpack.fillBackpack(items);

        assert backpack.getCostOfContent() == check;
        assert backpack.getWeightOfContent() <= backpack.getCapacity();
    }

    @Test
    public void test5() {
        Set<Item> items = Set.of(
                new Item("Статуэтка", 4, 1),
                new Item("Столовые принадлежности", 5, 2),
                new Item("Пачка сигарет", 1, 3)
        );

        int check = 3;

        Backpack backpack = new BackpackImpl(4);
        backpack.fillBackpack(items);

        assert backpack.getCostOfContent() == check;
        assert backpack.getWeightOfContent() <= backpack.getCapacity();
    }

    @Test
    public void test6() {
        Set<Item> items = Set.of(
                new Item("Бокс 1", 4, 2),
                new Item("Бокс 2", 4, 2),
                new Item("Бокс 3", 4, 2)
        );

        int check = 4;

        Backpack backpack = new BackpackImpl(8);
        backpack.fillBackpack(items);

        assert backpack.getCostOfContent() == check;
        assert backpack.getWeightOfContent() <= backpack.getCapacity();
    }
}
