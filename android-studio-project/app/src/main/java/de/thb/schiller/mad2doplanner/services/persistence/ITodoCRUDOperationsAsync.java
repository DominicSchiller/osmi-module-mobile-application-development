package de.thb.schiller.mad2doplanner.services.persistence;

import java.util.List;
import java.util.function.Consumer;

import de.thb.schiller.mad2doplanner.model.entities.TodoItem;

/**
 * @author Dominic Schiller
 * @since 03.07.17
 *
 * This interface defines asynchronous CRUD operations on TodoItems.
 */

interface ITodoCRUDOperationsAsync {

    //region C ... CREATE
    /**
     * Creates an object of type TodoItem
     * @param todoItem The to-do item to create
     * @param delegate The delegate to callback after process has been finished
     */
    void createTodoItem(TodoItem todoItem, Consumer<TodoItem> delegate);

    //endregion


    //region R ... READ

    /**
     * Reads all available TodoItems
     * @param delegate The delegate to callback after process has been finished
     */
    void readAllTodoItems(Consumer<List<TodoItem>> delegate);

    /**
     * Reads a specific TodoItem
     * @param id The TodoItem's specific identifier
     * @param delegate The delegate to callback after process has been finished
     */
    void readTodoItem(long id, Consumer<TodoItem> delegate);

    //endregion


    //region U ... Update

    /**
     * Updates a specific TodoItem
     * @param todoItem The TodoItem to update
     * @param delegate The delegate to callback after process has been finished
     */
    void updateTodoItem(TodoItem todoItem, Consumer<TodoItem> delegate);

    //endregion


    //region D ... Delete

    /**
     * Deletes a specific TodoItem.
     * @param id The the to-do item's id
     * @param delegate The delegate to callback after process has been finished
     */
    void deleteTodoItem(long id, Consumer<Boolean> delegate);

    //endregion
}
