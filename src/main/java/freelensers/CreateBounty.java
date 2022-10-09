package freelensers;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import freelensers.db.DBConnectionM;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Optional;
import org.json.*;

/**
 * Azure Functions with HTTP Trigger.
 */
public class CreateBounty {
    /**
     * This function listens at endpoint "/api/HttpExample". Two ways to invoke it using "curl" command in bash:
     * 1. curl -d "HTTP Body" {your host}/api/HttpExample
     * 2. curl "{your host}/api/HttpExample?name=HTTP%20Query"
     */
    @FunctionName("CreateBounty")
    public HttpResponseMessage run(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET, HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS)
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("CreateBounty HTTP trigger processed a request.");

        

        String description  ="";
        String price = "";
        String liveUntil = "";
        int applicantNumber = 0;
        String owner = "";
        
        JSONObject payload = new JSONObject(request.getBody().get());

        description = payload.getString("description");
        price = payload.getString("price");
        liveUntil = payload.getString("liveUntil");
        applicantNumber = payload.getInt("applicantNumber");
        owner = payload.getString("owner");

        //save to db 
        try{
            
            Connection connection = DBConnectionM.getConnection();
            PreparedStatement ps = connection.prepareStatement("INSERT INTO bounty (description, price, liveUntil, applicantNumber, owner) VALUES (?,?,?,?,?)");
            ps.setString(1, description);
            ps.setString(2, price);
            ps.setString(3, liveUntil);
            ps.setInt(4, applicantNumber);
            ps.setString(5, owner);
            ps.executeUpdate();
            DBConnectionM.closeConnection(connection);

        }catch(Exception e){
            e.printStackTrace();
        }

        JSONObject toRet = new JSONObject();
        toRet.put("message", "Bounty created");

        return request.createResponseBuilder(HttpStatus.OK).body("Bounty created").build();
    }
}
