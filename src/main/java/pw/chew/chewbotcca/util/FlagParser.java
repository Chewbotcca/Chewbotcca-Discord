package pw.chew.chewbotcca.util;

import java.util.HashMap;
import java.util.Map;

public class FlagParser {
    public static Map<String, String> parse(String args) {
        String[] ar = args.split("--");
        Map<String, String> output = new HashMap<>();
        for (String arg : ar) {
            String name = arg.split(" ")[0];
            String data = arg.replace(name + " ", "");
            output.put(name, data);
        }
        return output;
    }
}
