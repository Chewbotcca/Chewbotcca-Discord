package pw.chew.chewbotcca.commands.english;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import org.json.JSONArray;
import org.json.JSONException;
import pw.chew.chewbotcca.util.PropertiesManager;
import pw.chew.chewbotcca.util.RestClient;

public class DefineCommand extends Command {

    public DefineCommand() {
        this.name = "define";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        String word = commandEvent.getArgs();
        JSONArray grabbedword;
        try {
            grabbedword = new JSONArray(RestClient.get("http://api.wordnik.com/v4/word.json/" + word + "/definitions?limit=1&includeRelated=true&useCanonical=false&includeTags=false&api_key=" + PropertiesManager.getWordnikToken()));
        } catch(JSONException e) {
            commandEvent.reply("Word not found! Check your local spell-checker!");
            return;
        }

        EmbedBuilder e = new EmbedBuilder()
                .setTitle("Definition for " + word)
                .setColor(0xd084)
                .setDescription(grabbedword.getJSONObject(0).getString("text"))
                .setAuthor("Dictionary", null, "https://icons.iconarchive.com/icons/johanchalibert/mac-osx-yosemite/1024/dictionary-icon.png");

        if(grabbedword.getJSONObject(0).has("partOfSpeech")) {
            e.addField("Part of Speech", grabbedword.getJSONObject(0).getString("partOfSpeech"), true);
        }

        commandEvent.reply(e.build());
    }
}
