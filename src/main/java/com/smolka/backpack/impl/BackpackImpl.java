package com.smolka.backpack.impl;

import com.smolka.backpack.Backpack;
import com.smolka.backpack.Item;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class BackpackImpl implements Backpack {

    private final Set<Item> itemsInBackpack;

    private final int capacity;

    public BackpackImpl(int capacity) {
        assert capacity > 0;

        this.itemsInBackpack = new HashSet<>();
        this.capacity = capacity;
    }

    @Override
    public void fillBackpack(Set<Item> items) {
        itemsInBackpack.clear();


        List<Item> preProcessedItems = preProcessItems(items);

        List<ItemSelectionResult> selectionResults = new ArrayList<>();
        for (int i = 0; i < preProcessedItems.size(); i++) {
            addSelectionResultForIndex(i, preProcessedItems, selectionResults);
        }

        int maxCost = -1;
        List<Item> branchWithMaxCost = new ArrayList<>();
        for (ItemSelectionResult selectionResult : selectionResults) {
            if (selectionResult.isEmpty()) {
                continue;
            }

            ItemsCostInfo itemsCostInfo = selectionResult.getBranchWithMaxCost();
            int currentCost = itemsCostInfo.cost();
            if (currentCost > maxCost) {
                maxCost = currentCost;
                branchWithMaxCost = itemsCostInfo.items();
            }
        }

        itemsInBackpack.addAll(branchWithMaxCost);
    }

    @Override
    public int getCostOfContent() {
        return itemsInBackpack.stream().map(Item::getCost).reduce(Integer::sum).orElse(0);
    }

    @Override
    public int getWeightOfContent() {
        return itemsInBackpack.stream().map(Item::getWeight).reduce(Integer::sum).orElse(0);
    }

    @Override
    public Set<Item> getItemsInBackpack() {
        return itemsInBackpack;
    }

    @Override
    public int getCapacity() {
        return capacity;
    }

    private List<Item> preProcessItems(Set<Item> items) {
        Set<Item> itemsWithoutOverWeightedElements = items.stream().filter(item -> item.getWeight() <= capacity).collect(Collectors.toSet());
        Map<Integer, WeightSequenceInfo> weightSequenceInfoMap = new HashMap<>();

        for (Item item : itemsWithoutOverWeightedElements) {
            int weight = item.getWeight();
            weightSequenceInfoMap.putIfAbsent(weight, new WeightSequenceInfo(weight));
            weightSequenceInfoMap.get(weight).addItem(item);
        }

        List<Item> preProcessedItemsList = new ArrayList<>();
        for (WeightSequenceInfo weightSequenceInfo : weightSequenceInfoMap.values()) {
            preProcessedItemsList.addAll(weightSequenceInfo.getMostProfitablePossibleSequence(capacity));
        }

        preProcessedItemsList.sort(Comparator.comparingInt(Item::getWeight));
        return preProcessedItemsList;
    }

    private void addSelectionResultForIndex(int index, List<Item> items, List<ItemSelectionResult> otherResults) {
        ItemSelectionResult result = new ItemSelectionResult(items.get(index));
        for (int i = index + 1; i < items.size(); i++) {
            Item nextItem = items.get(i);
            if (!result.put(nextItem, capacity)) {
                otherResults.add(result);
                return;
            }
        }

        otherResults.add(result);
    }

    private static class WeightSequenceInfo {

        private final int weight;

        private final List<Item> elements;

        public WeightSequenceInfo(int weight) {
            this.weight = weight;
            this.elements = new ArrayList<>();
        }

        public void addItem(Item item) {
            assert item.getWeight() == weight;
            elements.add(item);
        }

        public List<Item> getMostProfitablePossibleSequence(int capacity) {
            if (elements.size() == 1) {
                return elements;
            }

            List<Item> itemsSortedByCost = new ArrayList<>(elements);
            itemsSortedByCost.sort(Comparator.comparingInt(Item::getCost));

            int limit = capacity / weight;

            if (limit > itemsSortedByCost.size()) {
                return itemsSortedByCost;
            }

            return itemsSortedByCost.subList(itemsSortedByCost.size() - limit, itemsSortedByCost.size());
        }
    }

    private record ItemsCostInfo(
            List<Item> items,
            int cost
    ) {

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            ItemsCostInfo that = (ItemsCostInfo) o;
            return cost == that.cost && Objects.equals(items, that.items);
        }

        @Override
        public int hashCode() {
            return Objects.hash(items, cost);
        }
    }

    private static class ItemSelectionResult {

        //todo если не нашлось ничего дельного в branchesMinElementExceptRoot - пытаемся уже резаться от этой ветки!
        private final Branch rootBranch;

        private final List<Branch> branches;

        private final Map<Integer, List<Branch>> branchesMinElementExceptRootMap;

        public ItemSelectionResult(Item rootItem) {
            this.branches = new ArrayList<>();
            this.branchesMinElementExceptRootMap = new HashMap<>();
            this.rootBranch = new Branch(rootItem);
            this.branches.add(rootBranch);
        }

        public boolean isEmpty() {
            return branches.isEmpty();
        }

        public ItemsCostInfo getBranchWithMaxCost() {
            int maxCost = -1;
            List<Item> branchElementsWithMaxCost = new ArrayList<>();

            for (Branch branch : branches) {
                int costOfBranch = branch.getBranchCost();
                if (maxCost < costOfBranch) {
                    maxCost = costOfBranch;
                    branchElementsWithMaxCost = branch.getAllItems();
                }
            }

            return new ItemsCostInfo(branchElementsWithMaxCost, maxCost);
        }

        public boolean put(Item newItem, int capacity) {
            Integer newItemWeight = newItem.getWeight();

            List<Branch> newBranches = new ArrayList<>();
            boolean successfulAdded = false;
            for (Map.Entry<Integer, List<Branch>> branchesMinElementExceptRootMapEntry : branchesMinElementExceptRootMap.entrySet()) {
                Integer minWeight = branchesMinElementExceptRootMapEntry.getKey();
                List<Branch> branchesWithMin = branchesMinElementExceptRootMapEntry.getValue();

                if (minWeight + newItemWeight < capacity) {
                    for (Branch branch : branchesWithMin) {
                        if (!branch.putInBranchAfterRoot(newItem, capacity)) {
                            Branch mostProfitableCompletion = branch.createMostProfitableCompletionWithItemsAfterRootAndNewItem(newItem, capacity);
                            if (mostProfitableCompletion == null) {
                                continue;
                            }

                            successfulAdded = true;
                            newBranches.add(mostProfitableCompletion);
                        } else {
                            successfulAdded = true;
                        }
                    }
                }
            }

            if (!newBranches.isEmpty()) {
                branches.addAll(newBranches);
                for (Branch branch : newBranches) {
                    branchesMinElementExceptRootMap.putIfAbsent(branch.getMinWeightAfterRoot(), new ArrayList<>());
                    branchesMinElementExceptRootMap.get(branch.getMinWeightAfterRoot()).add(branch);
                }
            }

            if (successfulAdded) {
                return true;
            }

            Branch newBranchFromRoot = new Branch(rootBranch);
            if (!newBranchFromRoot.putInBranchAfterRoot(newItem, capacity)) {
                return false;
            }

            branches.add(newBranchFromRoot);
            branchesMinElementExceptRootMap.putIfAbsent(newBranchFromRoot.getMinWeightAfterRoot(), new ArrayList<>());
            branchesMinElementExceptRootMap.get(newBranchFromRoot.getMinWeightAfterRoot()).add(newBranchFromRoot);

            return true;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            ItemSelectionResult that = (ItemSelectionResult) o;
            return Objects.equals(branches, that.branches);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(branches);
        }
    }

    private static class Branch {

        private final Item rootItem;

        private final List<Item> itemsAfterRoot;

        private int branchWeight;

        private int branchCost;

        private Integer minWeightAfterRoot;

        public Branch(Item rootItem) {
            this.rootItem = rootItem;
            this.itemsAfterRoot = new ArrayList<>();
            this.branchWeight = rootItem.getWeight();
            this.branchCost = rootItem.getCost();
            this.minWeightAfterRoot = null;
        }

        public Branch(Branch other) {
            this.rootItem = other.rootItem;
            this.itemsAfterRoot = new ArrayList<>(other.itemsAfterRoot);
            this.branchWeight = other.getBranchWeight();
            this.branchCost = other.getBranchCost();
            this.minWeightAfterRoot = other.getMinWeightAfterRoot();
        }

        private Branch(Item rootItem, List<Item> itemsAfterRoot) {
            this.rootItem = rootItem;
            this.itemsAfterRoot = itemsAfterRoot;
            this.branchCost = rootItem.getCost() + itemsAfterRoot.stream().map(Item::getCost).reduce(Integer::sum).orElse(0);
            this.branchWeight = rootItem.getWeight() + itemsAfterRoot.stream().map(Item::getWeight).reduce(Integer::sum).orElse(0);
            this.minWeightAfterRoot = itemsAfterRoot.stream().map(Item::getWeight).min(Integer::compareTo).orElse(null);
        }

        public Integer getBranchCost() {
            return branchCost;
        }

        public Integer getBranchWeight() {
            return branchWeight;
        }

        public List<Item> getAllItems() {
            List<Item> result = new ArrayList<>();
            result.add(rootItem);
            result.addAll(itemsAfterRoot);

            return result;
        }

        public Integer getMinWeightAfterRoot() {
            return minWeightAfterRoot;
        }

        public Branch createMostProfitableCompletionWithItemsAfterRootAndNewItem(Item item, int capacity) {
            //todo логика сильно тормозит на большом тесте!
            //todo возможно, в т.ч. из за того, что мы все-таки заходим сюда, когда не можем распределять. видимо, проверка с minWeight не работает так, как предполагалось
            //todo эта логика в любом случае подразумевается дорогой, чем меньше ее трогаем - тем лучше
            if (itemsAfterRoot.isEmpty()) {
                //todo для резанья от рутовой ветки этот метод предназначен не будет!
                return null;
            }

            if (branchWeight + item.getWeight() <= capacity) {
                this.putInBranchAfterRoot(item, capacity);
                return this;
            }

            if (minWeightAfterRoot + item.getWeight() >= capacity) {
                //todo мы априори такое не укопмлектуем с afterRoot, а если нам нужен только root - это вообще отдельный кейс пусть будет пока
                return null;
            }

            int freeSpaceNeeded = item.getWeight() - (capacity - branchWeight);

            int minLoss = Integer.MAX_VALUE;
            int minLossIndex = -1;

            int rootWeight = rootItem.getWeight();

            List<List<Item>> possibleCompletionsWithLoss = new ArrayList<>();

            for (int i = 0; i < itemsAfterRoot.size(); i++) {
                List<Item> currentCompletion = new ArrayList<>();
                Item currentItem = itemsAfterRoot.get(i);
                currentCompletion.add(currentItem);
                int currLoad = rootWeight;
                currLoad += currentItem.getWeight();

                int currCost = currentItem.getCost();

                if (currLoad + item.getWeight() > capacity) {
                    continue;
                }

                if (currLoad >= freeSpaceNeeded) {
                    possibleCompletionsWithLoss.add(new ArrayList<>(currentCompletion));
                    if (currCost < minLoss) {
                        minLoss = currCost;
                        minLossIndex = possibleCompletionsWithLoss.size() - 1;
                    }

                    continue;
                }

                for (int j = i + 1; j < itemsAfterRoot.size(); j++) {
                    Item otherItem = itemsAfterRoot.get(j);
                    currentCompletion.add(otherItem);
                    currLoad += otherItem.getWeight();
                    currCost += otherItem.getCost();

                    if (currLoad + item.getWeight() > capacity) {
                        break;
                    }

                    if (currLoad >= freeSpaceNeeded) {
                        possibleCompletionsWithLoss.add(new ArrayList<>(currentCompletion));
                        if (currCost < minLoss) {
                            minLoss = currCost;
                            minLossIndex = possibleCompletionsWithLoss.size() - 1;
                        }
                        currentCompletion.removeLast();
                        currLoad -= otherItem.getWeight();
                        currCost -= otherItem.getCost();
                    }
                }
            }

            if (minLossIndex == -1) {
                return null;
            }

            List<Item> mostProfitableItemsAfterRoot = possibleCompletionsWithLoss.get(minLossIndex);
            mostProfitableItemsAfterRoot.add(item);

            return new Branch(rootItem, mostProfitableItemsAfterRoot);
//            int needed = capacity - item.getWeight();
//
//            int rootWeight = rootItem.getWeight();
//
//            List<List<Item>> possibleCompletionsWithLoss = new ArrayList<>();
//
//            int minLoss = Integer.MAX_VALUE;
//            int minLossIndex = -1;
//
//            // todo пока в тупую
//            for (int i = 0; i < itemsAfterRoot.size(); i++) {
//                List<Item> currentCompletion = new ArrayList<>();
//                Item currentItem = itemsAfterRoot.get(i);
//                currentCompletion.add(currentItem);
//                int currLoad = rootWeight;
//                currLoad += currentItem.getWeight();
//                int currCost = currentItem.getCost();
//
//                if (currLoad >= needed) {
//                    possibleCompletionsWithLoss.add(new ArrayList<>(currentCompletion));
//
//                    if (currCost < minLoss) {
//                        minLoss = currCost;
//                        minLossIndex = possibleCompletionsWithLoss.size() - 1;
//                    }
//                }
//
//                for (int j = i + 1; j < itemsAfterRoot.size(); j++) {
//                    Item otherItem = itemsAfterRoot.get(j);
//                    currentCompletion.add(otherItem);
//                    currLoad += otherItem.getWeight();
//                    currCost += otherItem.getCost();
//
//                    if (currLoad >= needed) {
//                        possibleCompletionsWithLoss.add(new ArrayList<>(currentCompletion));
//
//                        if (currCost < minLoss) {
//                            minLoss = currCost;
//                            minLossIndex = possibleCompletionsWithLoss.size() - 1;
//                        }
//                    }
//                }
//            }
//
//            if (minLossIndex == -1) {
//                return null;
//            }
//
//            List<Item> mostProfitableItemsAfterRoot = possibleCompletionsWithLoss.get(minLossIndex);
//            mostProfitableItemsAfterRoot.add(item);
//
//            return new Branch(rootItem, mostProfitableItemsAfterRoot);
        }

        public boolean putInBranchAfterRoot(Item item, int capacity) {
            assert !Objects.equals(item, rootItem);

            if (branchWeight + item.getWeight() > capacity) {
                return false;
            }

            itemsAfterRoot.add(item);

            branchWeight += item.getWeight();
            branchCost += item.getCost();
            if (minWeightAfterRoot == null) {
                minWeightAfterRoot = item.getWeight();
                return true;
            }

            if (item.getWeight() < minWeightAfterRoot) {
                minWeightAfterRoot = item.getWeight();
            }

            return true;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            Branch branch = (Branch) o;
            return Objects.equals(itemsAfterRoot, branch.itemsAfterRoot) && Objects.equals(branchWeight, branch.branchWeight) && Objects.equals(branchCost, branch.branchCost);
        }

        @Override
        public int hashCode() {
            return Objects.hash(itemsAfterRoot, branchWeight, branchCost);
        }
    }
}
