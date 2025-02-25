package com.smolka.backpack.impl.garb;

import com.smolka.backpack.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class VariantService {

    //todo концепция поменялась!!! во первых immutable-часть в Variant нафиг с пляжу. во вторых мне кажется правильней обратный процесс.
    // две ветки - меньшая и большая, с меньшими весами и бОльшими весами.
    // наша задача - оптимизировать ветку с бОльшими весами за счет ветки с меньшими весами, если это возможно. должны выйти в максимальный профит и возможно даже стать чуточку легче.
    // ну или чуточку тяжелей, кому как. но главное - как можно дороже.

    public Variant mergeOptimally(Variant first, Variant second, int capacity) {
        Variant lower = first.getMinMutableElementWeight() > second.getMinMutableElementWeight() ? second : first;
        Variant higher = first.getMinMutableElementWeight() > second.getMinMutableElementWeight() ? first : second;

        int l = capacity - lower.getVariantWeight();

        List<Item> mutableFromHigher = higher.getMutablePart();
        List<Item> mutableFromLower = lower.getMutablePart();

        Set<ProfitInfo> profitInfos = new HashSet<>();

        for (Item currentItem : mutableFromHigher) {
            ProfitInfo profitInfo = new ProfitInfo(currentItem);
            for (Item item : mutableFromLower) {
                profitInfo.addProfitItem(item, l);
            }
            profitInfos.add(profitInfo);
        }

        ProfitInfo f = profitInfos.stream().findFirst().orElseThrow();

        Step step = new Step(f, profitInfos, l);
        Step result = step(step);

        return null;
    }

    private Step step(Step step) {
        System.out.println("*");
        List<ProfitItem> availableProfitItems = step.getAvailableProfitItemsForCurrent();

        if (availableProfitItems.isEmpty()) {
            //todo когда профитайтемы закончились - надо бы по хорошему докинуть в результат остатки профитинфо, т.е. "родной" ветки. наиболее выгодные.
            // делать это тоже можно по рывкам до хуяча и запоминанию максимума. отсортировать только остатки по весу???
            // хотя не все так просто конечно. наверное...
            MaxResult result = step.extractInfoAsResult();
            step.refreshResult(result);
            return step;
        }

        Set<ProfitInfo> availableProfitInfos = step.getOtherFreeProfitInfos();

        for (ProfitItem profitItem : availableProfitItems) {
            step.chooseProfitItem(profitItem);

            if (availableProfitInfos.isEmpty()) {
                MaxResult result = step.extractInfoAsResult();
                step.refreshResult(result);
                step.removeLastProfitItem();
                continue;
            }

            for (ProfitInfo profitInfo : availableProfitInfos) {
                Step newStep = step.newStepForNextProfitInfo(profitInfo);
                Step recursionResult = step(newStep);
                step.refreshResult(recursionResult.getResult());
            }

            step.removeLastProfitItem();
        }

        return step;
    }

    private static class Step {

        private final ProfitInfo rootProfitInfo;

        private final ProfitInfo currentProfitInfo;

        private final Map<ProfitInfo, ProfitItem> chooseMap;

        private final Set<ProfitInfo> profitInfosToIgnore;

        private final Set<ProfitInfo> allProfitsInfo;

        private final Set<ProfitItem> profitItemsToIgnore;

        private MaxResult result;

        private int currentWeightGain;

        private int currentProfit;

        private final int limit;

        public Step(ProfitInfo rootProfitInfo,
                    Map<ProfitInfo, ProfitItem> chooseMap,
                    ProfitInfo currentProfitInfo,
                    Set<ProfitInfo> profitInfosToIgnore,
                    Set<ProfitInfo> allProfitsInfo,
                    Set<ProfitItem> profitItemsToIgnore,
                    MaxResult result,
                    int currentWeightGain,
                    int currentProfit,
                    int limit) {
            this.chooseMap = chooseMap;
            this.rootProfitInfo = rootProfitInfo;
            this.currentProfitInfo = currentProfitInfo;
            this.profitInfosToIgnore = profitInfosToIgnore;
            this.allProfitsInfo = allProfitsInfo;
            this.profitItemsToIgnore = profitItemsToIgnore;
            this.result = result;
            this.currentWeightGain = currentWeightGain;
            this.currentProfit = currentProfit;
            this.limit = limit;
        }

        public Step(ProfitInfo rootProfitInfo, Set<ProfitInfo> allProfitsInfo, int limit) {
            this.chooseMap = new HashMap<>();
            this.rootProfitInfo = rootProfitInfo;
            this.currentProfitInfo = rootProfitInfo;
            this.profitInfosToIgnore = new HashSet<>(Set.of(rootProfitInfo));
            this.profitItemsToIgnore = new HashSet<>();
            this.allProfitsInfo = new HashSet<>(allProfitsInfo);
            this.currentProfit = 0;
            this.currentWeightGain = 0;
            this.limit = limit;
        }

        public Step(ProfitInfo rootProfitInfo, Set<ProfitInfo> allProfitsInfo, Set<ProfitInfo> profitInfosToIgnore, int limit) {
            this.chooseMap = new HashMap<>();
            this.rootProfitInfo = rootProfitInfo;
            this.currentProfitInfo = rootProfitInfo;
            this.profitInfosToIgnore = new HashSet<>(profitInfosToIgnore);
            this.profitInfosToIgnore.add(rootProfitInfo);
            this.profitItemsToIgnore = new HashSet<>();
            this.allProfitsInfo = new HashSet<>(allProfitsInfo);
            this.currentProfit = 0;
            this.currentWeightGain = 0;
            this.limit = limit;
        }

        public MaxResult getResult() {
            return result;
        }

        public void chooseProfitItem(ProfitItem profitItem) {
            chooseMap.put(currentProfitInfo, profitItem);
            this.profitItemsToIgnore.add(profitItem);
            this.currentProfit += profitItem.profit();
            this.currentWeightGain += profitItem.weightGain();
        }

        public void removeLastProfitItem() {
            ProfitItem profitItem = chooseMap.remove(currentProfitInfo);
            if (profitItem != null) {
                this.profitItemsToIgnore.remove(profitItem);
                this.currentProfit -= profitItem.profit();
                this.currentWeightGain -= profitItem.weightGain();
            }
        }

        public Step newStepForNextProfitInfo(ProfitInfo nextProfitInfo) {
            Set<ProfitInfo> profitInfosToIgnore = new HashSet<>(this.profitInfosToIgnore);
            profitInfosToIgnore.add(nextProfitInfo);

            return new Step(rootProfitInfo,
                    new HashMap<>(chooseMap),
                    nextProfitInfo,
                    profitInfosToIgnore,
                    allProfitsInfo,
                    new HashSet<>(profitItemsToIgnore),
                    result,
                    currentWeightGain,
                    currentProfit,
                    limit);
        }

        public Set<ProfitInfo> getOtherFreeProfitInfos() {
            return allProfitsInfo.stream().filter(pi -> !profitInfosToIgnore.contains(pi)).collect(Collectors.toSet());
        }

        public List<ProfitItem> getAvailableProfitItemsForCurrent() {
            List<ProfitItem> profitItems = new ArrayList<>();
            for (ProfitItem profitItem : currentProfitInfo.getProfitItems()) {
                if (profitItem.profit() <= 0) {
                    break;
                }
                if (currentWeightGain + profitItem.weightGain() > limit || profitItemsToIgnore.contains(profitItem)) {
                    continue;
                }

                profitItems.add(profitItem);
            }

            return profitItems;
        }

        public MaxResult extractInfoAsResult() {
            return new MaxResult(new HashMap<>(chooseMap), currentWeightGain, currentProfit);
        }

        public void refreshResult(MaxResult maxResult) {
            if (this.result == null) {
                this.result = maxResult;
                return;
            }

            if (this.result.currentProfit() < maxResult.currentProfit()) {
                this.result = maxResult;
            } else if (this.result.currentProfit() == maxResult.currentProfit() && this.result.currentWeightGain() > maxResult.currentWeightGain()) {
                this.result = maxResult;
            }
        }
    }

    private static class ProfitInfo {

        private final Item item;

        private final SortedSet<ProfitItem> profitItems;

        public ProfitInfo(Item item) {
            this.item = item;
            this.profitItems = new TreeSet<>((p1, p2) -> Integer.compare(p2.profit(), p1.profit()));
        }

        public void addProfitItem(Item otherItem, int l) {
            int weightGain = item.getWeight() - otherItem.getWeight();
            ProfitItem profitItem = new ProfitItem(otherItem, item.getCost() - otherItem.getCost(), weightGain);
            profitItems.add(profitItem);
        }

        public Item getItem() {
            return item;
        }

        public SortedSet<ProfitItem> getProfitItems() {
            return profitItems;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            ProfitInfo that = (ProfitInfo) o;
            return Objects.equals(item, that.item);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(item);
        }
    }

    private record MaxResult(
            Map<ProfitInfo, ProfitItem> chooseMap,
            int currentWeightGain,
            int currentProfit
    ) {

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            MaxResult result = (MaxResult) o;
            return currentProfit == result.currentProfit && currentWeightGain == result.currentWeightGain && Objects.equals(chooseMap, result.chooseMap);
        }

        @Override
        public int hashCode() {
            return Objects.hash(chooseMap, currentWeightGain, currentProfit);
        }
    }

    private record ProfitItem(
            Item item,
            int profit,
            int weightGain
    ) {

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            ProfitItem that = (ProfitItem) o;
            return Objects.equals(item, that.item);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(item);
        }
    }

}
