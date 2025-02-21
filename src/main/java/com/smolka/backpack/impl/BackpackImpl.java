package com.smolka.backpack.impl;

import com.smolka.backpack.Backpack;
import com.smolka.backpack.Item;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
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

        Set<Item> itemsWithoutOverWeightedElements = items.stream().filter(item -> item.getWeight() <= capacity).collect(Collectors.toSet());

        List<Item> sortedItems = new ArrayList<>(itemsWithoutOverWeightedElements.stream().toList());
        sortedItems.sort(Comparator.comparingInt(Item::getWeight));

        List<ItemSelectionResult> selectionResults = new ArrayList<>();
        for (int i = 0; i < sortedItems.size(); i++) {
            selectionResults.add(getSelectionResultFromIndex(i, sortedItems));
        }

        int maxCost = -1;
        List<Item> branchWithMaxCost = new ArrayList<>();
        for (ItemSelectionResult selectionResult : selectionResults) {
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

    private ItemSelectionResult getSelectionResultFromIndex(int index, List<Item> items) {
        ItemSelectionResult result = new ItemSelectionResult(items.get(index));
        for (int i = index + 1; i < items.size(); i++) {
            Item nextItem = items.get(i);
            int newWeight = result.getWeightOfLastBranch() + nextItem.getWeight();
            if (newWeight > capacity) {
                List<Item> newBranch = result.createCapableBranchFromLast(nextItem, capacity);
                if (newBranch.isEmpty()) {
                    return result;
                }

                result.addNewBranch(newBranch);
            } else {
                result.addInLastBranch(nextItem);
            }
        }

        return result;
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

        private final List<List<Item>> branches;

        public ItemSelectionResult(Item rootItem) {
            this.branches = new ArrayList<>();
            this.branches.add(new ArrayList<>(List.of(rootItem)));
        }

        public List<Item> getLastBranch() {
            return branches.getLast();
        }

        public ItemsCostInfo getBranchWithMaxCost() {
            int maxCost = -1;
            List<Item> branchWithMaxCost = new ArrayList<>();

            for (List<Item> branch : branches) {
                int costOfBranch = getCostOfBranch(branch);
                if (maxCost < costOfBranch) {
                    maxCost = costOfBranch;
                    branchWithMaxCost = branch;
                }
            }

            return new ItemsCostInfo(branchWithMaxCost, maxCost);
        }

        public List<Item> createCapableBranchFromLast(Item newItem, int capacity) {
            int newWeight = newItem.getWeight();
            List<Item> lastBranchCopy = new ArrayList<>(getLastBranch());
            if (getWeightOfLastBranch() + newWeight <= capacity) {
                lastBranchCopy.add(newItem);
                return lastBranchCopy;
            }

            while (!lastBranchCopy.isEmpty() && getWeightOfBranch(lastBranchCopy) + newWeight > capacity) {
                lastBranchCopy.removeLast();
                if (lastBranchCopy.isEmpty()) {
                    return lastBranchCopy;
                }
            }

            lastBranchCopy.add(newItem);

            return lastBranchCopy;
        }

        public int getWeightOfLastBranch() {
            return getWeightOfBranch(getLastBranch());
        }

        public void addInLastBranch(Item item) {
            getLastBranch().add(item);
        }

        public void addNewBranch(List<Item> items) {
            branches.add(items);
        }

        private int getWeightOfBranch(List<Item> branch) {
            return branch.stream().map(Item::getWeight).reduce(Integer::sum).orElse(0);
        }

        private int getCostOfBranch(List<Item> branch) {
            return branch.stream().map(Item::getCost).reduce(Integer::sum).orElse(0);
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
}
