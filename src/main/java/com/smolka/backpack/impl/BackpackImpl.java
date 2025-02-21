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
import java.util.UUID;
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
            int newWeight = result.getLastBranch().getWeight() + nextItem.getWeight();
            if (newWeight > capacity) {
                Branch newBranch = result.createCapableBranchFromLast(nextItem, capacity);
                if (newBranch.isEmpty()) {
                    otherResults.add(result);
                    return;
                }

                result.addNewBranch(newBranch);
            } else {
                result.addInLastBranch(nextItem);
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

        private final List<Branch> branches;

        public ItemSelectionResult(Item rootItem) {
            this.branches = new ArrayList<>();
            this.branches.add(new Branch());
            this.getLastBranch().addInBranch(rootItem);
        }

        public boolean isEmpty() {
            return branches.isEmpty();
        }

        public Branch getLastBranch() {
            return branches.getLast();
        }

        public ItemsCostInfo getBranchWithMaxCost() {
            int maxCost = -1;
            List<Item> branchElementsWithMaxCost = new ArrayList<>();

            for (Branch branch : branches) {
                int costOfBranch = branch.getCost();
                if (maxCost < costOfBranch) {
                    maxCost = costOfBranch;
                    branchElementsWithMaxCost = branch.getItemsInOrder();
                }
            }

            return new ItemsCostInfo(branchElementsWithMaxCost, maxCost);
        }

        public Branch createCapableBranchFromLast(Item newItem, int capacity) {
            int newWeight = newItem.getWeight();
            int weightOfLastElement = getLastBranch().getWeight();

            Branch lastBranchCopy = getLastBranch().getCopy();
            if (weightOfLastElement + newWeight <= capacity) {
                lastBranchCopy.addInBranch(newItem);
                return lastBranchCopy;
            }

            while (!lastBranchCopy.isEmpty() && weightOfLastElement + newWeight > capacity) {
                lastBranchCopy.removeLast();
                if (lastBranchCopy.isEmpty()) {
                    return lastBranchCopy;
                }
                weightOfLastElement = lastBranchCopy.getWeight();
            }

            lastBranchCopy.addInBranch(newItem);

            return lastBranchCopy;
        }

        public void addInLastBranch(Item item) {
            getLastBranch().addInBranch(item);
        }

        public void addNewBranch(Branch branch) {
            branches.add(branch);
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

        private final UUID uuid;

        private final List<Item> itemsInOrder;

        private Integer cost;

        private Integer weight;

        public Branch() {
            this.uuid = UUID.randomUUID();
            this.itemsInOrder = new ArrayList<>();
            this.cost = 0;
            this.weight = 0;
        }

        public Branch(List<Item> items) {
            this.uuid = UUID.randomUUID();
            this.itemsInOrder = items;
            this.cost = items.stream().map(Item::getCost).reduce(Integer::sum).orElse(0);
            this.weight = items.stream().map(Item::getWeight).reduce(Integer::sum).orElse(0);
        }

        public Branch getCopy() {
            return new Branch(new ArrayList<>(itemsInOrder));
        }

        public Integer getWeight() {
            return weight;
        }

        public Integer getCost() {
            return cost;
        }

        public List<Item> getItemsInOrder() {
            return itemsInOrder;
        }

        public void removeLast() {
            int weightOfLastElement = itemsInOrder.getLast().getWeight();
            int costOfLastElement = itemsInOrder.getLast().getCost();

            this.itemsInOrder.removeLast();
            this.weight -= weightOfLastElement;
            this.cost -= costOfLastElement;
        }

        public void addInBranch(Item item) {
            this.itemsInOrder.add(item);
            this.cost += item.getCost();
            this.weight += item.getWeight();
        }

        public boolean isEmpty() {
            return this.itemsInOrder.isEmpty();
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            Branch branch = (Branch) o;
            return Objects.equals(uuid, branch.uuid);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(uuid);
        }
    }
}
