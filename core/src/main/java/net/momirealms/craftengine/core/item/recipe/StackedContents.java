package net.momirealms.craftengine.core.item.recipe;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

public final class StackedContents<T> {
    public final Reference2IntOpenHashMap<T> amounts = new Reference2IntOpenHashMap<>();

    public void add(T input, int count) {
        this.amounts.addTo(input, count);
    }

    void take(T input, int count) {
        int previous = this.amounts.addTo(input, -count);
        if (previous < count) {
            throw new IllegalStateException("Took " + count + " items, but only had " + previous);
        }
    }

    void put(T input, int count) {
        this.amounts.addTo(input, count);
    }

    boolean hasAtLeast(T input, int minimum) {
        return this.amounts.getInt(input) >= minimum;
    }

    public boolean tryPick(List<? extends StackedContents.IngredientInfo<T>> ingredients) {
        return new Matcher(ingredients).tryPick(1);
    }

    @FunctionalInterface
    public interface IngredientInfo<T> {
        boolean acceptsItem(T entry);
    }

    List<T> getUniqueAvailableIngredientItems(List<? extends IngredientInfo<T>> ingredients) {
        List<T> list = new ArrayList<>();
        for (var entry : amounts.reference2IntEntrySet()) {
            if (entry.getIntValue() > 0 && anyIngredientMatches(ingredients, entry.getKey())) {
                list.add(entry.getKey());
            }
        }
        return list;
    }

    private static <T> boolean anyIngredientMatches(Iterable<? extends IngredientInfo<T>> ingredients, T item) {
        for (IngredientInfo<T> ingredientInfo : ingredients) {
            if (ingredientInfo.acceptsItem(item)) {
                return true;
            }
        }
        return false;
    }

    public class Matcher {
        private final List<? extends IngredientInfo<T>> ingredients;
        private final int ingredientCount;
        private final List<T> items;
        private final int itemCount;
        private final BitSet connections;
        private final BitSet assigned;
        private final boolean[] visitedIngredients;
        private final boolean[] visitedItems;
        private final boolean[] satisfied;

        private final IntList path = new IntArrayList();

        public Matcher(List<? extends IngredientInfo<T>> ingredients) {
            this.ingredients = ingredients;
            this.ingredientCount = ingredients.size();
            this.items = getUniqueAvailableIngredientItems(ingredients);
            this.itemCount = this.items.size();

            int connectionCount = this.ingredientCount * this.itemCount;
            this.connections = new BitSet(connectionCount);
            this.assigned = new BitSet(connectionCount);

            this.visitedIngredients = new boolean[this.ingredientCount];
            this.visitedItems = new boolean[this.itemCount];
            this.satisfied = new boolean[this.ingredientCount];

            this.buildConnections();
        }

        private void buildConnections() {
            for (int ingredientIndex = 0; ingredientIndex < this.ingredientCount; ingredientIndex++) {
                IngredientInfo<T> ingredientInfo = this.ingredients.get(ingredientIndex);
                for (int itemIndex = 0; itemIndex < this.itemCount; itemIndex++) {
                    if (ingredientInfo.acceptsItem(this.items.get(itemIndex))) {
                        this.setConnection(itemIndex, ingredientIndex);
                    }
                }
            }
        }

        @Nullable
        private IntList tryAssigningNewItem(int min) {
            this.clearAllVisited();
            for (int i = 0; i < this.itemCount; i++) {
                if (hasAtLeast(this.items.get(i), min)) {
                    IntList intList = this.findNewItemAssignmentPath(i);
                    if (intList != null) {
                        return intList;
                    }
                }
            }
            return null;
        }

        @Nullable
        private IntList findNewItemAssignmentPath(int itemIndex) {
            this.path.clear();
            this.visitItem(itemIndex);
            this.path.add(itemIndex);

            while (!this.path.isEmpty()) {
                int currentPathSize = this.path.size();
                if (isPathIndexItem(currentPathSize - 1)) {
                    int currentItem = this.path.getInt(currentPathSize - 1);
                    for (int ingredientIndex = 0; ingredientIndex < this.ingredientCount; ingredientIndex++) {
                        if (!this.hasVisitedIngredient(ingredientIndex) &&
                                this.hasConnection(currentItem, ingredientIndex) &&
                                !this.isAssigned(currentItem, ingredientIndex)) {
                            this.visitIngredient(ingredientIndex);
                            this.path.add(ingredientIndex);
                            break;
                        }
                    }
                } else {
                    int currentIngredient = this.path.getInt(currentPathSize - 1);
                    if (!this.isSatisfied(currentIngredient)) {
                        return this.path;
                    }
                    for (int itemIndexCandidate = 0; itemIndexCandidate < this.itemCount; itemIndexCandidate++) {
                        if (!this.hasVisitedItem(itemIndexCandidate) &&
                                this.isAssigned(itemIndexCandidate, currentIngredient)) {
                            assert this.hasConnection(itemIndexCandidate, currentIngredient);

                            this.visitItem(itemIndexCandidate);
                            this.path.add(itemIndexCandidate);
                            break;
                        }
                    }
                }
                int newPathSize = this.path.size();
                if (newPathSize == currentPathSize) {
                    this.path.removeInt(newPathSize - 1);
                }
            }
            return null;
        }

