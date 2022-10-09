package freelensers;

import java.util.*;

import org.json.JSONObject;

import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;

import java.sql.*;
import freelensers.db.DBConnectionM;

/**
 * Azure Functions with HTTP Trigger.
 */
public class CreateSubmission {
    /**
     * This function listens at endpoint "/api/CreateSubmission". Two ways to invoke it using "curl" command in bash:
     * 1. curl -d "HTTP Body" {your host}/api/CreateSubmission
     * 2. curl {your host}/api/CreateSubmission?name=HTTP%20Query
     */
    @FunctionName("CreateSubmission")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.GET, HttpMethod.POST}, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Submission of work");
        
        // Parse query parameter
        JSONObject payload = new JSONObject(request.getBody().get());
        String bountyId = payload.getString("bountyId");
        String description = payload.getString("description");
        String owner = payload.getString("owner");
        String link = payload.getString("link");
        
        String query = "INSERT into entries (description,link,bountyid,owner) VALUES (?,?,?,?)";
        try (Connection conn = DBConnectionM.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, description);
            stmt.setString(2, link);
            stmt.setString(3, bountyId);
            stmt.setString(4, owner);
            stmt.executeUpdate();
            return request.createResponseBuilder(HttpStatus.OK).body("Success").build();
        } catch (Exception e) {
            context.getLogger().info("Error: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body(e.getMessage()).build();
        }
        
    }
}
