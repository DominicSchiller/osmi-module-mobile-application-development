package de.thb.schiller.mad2doplanner.services.auth;

import de.thb.schiller.mad2doplanner.model.entities.User;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.PUT;

/**
 * @author Dominic Schiller
 * @since 04.07.17
 */

interface IAuthRESTWebAPI {

    /**
     * Authenticates against the remote endpoint.
     * @param user The user object to authenticate with
     * @return Authentication success status
     */
    @PUT("/api/users/auth")
    Call<Boolean> authenticate(@Body User user);
}
