package freelensers;

import java.util.*;

import org.json.JSONObject;

import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;
import org.json.*;
import java.sql.*;
import freelensers.db.DBConnectionM;
/**
 * Azure Functions with HTTP Trigger.
 */
public class ViewSubmissions {
    /**
     * This function listens at endpoint "/api/ViewSubmissions". Two ways to invoke it using "curl" command in bash:
     * 1. curl -d "HTTP Body" {your host}/api/ViewSubmissions
     * 2. curl {your host}/api/ViewSubmissions?name=HTTP%20Query
     */
    @FunctionName("ViewSubmissions")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.GET, HttpMethod.POST}, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("View bounty submissions triggered a process request.");

        JSONObject toRet = new JSONObject();
        String bountyId = request.getQueryParameters().get("bountyId");
        String owner = request.getQueryParameters().get("address");

        try {
            Connection connection = DBConnectionM.getConnection();
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM submission WHERE bountyId = ? AND owner = ?");
            ps.setString(1, bountyId);
            ps.setString(2, owner);
            ResultSet rs = ps.executeQuery();
            JSONArray submissions = new JSONArray();
            while (rs.next()) {
                JSONObject submission = new JSONObject();
                submission.put("id", rs.getString("id"));
                submission.put("bountyId", rs.getString("bountyId"));
                submission.put("owner", rs.getString("owner"));
                submission.put("description", rs.getString("description"));
                submission.put("price", rs.getString("price"));
                submission.put("liveUntil", rs.getString("liveUntil"));
                submission.put("applicantNumber", rs.getString("applicantNumber"));
                submissions.put(submission);
            }
            toRet.put("submissions", submissions);
            return request.createResponseBuilder(HttpStatus.OK).body(toRet.toString()).build();
        } catch (Exception e) {
            e.printStackTrace();
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage()).build();
        }

    }
}
