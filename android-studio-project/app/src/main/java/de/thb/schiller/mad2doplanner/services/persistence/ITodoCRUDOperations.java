package de.thb.schiller.mad2doplanner.services.persistence;

import java.util.List;

import de.thb.schiller.mad2doplanner.model.entities.TodoItem;

/**
 * @author Dominic Schiller
 * @since 25.06.17.
 *
 * This interface defines all allowed CRUD operations on TodoItems
 * @see de.thb.schiller.mad2doplanner.model.entities.TodoItem
 */

interface ITodoCRUDOperations {

    //region C ... CREATE
    /**
     * Creates an object of type TodoItem
     * @return the created TodoItem object
     */
    TodoItem createTodoItem(TodoItem todoItem);

    //endregion


    //region R ... READ

    /**
     * Reads all available TodoItems
     * @return A list of all found TodoItems
     */
    List<TodoItem> readAllTodoItems();

    /**
     * Reads a specific TodoItem
     * @param id The TodoItem's specific identifier
     * @return The found TodoItem object
     */
    TodoItem readTodoItem(long id);

    //endregion


    //region U ... Update

    /**
     * Updates a specific TodoItem
     * @param todoItem The TodoItem to update
     * @return The updated TodoItem object
     */
    TodoItem updateTodoItem(TodoItem todoItem);

    //endregion


    //region D ... Delete

    /**
     * Deletes a specific TodoItem.
     * @param id The the to-do item's id
     * @return The deletion success status
     */
    boolean deleteTodoItem(long id);

    //endregion
}
