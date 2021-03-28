package de.thb.schiller.mad2doplanner.model.sorting;

import static java.util.Arrays.asList;

import java.util.Comparator;
import java.util.List;

import de.thb.schiller.mad2doplanner.model.entities.TodoItem;

/**
 * @author Dominic Schiller
 * @since 15.06.17
 *
 * This comparator is a sort comparator for ordering to-do items in a list which chains multiple sort criteria together.
 */

public class TodoItemComparator implements Comparator<TodoItem> {

    static SortDirection sortDirection;
    private static TodoItemComparator instance;

    private final List<Comparator<TodoItem>> mComparators;
    private static boolean isReversed = false;

    /**
     * Constructor
     */
    private TodoItemComparator() {
        // initializes the comparator chain
        this.mComparators = asList(
                new TodoItemIsDoneComparator(),
                new TodoImportanceComparator(),
                new TodoDueDateComparator()
        );
    }

    /**
     * Get the comparator instance.
     * @return The singleton comparator instance
     */
    public static TodoItemComparator getInstance() {
        if(isReversed) {
            return (TodoItemComparator) getInstance(SortDirection.DESCENDING).reversed();
        }

        return getInstance(SortDirection.DESCENDING);
    }

    /**
     * Get the comparator instance with defined sort direction.
     * @param sortDirection The sort direction at which to sort
     * @return The singleton comparator instance
     */
    public static TodoItemComparator getInstance(SortDirection sortDirection) {
        TodoItemComparator.sortDirection = sortDirection;

        if(instance == null) {
            instance = new TodoItemComparator();
        }

        return instance;
    }

    /**
     * Compares two to-do items and return a order index.
     * @param item1 The first to-do item
     * @param item2 The second to-do item
     * @return The order index.
     *
     * @see TodoItem
     */
    @Override
    public int compare(TodoItem item1, TodoItem item2) {
        for(Comparator<TodoItem> comparator : mComparators) {
            int result = comparator.compare(item1, item2);

            if(result != 0) {
                return result;
            }
        }

        return 0;
    }

    /**
     * Get a reversed comparator with swapped sort criteria.
     * @return The reversed comparator.
     */
    @Override
    public Comparator<TodoItem> reversed() {
        // swap comparators at index 1 and 2
        Comparator<TodoItem> tmpComparator = mComparators.get(1);
        mComparators.set(1, mComparators.get(2));
        mComparators.set(2, tmpComparator);

        isReversed = !isReversed;

        return this;
    }

    /**
     * Comparator for comparing the isDone field of two to-do items.
     */
    private static class TodoItemIsDoneComparator implements Comparator<TodoItem> {

        /**
         * Compare two to-do items by it's isDone field.
         * @param item1 The first to-do item
         * @param item2 The second to-do item
         * @return The order index.
         *
         * @see TodoItem
         */
        @Override
        public int compare(TodoItem item1, TodoItem item2) {
            return Boolean.compare(item1.isDone(), item2.isDone());
        }
    }

    /**
     * Comparator for comparing the isImportant field of two to-do items.
     */
    private static class TodoImportanceComparator implements Comparator<TodoItem> {

        /**
         * Compare two to-do items by it's priority field
         * @param item1 The first to-do item
         * @param item2 The second to-do item
         * @return The order index.
         *
         * @see TodoItem
         */
        @Override
        public int compare(TodoItem item1, TodoItem item2) {
            switch(sortDirection) {
                case ASCENDING:
                    return (item1.isImportant() ? 1 : 0) - (item2.isImportant() ? 1 : 0);
                case DESCENDING:
                    return (item2.isImportant() ? 1 : 0) - (item1.isImportant() ? 1 : 0);
                default:
                    return 0;
            }
        }
    }

    /**
     * Comparator for comparing the dueDate field of two to-do items.
     */
    private static class TodoDueDateComparator implements Comparator<TodoItem> {

        /**
         * Compare two to-do items by it's dueDate field
         * @param item1 The first to-do item
         * @param item2 The second to-do item
         * @return The order index.
         *
         * @see TodoItem
         */
        @Override
        public int compare(TodoItem item1, TodoItem item2) {
            switch(sortDirection) {
                case ASCENDING:
                    return Long.compare(item2.getDueDate(), item1.getDueDate());
                case DESCENDING:
                    return Long.compare(item1.getDueDate(), item2.getDueDate());
                default:
                    return 0;
            }
        }
    }
}
