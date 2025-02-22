package com.smolka;

import com.smolka.backpack.Backpack;
import com.smolka.backpack.Item;
import com.smolka.backpack.impl.BackpackImpl;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class BackpackTest {

    @Test
    public void test13() {
        Set<Item> items = Set.of(
                new Item("1", 1, 10),
                new Item("2", 2, 4),
                new Item("3", 3, 5),
                new Item("4", 4, 8),
                new Item("5", 5, 10)
        );

        int check = 28;

        Backpack backpack = new BackpackImpl(10);
        backpack.fillBackpack(items);

        assert backpack.getCostOfContent() == check;
        assert backpack.getWeightOfContent() <= backpack.getCapacity();
    }

    @Test
    public void test12() {
        //todo слишком торопишься уходить вперед при сбросе "балласта". подбирает 126, но 136 было бы выгодней, если бы ты не сбросил 3 как балласт
        //todo точно ли с последней веткой надо сверять, игнорируя остальные? подумай еще раз. скорее всего тоже много чего теряешь, как и в этом случае
        Set<Item> items = Set.of(
                new Item("1", 1, 10),
                new Item("2", 2, 2),
                new Item("3", 3, 3),
                new Item("4", 6, 6),
                new Item("5", 9, 9)
        );

        int check = 19;

        Backpack backpack = new BackpackImpl(10);
        backpack.fillBackpack(items);

        assert backpack.getCostOfContent() == check;
        assert backpack.getWeightOfContent() <= backpack.getCapacity();
    }

    @Test
    public void test11() {
        //todo здесь повисает
        Set<Item> items = new HashSet<>();
        for (int i = 1; i < 1000; i++) {
            items.add(new Item(String.valueOf(i), i, i));
        }

        int check = 1999;

        Backpack backpack = new BackpackImpl(1999);

        backpack.fillBackpack(items);
        assert backpack.getCostOfContent() == check;
        assert backpack.getWeightOfContent() <= backpack.getCapacity();
    }

    @Test
    public void test10() {
        Set<Item> items = Set.of(
                new Item("1", 1, 1),
                new Item("2", 1, 2),
                new Item("3", 1, 3),
                new Item("4", 2, 10)
        );

        int check = 13;

        Backpack backpack = new BackpackImpl(3);

        backpack.fillBackpack(items);

        assert backpack.getCostOfContent() == check;
        assert backpack.getWeightOfContent() <= backpack.getCapacity();
    }

    @Test
    public void test9() {
        Set<Item> items = Set.of(
                new Item("1", 1, 1),
                new Item("2", 1, 2),
                new Item("3", 1, 3),
                new Item("4", 1, 4),
                new Item("5", 2, 5),
                new Item("6", 2, 6),
                new Item("7", 3, 7),
                new Item("8", 3, 8)
        );

        int check = 15;

        Backpack backpack = new BackpackImpl(5);

        backpack.fillBackpack(items);

        assert backpack.getCostOfContent() == check;
        assert backpack.getWeightOfContent() <= backpack.getCapacity();
    }

    @Test
    public void test8() {
        Set<Item> items = Set.of(
                new Item("1", 1, 1),
                new Item("2", 1, 2),
                new Item("3", 1, 3),
                new Item("4", 1, 4),
                new Item("5", 1, 5),
                new Item("6", 1, 6),
                new Item("8", 1, 8),
                new Item("9", 1, 9),
                new Item("21", 1, 21),
                new Item("10", 1, 10),
                new Item("11", 1, 11),
                new Item("12", 1, 12),
                new Item("13", 1, 13),
                new Item("14", 1, 14),
                new Item("15", 1, 15),
                new Item("16", 1, 16),
                new Item("17", 1, 17),
                new Item("52", 2, 1000),
                new Item("19", 1, 19),
                new Item("20", 1, 20),
                new Item("22", 1, 22),
                new Item("23", 1, 23),
                new Item("24", 1, 24),
                new Item("25", 1, 25),
                new Item("26", 1, 26),
                new Item("51", 2, 1000),
                new Item("27", 1, 27),
                new Item("28", 1, 28),
                new Item("29", 1, 29),
                new Item("30", 1, 30),
                new Item("31", 1, 31),
                new Item("32", 1, 32),
                new Item("33", 1, 33),
                new Item("34", 1, 34),
                new Item("35", 1, 35),
                new Item("36", 1, 36),
                new Item("18", 1, 18),
                new Item("37", 1, 37),
                new Item("38", 1, 38),
                new Item("39", 1, 39),
                new Item("40", 1, 40),
                new Item("41", 1, 41),
                new Item("42", 1, 42),
                new Item("7", 1, 7),
                new Item("43", 1, 43),
                new Item("44", 1, 44),
                new Item("45", 1, 45),
                new Item("46", 1, 46),
                new Item("47", 1, 47),
                new Item("48", 1, 48),
                new Item("49", 1, 49),
                new Item("50", 1, 50)
        );

        int check = 2285;

        Backpack backpack = new BackpackImpl(10);
        backpack.fillBackpack(items);

        assert backpack.getCostOfContent() == check;
        assert backpack.getWeightOfContent() <= backpack.getCapacity();
    }

    @Test
    public void test1() {
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
        //todo здесь переполняет рюкзак
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

    @Test
    public void test7() {
        Set<Item> items = Set.of(
                new Item("Бокс 1", 4, 2),
                new Item("Бокс 2", 4, 2),
                new Item("Бокс 3", 4, 2)
        );

        int check = 0;

        Backpack backpack = new BackpackImpl(3);
        backpack.fillBackpack(items);

        assert backpack.getCostOfContent() == check;
        assert backpack.getWeightOfContent() <= backpack.getCapacity();
    }
}
