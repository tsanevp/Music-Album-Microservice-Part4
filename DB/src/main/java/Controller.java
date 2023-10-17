import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/album")
public class Controller {

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public String getIt() {
        return "Got it";
    }
    
}
