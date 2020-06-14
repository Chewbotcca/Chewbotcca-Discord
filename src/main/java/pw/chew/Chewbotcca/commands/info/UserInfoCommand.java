package pw.chew.Chewbotcca.commands.info;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;
import pw.chew.Chewbotcca.util.RestClient;

import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

public class UserInfoCommand extends Command {

    public UserInfoCommand() {
        this.name = "userinfo";
        this.aliases = new String[]{"uinfo"};
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        String args = commandEvent.getArgs();
        User user = null;

        String mode = "";

        if(args.contains("member")) {
            mode = "member";
            args = args.replace(" member", "");
            args = args.replace("member ", "");
            args = args.replace("member", "");
        }

        try {
            commandEvent.getGuild().retrieveMembers().get();
            await().atMost(30, TimeUnit.SECONDS).until(() -> commandEvent.getGuild().getMemberCache().size() == commandEvent.getGuild().getMemberCount());
        } catch (InterruptedException | ExecutionException interruptedException) {
            interruptedException.printStackTrace();
        }

        if(args.length() == 0) {
            user = commandEvent.getAuthor();
        } else {
            try {
                Long.parseLong(commandEvent.getArgs());
                user = commandEvent.getGuild().getMemberById(args).getUser();
            } catch (NullPointerException e) {
                commandEvent.reply("No user found for the given ID.");
            } catch (NumberFormatException e) {
                List<Member> users = commandEvent.getGuild().getMembersByName(args, true);
                List<Member> byNick = commandEvent.getGuild().getMembersByEffectiveName(args, true);
                if(users.size() == 0 && byNick.size() == 0) {
                    commandEvent.reply("No members found for the given input");
                    return;
                } else if(users.size() > 0 && byNick.size() == 0) {
                    user = users.get(0).getUser();
                } else {
                    user = byNick.get(0).getUser();
                }
            }
        }
        if(user == null) {
            commandEvent.reply("No users found for the given input");
            return;
        }

        Member member = commandEvent.getGuild().getMemberById(user.getId());
        if(member != null && mode.equals("member")) {
            commandEvent.reply(gatherMemberInfo(commandEvent, member).build());
        } else if(member == null && mode.equals("member")) {
            commandEvent.reply("This user is not on this server!");
        } else {
            commandEvent.reply(gatherMainInfo(commandEvent, user).build());
        }
    }

    public EmbedBuilder gatherMainInfo(CommandEvent commandEvent, User user) {
        Member member = commandEvent.getGuild().getMemberById(user.getId());
        boolean onServer = false;
        if(member != null) {
            onServer = true;
        }
        boolean self = user == commandEvent.getAuthor();

        EmbedBuilder e = new EmbedBuilder();
        if (self)
            e.setTitle("User info for you!");
        else
            e.setTitle("User info for " + user.getAsTag());
        e.setThumbnail(user.getAvatarUrl());
        e.addField("Name#Discrim", user.getAsTag(), true);
        e.addField("User ID", user.getId(), true);

        if(onServer) {
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
                    if (self)
                        status = "Invisible";
                    else
                        status = "Offline";
                    e.setColor(Color.decode("#747F8D"));
                }
                default -> {
                    status = member.getOnlineStatus().getKey();
                    e.setColor(Color.decode("#747F8D"));
                }
            }

            e.addField("Status", status, true);
            if (member.getNickname() != null) {
                e.addField("Nickname", member.getNickname(), true);
            }

            List<CharSequence> activities = new ArrayList<>();
            for (int i = 0; i < member.getActivities().size(); i++) {
                Activity activity = member.getActivities().get(i);
                if (activity.getType() == Activity.ActivityType.CUSTOM_STATUS)
                    activities.add(activity.getEmoji().getAsMention() + " " + activity.getName());
                else
                    activities.add(activity.getName());
            }

            if (activities.size() > 0)
                e.addField("Activities", String.join("\n", activities), true);
        }

        try {
            JSONObject dbio = new JSONObject(RestClient.get("https://api.discord.bio/v1/user/details/" + user.getId())).getJSONObject("payload").getJSONObject("user").getJSONObject("details");

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

        if(onServer)
            e.addField("More Information", "Add `member` to see more about this Member.", true);

        return e;
    }

    public EmbedBuilder gatherMemberInfo(CommandEvent commandEvent, Member member) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Member info for " + member.getEffectiveName());
        int position = 0;
        List<Member> members = commandEvent.getGuild().getMemberCache().asList();
        Member[] bruh = members.toArray(new Member[0]);
        Arrays.sort(bruh, (o1, o2) -> {
            if (o1.getTimeJoined().toEpochSecond() > o2.getTimeJoined().toEpochSecond())
                return 1;
            else if (o1.getTimeJoined() == o2.getTimeJoined())
                return 0;
            else
                return -1;
        });
        boolean found = false;
        for(int i = 0; i < bruh.length || !found; i++) {
            if(member.getId().equals(bruh[i].getId())) {
                found = true;
                position = i + 1;
            }
        }
        embed.addField("Display Name", member.getEffectiveName(), true);
        embed.addField("Join Position", String.valueOf(position), true);

        return embed;
    }

    public String dateParser(String date) throws ParseException {
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat outputFormat = new SimpleDateFormat("EEEE, MMM dd, yyyy");
        Date date1 = inputFormat.parse(date);
        return outputFormat.format(date1);
    }
}
