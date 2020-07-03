package pw.chew.chewbotcca.commands.info;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import org.json.JSONObject;
import pw.chew.chewbotcca.Main;
import pw.chew.chewbotcca.util.PropertiesManager;
import pw.chew.chewbotcca.util.RestClient;

import java.awt.*;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class BotInfoCommand extends Command {
    public BotInfoCommand() {
        this.name = "botinfo";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.aliases = new String[]{"binfo"};
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        commandEvent.getChannel().sendTyping().queue();
        String[] args = commandEvent.getArgs().split(" ");
        if(args.length == 0) {
            commandEvent.reply("Please specify a bot with either a mention or its ID");
            return;
        }
        String botId;
        if(args[0].contains("<@")) {
            botId = args[0].replace("<@!", "").replace(">", "");
        } else {
            botId = args[0];
        }
        String list = "dbl";
        if(args.length > 1) {
            list = args[1].toLowerCase();
        }
        switch (list) {
            case "dbl", "top.gg", "topgg" -> commandEvent.reply(gatherTopggInfo(botId, commandEvent).build());
            case "dbots" -> commandEvent.reply(gatherDBotsInfo(botId, commandEvent).build());
            default -> commandEvent.reply("Invalid Bot List! Possible: ```dbots, topgg```");
        }
    }

    private EmbedBuilder gatherTopggInfo(String id, CommandEvent event) {
        JSONObject bot = new JSONObject(RestClient.get("https://top.gg/api/bots/" + id, PropertiesManager.getTopggToken()));

        if(bot.has("error")) {
            return new EmbedBuilder().setTitle("Error!").setDescription(bot.getString("error"));
        }

        EmbedBuilder e = new EmbedBuilder();
        e.setTitle("Bot Information");

        String certified;
        if(bot.getBoolean("certifiedBot")) {
            certified = "https://cdn.discordapp.com/emojis/392249976639455232.png";
        } else {
            certified = String.format("https://cdn.discordapp.com/avatars/%s/%s.%s", id, bot.getString("avatar"), "png");
        }

        e.setAuthor(bot.getString("username") + "#" + bot.getString("discriminator"), "https://top.gg/bot/" + id, certified);

        e.setThumbnail(String.format("https://cdn.discordapp.com/avatars/%s/%s.%s", id, bot.getString("avatar"), "png"));

        e.setDescription(bot.getString("shortdesc"));

        e.addField("Bot ID", bot.getString("id"), true);

        if(bot.has("server_count"))
            e.addField("Server Count", String.valueOf(bot.getInt("server_count")), true);
        else
            e.addField("Server Count", "Unknown", true);

        e.addField("Prefix", "`" + bot.getString("prefix") + "`", true);

        e.addField("Library", bot.getString("lib"), true);

        e.addField("Points", "This Month: " + bot.getInt("monthlyPoints") + "\n" +
                "All Time: " + bot.getInt("points"), true);

        List<String> tags = new ArrayList<>();
        for(Object tag : bot.getJSONArray("tags")) {
            tags.add((String) tag);
        }

        if(tags.isEmpty()) {
            e.addField("Tags", "None", true);
        } else {
            e.addField("Tags", String.join(", ", tags), true);
        }

        List<String> owners = new ArrayList<>();
        for(Object owner : bot.getJSONArray("owners")) {
            User user = event.getJDA().getUserById((String) owner);
            if(user == null)
                owners.add((String) owner);
            else
                owners.add(user.getAsTag());
        }

        if(owners.isEmpty()) {
            e.addField("Owners", "None", true);
        } else {
            e.addField("Owners", String.join(", ", owners), true);
        }

        List<CharSequence> links = new ArrayList<>();
        links.add("[Bot Page](https://top.gg/bot/" + id + ")");
        links.add("[Vote](https://top.gg/bot/" + id + "/vote)");
        if(!bot.getString("invite").equals(""))
            links.add("[Invite](" + bot.getString("invite") + ")");
        if(!bot.getString("website").equals(""))
            links.add("[Website](" + bot.getString("website") + ")");
        if(!bot.getString("support").equals(""))
            links.add("[Support Server](https://discord.gg/" + bot.getString("support") + ")");
        if(!bot.getString("github").equals(""))
            links.add("[GitHub](" + bot.getString("github") + ")");

        e.addField("Links", String.join("\n", links), true);

        e.setFooter("Bot added");
        e.setTimestamp(dateParser(bot.getString("date")));

        e.setColor(Color.decode("#43B581"));

        return e;
    }

    private EmbedBuilder gatherDBotsInfo(String id, CommandEvent event) {
        JSONObject bot = new JSONObject(RestClient.get("https://discord.bots.gg/api/v1/bots/" + id, PropertiesManager.getDbotsToken()));

        if(bot.has("message")) {
            return new EmbedBuilder().setTitle("Error!").setDescription("This bot does not exist on this list!");
        }

        EmbedBuilder e = new EmbedBuilder();
        e.setTitle("Bot Information");

        e.setAuthor(bot.getString("username") + "#" + bot.getString("discriminator"), "https://discord.bots.gg/bots/" + bot.getString("userId"));

        e.setThumbnail(bot.getString("avatarURL"));

        e.setDescription(bot.getString("shortDescription"));

        e.addField("Bot ID", bot.getString("userId"), true);

        e.addField("Server Count", String.valueOf(bot.getInt("guildCount")), true);

        e.addField("Prefix", "`" + bot.getString("prefix") + "`", true);

        e.addField("Library", bot.getString("libraryName"), true);

        User user = event.getJDA().getUserById(bot.getJSONObject("owner").getString("userId"));
        if(user == null)
            e.addField("Owner", "Unknown", true);
        else
            e.addField("Owner", user.getAsTag(), true);

        List<CharSequence> links = new ArrayList<>();
        links.add("[Bot Page](https://discord.bots.gg/bots/" + id + ")");
        if(!bot.getString("botInvite").equals(""))
            links.add("[Invite](" + bot.getString("botInvite") + ")");
        if(!bot.getString("website").equals(""))
            links.add("[Website](" + bot.getString("website") + ")");
        if(!bot.getString("supportInvite").equals(""))
            links.add("[Support Server](" + bot.getString("supportInvite") + ")");
        if(!bot.getString("openSource").equals(""))
            links.add("[Source Code](" + bot.getString("openSource") + ")");

        e.addField("Links", String.join("\n", links), true);

        e.setFooter("Bot added");
        e.setTimestamp(dateParser(bot.getString("addedDate")));

        e.setColor(Color.decode("#43B581"));

        return e;
    }

    public OffsetDateTime dateParser(String date) {
        DateTimeFormatter inputFormat = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSSX");
        return OffsetDateTime.parse(date, inputFormat);
    }
}



