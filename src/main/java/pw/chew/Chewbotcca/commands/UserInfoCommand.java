package pw.chew.Chewbotcca.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Member;
import org.json.JSONException;
import org.json.JSONObject;
import pw.chew.Chewbotcca.util.RestClient;

import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UserInfoCommand extends Command {

    public UserInfoCommand() {
        this.name = "userinfo";
        this.aliases = new String[]{"uinfo"};
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        Member member = commandEvent.getMember();

        EmbedBuilder e = new EmbedBuilder();
        e.setTitle("User info for you!");
        e.setThumbnail(member.getUser().getAvatarUrl());
        e.addField("Name#Discrim", member.getUser().getAsTag(), true);
        e.addField("User ID", member.getId(), true);

        String status;
        switch (member.getOnlineStatus()) {
            case ONLINE -> {
                status = "Online";
                e.setColor(Color.decode("#43B581"));
            }
            case IDLE -> {
                status = "Idle";
                e.setColor(Color.decode("#FAA61A"));
            }
            case DO_NOT_DISTURB -> {
                status = "Do Not Disturb";
                e.setColor(Color.decode("#F04747"));
            }
            case OFFLINE -> {
                status = "Offline";
                e.setColor(Color.decode("#747F8D"));
            }
            default -> {
                status = member.getOnlineStatus().getKey();
                e.setColor(Color.decode("#747F8D"));
            }
        }

        e.addField("Status", status, true);
        if(member.getNickname() != null) {
            e.addField("Nickname", member.getNickname(), true);
        }

        List<CharSequence> activities = new ArrayList<>();
        for(int i = 0; i < member.getActivities().size(); i++) {
            Activity activity = member.getActivities().get(i);
            if(activity.getType() == Activity.ActivityType.CUSTOM_STATUS)
                activities.add(activity.getEmoji().getAsMention() + " " + activity.getName());
            else
                activities.add(activity.getName());
        }

        e.addField("Activities", String.join("\n", activities), true);

        try {
            JSONObject dbio = new JSONObject(RestClient.get("https://api.discord.bio/v1/user/details/" + member.getId())).getJSONObject("payload").getJSONObject("user").getJSONObject("details");

            e.setDescription(dbio.getString("description"));
            if(!dbio.isNull("birthday"))
                e.addField("Birthday", dateParser(dbio.getString("birthday")), true);
            if(!dbio.isNull("gender")) {
                String gender = switch (dbio.getInt("gender")) {
                    case 0 -> "Male";
                    case 1 -> "Female";
                    case 2 -> "Non-Binary";
                    default -> "Undisclosed";
                };
                e.addField("Gender", gender, true);
            }
            if(!dbio.isNull("location"))
                e.addField("Location", dbio.getString("location"), true);
            if(!dbio.isNull("occupation"))
                e.addField("Occupation", dbio.getString("occupation"), true);
            e.setFooter("Profile info provided by discord.bio");
        } catch (JSONException | ParseException ignored) { }


        commandEvent.reply(e.build());
    }

    public String dateParser(String date) throws ParseException {
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat outputFormat = new SimpleDateFormat("EEEE, MMM dd, yyyy");
        Date date1 = inputFormat.parse(date);
        return outputFormat.format(date1);
    }
}
