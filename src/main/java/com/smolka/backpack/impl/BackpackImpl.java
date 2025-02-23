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

    private static int COUNT = 0;

    private static int COUNT_FAIL = 0;

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
        List<ItemAnalyzeResult> itemAnalyzeResults = new ArrayList<>();

        for (int i = 0; i < preProcessedItems.size(); i++) {
            itemAnalyzeResults.add(getItemAnalyzeResult(i, preProcessedItems));
        }

        itemAnalyzeResults = itemAnalyzeResults;

        //todo надо попробовать принципиально поменять подход
        /*
        123456789
        для единицы
        1234
        15
        16
        17
        18
        19
        при W = 10
        как-то соотносить между собой, получать наиболее профитное, до тех пор пока не выйдем на супер-профит.
        по сути наверное некоторое возвращение к начальному плану, только теперь с учетом того, что нельзя забывать про предыдущие значения!!!
        потиху и без фанатизма копать в эту сторону. уже есть неплохой метод в бранче - createMostProfitableCompletionWithItemsAfterRootAndNewItem. тут будет что-то подобное, но выше уровнем, с ветками, а не по одному.
        пока отдыхать и тихонько думать.
        * **/
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

    private ItemAnalyzeResult getItemAnalyzeResult(int index, List<Item> preProcessedItems) {
        assert index < preProcessedItems.size();

        Item rootItem = preProcessedItems.get(index);
        ItemAnalyzeResult result = new ItemAnalyzeResult(rootItem);
        Branch branch = new Branch(rootItem);
        result.addNewBranch(new Branch(branch));

        for (int i = index + 1; i < preProcessedItems.size(); i++) {
            Item otherItem = preProcessedItems.get(i);
            boolean putResult = branch.putInBranchAfterRoot(otherItem, capacity);
            if (!putResult || i == preProcessedItems.size() - 1) {
                result.addNewBranch(new Branch(branch));

                if (!putResult) {
                    branch = new Branch(rootItem);
                    i--;
                }
            }
        }

        return result;
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

    private static class ItemAnalyzeResult {

        private final Item rootItem;

        private final List<Branch> branches;

        public ItemAnalyzeResult(Item rootItem) {
            this.rootItem = rootItem;
            this.branches = new ArrayList<>();
        }

        public void addNewBranch(Branch branch) {
            assert Objects.equals(branch.getRootItem(), rootItem);
            branches.add(branch);
        }

        public Branch getMostProfitableBranch(int capacity) {
            return null;
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

        public Item getRootItem() {
            return rootItem;
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

            COUNT++;

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
                COUNT_FAIL++;
                return null;
            }

            List<Item> mostProfitableItemsAfterRoot = possibleCompletionsWithLoss.get(minLossIndex);
            mostProfitableItemsAfterRoot.add(item);

            return new Branch(rootItem, mostProfitableItemsAfterRoot);
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
