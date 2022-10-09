package freelensers;

import java.util.*;
import com.microsoft.azure.functions.annotation.*;

import freelensers.db.DBConnectionM;

import com.microsoft.azure.functions.*;
import java.sql.*;


import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.*;

/**
 * Azure Functions with HTTP Trigger.
 */
public class VerifyHumanity {
    /**
     * This function listens at endpoint "/api/VerifyHumanity". Two ways to invoke it using "curl" command in bash:
     * 1. curl -d "HTTP Body" {your host}/api/VerifyHumanity
     * 2. curl {your host}/api/VerifyHumanity?name=HTTP%20Query
     */
    @FunctionName("VerifyHumanity")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.POST}, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Verify Humanity trigger processed a request.");

        // Parse query parameter

        JSONObject json = new JSONObject(request.getBody().get());
        String ethAddress = json.getString("ethAddress");
        context.getLogger().info(ethAddress);

        JSONObject verificationResponse = json.getJSONObject("verificationResponse");
        JSONObject verifyIsHuman = makeVerifyRequest(verificationResponse, context);

        context.getLogger().info(verifyIsHuman.toString());

        boolean isSuccess = verifyIsHuman.has("success");
        boolean hasDetail = verifyIsHuman.has("detail");
        boolean alreadyVerified = false;

        if(isSuccess || hasDetail){
            String nullifier_hash = verifyIsHuman.has("nullifier_hash")?verifyIsHuman.getString("nullifier_hash"):"";
            
            try{
                Connection conn = DBConnectionM.getConnection();
                String insert = "INSERT INTO users (ethAddress, nullifier_hash) VALUES (?,?)";
                PreparedStatement ps = conn.prepareStatement(insert);
                ps.setString(1, ethAddress);
                ps.setString(2, nullifier_hash);
                ps.executeUpdate();
                ps.close();
            }catch(Exception e){
                context.getLogger().info("Error: " + e.getMessage());
                return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage()).build();
            }
            return request.createResponseBuilder(HttpStatus.OK).body("{\"status\":\"success\"}").build();
        }
        // Parse query parameter
        return request.createResponseBuilder(HttpStatus.OK).body("{\"status\":\"success\"}").build();
       
        
    }

    public JSONObject makeVerifyRequest(JSONObject verificationResponse, final ExecutionContext context) {
        try{
        //make http request
            //adding to verification response.
            verificationResponse.put("action_id", "wid_staging_f3a312cdc8dc4681b54e8227cbe09c7c");
            verificationResponse.put("signal", "my_signal");
            context.getLogger().info(verificationResponse.toString());

            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost("https://developer.worldcoin.org/api/v1/verify");
            httpPost.setHeader("Content-Type", "application/json");
            
            httpPost.setEntity(new StringEntity(verificationResponse.toString()));
            CloseableHttpResponse response = httpClient.execute(httpPost);
            context.getLogger().info("Response Code : " + response.getStatusLine().getStatusCode());
            context.getLogger().info("Response Message : " + response.getStatusLine().getReasonPhrase());
            context.getLogger().info("Response Body : " + response.getEntity().getContent());

            String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
            context.getLogger().info("responseString: " + responseString);

            return new JSONObject(response.getEntity().getContent());
        }catch(Exception e){
            context.getLogger().info("Error: " + e.getMessage());
        }
        return new JSONObject();
    }
}
