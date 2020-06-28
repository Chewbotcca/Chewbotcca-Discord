package pw.chew.chewbotcca.util;

import org.json.JSONObject;
import pw.chew.chewbotcca.Main;

public class ServerSettings {
    JSONObject data;
    public ServerSettings(JSONObject input) {
        data = input;
    }

    public static ServerSettings retrieveServer(String id) {
        JSONObject response = new JSONObject(RestClient.get(
                "https://chew.pw/chewbotcca/discord/server/" + id + "/api/get",
                Main.getProp().getProperty("chewkey")
        ));
        return new ServerSettings(response);
    }

    public String getId() {
        return String.valueOf(data.getLong("serverid"));
    }
}
