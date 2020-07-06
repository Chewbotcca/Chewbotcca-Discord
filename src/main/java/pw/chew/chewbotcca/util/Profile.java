package pw.chew.chewbotcca.util;

import org.json.JSONObject;

public class Profile {
    final JSONObject data;
    public Profile(JSONObject input) {
        data = input;
    }

    public static Profile retrieveProfile(String id) {
        JSONObject response = new JSONObject(RestClient.get(
                "https://chew.pw/chewbotcca/discord/profile/" + id + "/api/get",
                PropertiesManager.getChewKey()
        ));
        return new Profile(response);
    }

    public String getId() {
        return String.valueOf(data.getLong("userid"));
    }
}
