package freelensers;

import java.util.*;
import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;

import org.json.*;
import freelensers.db.DBConnectionM;
import java.sql.*;

/**
 * Azure Functions with HTTP Trigger.
 */
public class GetBounty {
    /**
     * This function listens at endpoint "/api/GetBounty". Two ways to invoke it using "curl" command in bash:
     * 1. curl -d "HTTP Body" {your host}/api/GetBounty
     * 2. curl {your host}/api/GetBounty?name=HTTP%20Query
     */
    @FunctionName("GetBounty")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.GET}, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        JSONObject toRet = new JSONObject();
        String bountyId = request.getQueryParameters().get("id");
        try {
            //get all bounties from db
            Connection connection = DBConnectionM.getConnection();
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM bounty where id=?");
            ps.setString(1, bountyId);
            ResultSet rs = ps.executeQuery();
            JSONArray bounties = new JSONArray();
            while(rs.next()){
                JSONObject bounty = new JSONObject();
                bounty.put("id", rs.getInt("id"));
                bounty.put("description", rs.getString("description"));
                bounty.put("price", rs.getString("price"));
                bounty.put("liveUntil", rs.getString("liveUntil"));
                bounty.put("applicantNumber", rs.getInt("applicantNumber"));
                bounty.put("owner", rs.getString("owner"));
                bounties.put(bounty);
            }
            toRet.put("bounties", bounties);
            DBConnectionM.closeConnection(connection);

        } catch (Exception e) {
            context.getLogger().info("Error: " + e.getMessage());
            toRet.put("error", e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body(toRet.toString()).build();
        }

        return request.createResponseBuilder(HttpStatus.OK).body(toRet.toString()).build();
    }
}
