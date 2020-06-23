package pw.chew.Chewbotcca.util;

import org.json.JSONObject;
import org.slf4j.LoggerFactory;
import pw.chew.Chewbotcca.Main;

public class Profile {
    JSONObject data;
    public Profile(JSONObject input) {
        data = input;
    }

    public static Profile retrieveProfile(String id) {
        JSONObject response = new JSONObject(RestClient.get(
                "https://chew.pw/chewbotcca/discord/profile/" + id + "/api/get",
                Main.getProp().getProperty("chewkey")
        ));
        return new Profile(response);
    }

    public String getId() {
        return String.valueOf(data.getLong("userid"));
    }
}
