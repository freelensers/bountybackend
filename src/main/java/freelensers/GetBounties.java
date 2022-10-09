package freelensers;

import java.util.*;
import java.util.logging.Logger;

import org.json.JSONObject;
import org.json.JSONArray;

import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;

import freelensers.db.DBConnectionM;
import java.sql.*;

/**
 * Azure Functions with HTTP Trigger.
 */
public class GetBounties {
    /**
     * This function listens at endpoint "/api/GetBounties". Two ways to invoke it using "curl" command in bash:
     * 1. curl -d "HTTP Body" {your host}/api/GetBounties
     * 2. curl {your host}/api/GetBounties?name=HTTP%20Query
     */
    @FunctionName("GetBounties")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.GET}, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Get All Bounties Function processed a request.");

        JSONObject toRet = new JSONObject();

        try {
            //get all bounties from db
            Connection connection = DBConnectionM.getConnection();
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM bounty");
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
            //add to json
            //return them as json
        } catch (Exception e) {
            context.getLogger().info("Error: " + e.getMessage());
            toRet.put("error", e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body(toRet.toString()).build();
         }

        return request.createResponseBuilder(HttpStatus.OK).body(toRet.toString()).build();
    }
}
