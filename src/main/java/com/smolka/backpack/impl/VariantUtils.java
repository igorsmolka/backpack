package com.smolka.backpack.impl;

public class VariantUtils {

    /**
     * Две ветки не должны содержать одинаковых элементов в mutable и должны содержать одинаковую immutable. Валидацию добавлю потом.
     * Мерж пока пытаемся делать следующим образом.
     * Ветка left будет "большей" веткой, ветка right - "меньшей". Именно right мы пытаемся оптимизировать за счет left.
     * Определяем максимальный размер удаления. Это должен быть минимум от размерностей mutable-части двух веток. Если в итоге он равняется right - делаем -1.
     * Так мы гарантированно оставим как минимум 1 элемент в right, таким образом это действительно будет мерж, полную замену эта операция не подразумевает.
     * Начинаем цикл от полученного maxRemove и идем вниз вплоть до 1. (пока так себе представляю!). Эта перменная-счетчик, идущая вниз от maxRemove будет называться partSize.
     * Берем left.
     * Сортируем предметы left по цене по убывающей. Начинаем резать порции по partSize. Какой алго?
     * Например
     * 1 (70)
     * 2 (50)
     * 3 (30)
     *
     * размер порции - 2.
     * сначала 1-2, потом 1-3, потом 2-3 и так далее. При этом 1-3 мы можем смело игнорировать, оно будет:
     * во-первых, тяжелее, чем более раннее 1-2.
     * во-вторых, мы доберемся до него позже, что говорит о том, что эта порция еще и дешевле.
     * 2-3 к слову тоже в данном случае. Здесь имеет смысл только 1-2.
     *
     * @param first
     * @param second
     * @param capacity
     * @return
     */
    public static Variant mergeOptimally(Variant first, Variant second, int capacity) {
        Variant left = first.getMinMutableElementWeight() > second.getMinMutableElementWeight() ? second : first;
        Variant right = first.getMinMutableElementWeight() > second.getMinMutableElementWeight() ? first : second;

        int maxRemove = Integer.min(left.sizeOfMutablePart(), right.sizeOfMutablePart());
        if (maxRemove == right.sizeOfMutablePart()) {
            maxRemove--;
        }

        int amendedCapacity = capacity - right.getImmutablePartWeight();


        return null;
    }
}
