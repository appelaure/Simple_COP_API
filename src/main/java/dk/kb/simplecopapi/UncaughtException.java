package dk.kb.simplecopapi;

/**
 * Created by laap on 09-05-2017.
 */
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class UncaughtException extends Throwable implements ExceptionMapper<Throwable>
{
    private static final long serialVersionUID = 1L;

    @Override
    public Response toResponse(Throwable exception) {
        return Response.status(500).entity("Sorry, an error has occurred. Pleade try again later").type("text/plain").build();
    }
}