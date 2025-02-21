package com.smolka.backpack.impl;

import com.smolka.backpack.Backpack;
import com.smolka.backpack.Item;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
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

        Set<Item> itemsWithoutOverWeightedElements = items.stream().filter(item -> item.getWeight() <= capacity).collect(Collectors.toSet());

        List<Item> sortedItems = new ArrayList<>(itemsWithoutOverWeightedElements.stream().toList());
        sortedItems.sort(Comparator.comparingInt(Item::getWeight));

        List<ItemSelectionResult> selectionResults = new ArrayList<>();
        for (int i = 0; i < sortedItems.size(); i++) {
            addSelectionResultForIndex(i, sortedItems, selectionResults);
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

    private void addSelectionResultForIndex(int index, List<Item> items, List<ItemSelectionResult> otherResults) {
        ItemSelectionResult result = new ItemSelectionResult(items.get(index));
        for (int i = index + 1; i < items.size(); i++) {
            System.out.println("*");
            Item nextItem = items.get(i);
            int newWeight = result.getWeightOfLastBranch() + nextItem.getWeight();
            if (newWeight > capacity) {
                Branch newBranch = result.createCapableBranchFromLast(nextItem, capacity);
                if (newBranch.isEmpty()) {
                    otherResults.add(postProcessResultAndReturn(result, otherResults));
                    return;
                }

                result.addNewBranch(newBranch);
            } else {
                result.addInLastBranch(nextItem);
            }
        }

        otherResults.add(postProcessResultAndReturn(result, otherResults));
    }

    private ItemSelectionResult postProcessResultAndReturn(ItemSelectionResult result, List<ItemSelectionResult> otherResults) {
        Set<UUID> branchesToRemove = new HashSet<>();
        for (Branch branch : result.getBranches()) {
            for (ItemSelectionResult otherResult : otherResults) {
                if (otherResult.containsAsSubCombination(branch.getItemsCombination())) {
                    branchesToRemove.add(branch.getUuid());
                    break;
                }
            }
        }

        result.removeAllBranchesByUuids(branchesToRemove);
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

        public List<Branch> getBranches() {
            return branches;
        }

        public boolean containsAsSubCombination(Set<Item> items) {
            for (Branch branch : branches) {
                if (branch.combinationContainsAll(items)) {
                    return true;
                }
            }

            return false;
        }

        public void removeAllBranchesByUuids(Set<UUID> toRemove) {
            branches.removeIf(b -> toRemove.contains(b.getUuid()));
        }

        public ItemsCostInfo getBranchWithMaxCost() {
            int maxCost = -1;
            List<Item> branchElementsWithMaxCost = new ArrayList<>();

            for (Branch branch : branches) {
                List<Item> branchElements = branch.getItemsInOrder();
                int costOfBranch = getCostOfBranchElements(branchElements);
                if (maxCost < costOfBranch) {
                    maxCost = costOfBranch;
                    branchElementsWithMaxCost = branchElements;
                }
            }

            return new ItemsCostInfo(branchElementsWithMaxCost, maxCost);
        }

        public Branch createCapableBranchFromLast(Item newItem, int capacity) {
            int newWeight = newItem.getWeight();
            List<Item> lastBranchElementsCopy = new ArrayList<>(getLastBranch().getItemsInOrder());
            if (getWeightOfLastBranch() + newWeight <= capacity) {
                lastBranchElementsCopy.add(newItem);
                return new Branch(lastBranchElementsCopy);
            }

            while (!lastBranchElementsCopy.isEmpty() && getWeightOfBranchElements(lastBranchElementsCopy) + newWeight > capacity) {
                lastBranchElementsCopy.removeLast();
                if (lastBranchElementsCopy.isEmpty()) {
                    return new Branch(lastBranchElementsCopy);
                }
            }

            lastBranchElementsCopy.add(newItem);

            return new Branch(lastBranchElementsCopy);
        }

        public int getWeightOfLastBranch() {
            return getWeightOfBranchElements(getLastBranch().getItemsInOrder());
        }

        public void addInLastBranch(Item item) {
            getLastBranch().addInBranch(item);
        }

        public void addNewBranch(Branch branch) {
            branches.add(branch);
        }

        private int getWeightOfBranchElements(List<Item> branch) {
            return branch.stream().map(Item::getWeight).reduce(Integer::sum).orElse(0);
        }

        private int getCostOfBranchElements(List<Item> branch) {
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

    private static class Branch {

        private final UUID uuid;

        private final List<Item> itemsInOrder;

        private final Set<Item> itemsCombination;

        public Branch() {
            this.uuid = UUID.randomUUID();
            this.itemsInOrder = new ArrayList<>();
            this.itemsCombination = new HashSet<>();
        }

        public Branch(List<Item> items) {
            this.uuid = UUID.randomUUID();
            this.itemsInOrder = items;
            this.itemsCombination = new HashSet<>(items);
        }

        public UUID getUuid() {
            return uuid;
        }

        public List<Item> getItemsInOrder() {
            return itemsInOrder;
        }

        public Set<Item> getItemsCombination() {
            return itemsCombination;
        }

        public boolean combinationContainsAll(Set<Item> otherItems) {
            return itemsCombination.containsAll(otherItems);
        }

        public void addInBranch(Item item) {
            this.itemsInOrder.add(item);
            this.itemsCombination.add(item);
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
