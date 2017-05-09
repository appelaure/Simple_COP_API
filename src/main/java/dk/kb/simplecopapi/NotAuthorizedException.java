package dk.kb.simplecopapi;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by laap on 09-05-2017.
 */
public class NotAuthorizedException extends WebApplicationException {
    public NotAuthorizedException(String message) {
        super(Response.status(Response.Status.UNAUTHORIZED)
                .entity(message).type(MediaType.TEXT_PLAIN).build());
    }
}