        public boolean tryPick(int quantity) {
            int assignedIngredientsCount = 0;
            for (;;) {
                IntList assignmentPath = this.tryAssigningNewItem(quantity);
                if (assignmentPath == null) {
                    boolean allIngredientsTried = assignedIngredientsCount == this.ingredientCount;
                    this.clearAllVisited();
                    Arrays.fill(this.satisfied, false);

                    for (int ingredientIndex = 0; ingredientIndex < this.ingredientCount; ingredientIndex++) {
                        for (int itemIndex = 0; itemIndex < this.itemCount; itemIndex++) {
                            if (this.isAssigned(itemIndex, ingredientIndex)) {
                                this.unassign(itemIndex, ingredientIndex);
                                StackedContents.this.put(this.items.get(itemIndex), quantity);
                                break;
                            }
                        }
                    }

                    assert this.assigned.nextSetBit(0) == -1;
                    return allIngredientsTried;
                }

                int firstItemIndex = assignmentPath.getInt(0);
                StackedContents.this.take(this.items.get(firstItemIndex), quantity);

                int lastIngredientIndex = assignmentPath.size() - 1;
                this.setSatisfied(assignmentPath.getInt(lastIngredientIndex));
                assignedIngredientsCount++;

                for (int pathIndex = 0; pathIndex < assignmentPath.size() - 1; pathIndex++) {
                    if (isPathIndexItem(pathIndex)) {
                        int itemIndex = assignmentPath.getInt(pathIndex);
                        int ingredientIndex = assignmentPath.getInt(pathIndex + 1);
                        this.assign(itemIndex, ingredientIndex);
                    } else {
                        int ingredientIndex = assignmentPath.getInt(pathIndex + 1);
                        int itemIndex = assignmentPath.getInt(pathIndex);
                        this.unassign(itemIndex, ingredientIndex);
                    }
                }
            }
        }

        private static boolean isPathIndexItem(int index) {
            return (index & 1) == 0;
        }

        private void setConnection(int itemIndex, int ingredientIndex) {
            this.connections.set(this.getConnectionIndex(itemIndex, ingredientIndex));
        }

        private boolean hasConnection(int itemIndex, int ingredientIndex) {
            return this.connections.get(this.getConnectionIndex(itemIndex, ingredientIndex));
        }

        private int getConnectionIndex(int itemIndex, int ingredientIndex) {
            assert itemIndex >= 0 && itemIndex < this.itemCount;
            assert ingredientIndex >= 0 && ingredientIndex < this.ingredientCount;
            return itemIndex * this.ingredientCount + ingredientIndex;
        }

        private boolean isAssigned(int itemIndex, int ingredientIndex) {
            return this.assigned.get(this.getConnectionIndex(itemIndex, ingredientIndex));
        }

        private void assign(int itemIndex, int ingredientIndex) {
            int i = this.getConnectionIndex(itemIndex, ingredientIndex);
            assert !this.assigned.get(i);
            this.assigned.set(i);
        }

        private void unassign(int itemIndex, int ingredientIndex) {
            int i = this.getConnectionIndex(itemIndex, ingredientIndex);
            assert this.assigned.get(i);
            this.assigned.clear(i);
        }

        private boolean isSatisfied(int ingredientIndex) {
            return this.satisfied[ingredientIndex];
        }

        private void setSatisfied(int ingredientIndex) {
            this.satisfied[ingredientIndex] = true;
        }

        private void visitIngredient(int index) {
            this.visitedIngredients[index] = true;
        }

        private boolean hasVisitedIngredient(int index) {
            return this.visitedIngredients[index];
        }

        private void visitItem(int index) {
            this.visitedItems[index] = true;
        }

        private boolean hasVisitedItem(int index) {
            return this.visitedItems[index];
        }

        private void clearAllVisited() {
            Arrays.fill(this.visitedIngredients, false);
            Arrays.fill(this.visitedItems, false);
        }
    }
}
