package de.thb.schiller.mad2doplanner.services.persistence;

import java.util.List;

import de.thb.schiller.mad2doplanner.model.entities.TodoItem;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/**
 * @author Dominic Schiller
 * @since 30.06.17
 *
 * This interface declares all methods available for to-do item REST requests.
 */

interface ITodoRESTWebAPI {

    /**
     * Creates a TodoItem remotely by sending a POST request
     * @param todoItem The to-do item to create remotely
     * @return The created to-do item
     */
    @POST("/api/todos")
    Call<TodoItem> createTodoItem(@Body TodoItem todoItem);

    /**
     * Reads all to-do items from remote endpoint by sending a GET request
     * @return A list of retrieved to-do items
     */
    @GET("/api/todos")
    Call<List<TodoItem>> readAllTodoItems();

    /**
     * Reads a specific to-do item from remote endpoint by sending a GET request
     * @param id The to-do item's id
     * @return The retrieved to-do item
     */
    @GET("/api/todos/{id}")
    Call<TodoItem> readTodoItem(@Path("id") long id);

    /**
     * Updates a specific to-do item remotely by sending a PUT request
     * @param id The to-do item's id
     * @param todoItem The to-do item to update it's data remotely
     * @return The updated to-do item
     */
    @PUT("/api/todos/{id}")
    Call<TodoItem> updateTodoItem(@Path("id") long id, @Body TodoItem todoItem);

    /**
     * Deletes a specific to-do item remotely by sending a DELETE request
     * @param id The to-do item's id
     * @return the deletion success status
     */
    @DELETE("/api/todos/{id}")
    Call<Boolean> deleteTodoItem(@Path("id") long id);
}
