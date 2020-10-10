/*
 * Copyright (C) 2020 Chewbotcca
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package pw.chew.chewbotcca.commands.info;

import bio.discord.api.DBioAPI;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import pw.chew.chewbotcca.util.DateTime;
import pw.chew.chewbotcca.util.Mention;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static pw.chew.chewbotcca.commands.info.ServerInfoCommand.capitalize;

// %^uinfo command
public class UserInfoCommand extends Command {

    public UserInfoCommand() {
        this.name = "userinfo";
        this.aliases = new String[]{"uinfo"};
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        commandEvent.getChannel().sendTyping().queue();
        // gather args
        String args = commandEvent.getArgs();
        User user;

        String mode = "";

        // If they want member info
        if(args.contains("--member")) {
            mode = "member";
            args = args.replace("--member", "").trim();
        }

        // Get server members (in sync) for join position
        new Thread(() -> commandEvent.getGuild().loadMembers().get());
        await().atMost(30, TimeUnit.SECONDS).until(() -> commandEvent.getGuild().getMemberCache().size() == commandEvent.getGuild().getMemberCount());

        // Attempt to find the author if they exist
        if(args.length() == 0) {
            user = commandEvent.getAuthor();
        } else {
            Object mention = Mention.parseMention(args, commandEvent.getGuild(), commandEvent.getJDA());
            if(mention != null) {
                if (mention instanceof Member) {
                    user = ((Member)mention).getUser();
                } else {
                    user = (User)mention;
                }
            } else {
                try {
                    user = commandEvent.getJDA().getUserById(args);
                    if (user == null) {
                        user = commandEvent.getJDA().retrieveUserById(args).complete();
                    }
                } catch (NullPointerException | NumberFormatException e) {
                    List<Member> users = commandEvent.getGuild().getMembersByName(args, true);
                    List<Member> byNick = commandEvent.getGuild().getMembersByEffectiveName(args, true);
                    if (users.size() == 0 && byNick.size() == 0) {
                        commandEvent.reply("No members found for the given input");
                        return;
                    } else if (users.size() > 0 && byNick.size() == 0) {
                        user = users.get(0).getUser();
                    } else {
                        user = byNick.get(0).getUser();
                    }
                }
            }
        }
        if(user == null) {
            commandEvent.reply("No users found for the given input");
            return;
        }

        // Generate and respond
        Member member = commandEvent.getGuild().getMemberById(user.getId());
        if(member != null && mode.equals("member")) {
            commandEvent.reply(gatherMemberInfo(commandEvent.getGuild(), member).build());
        } else if(member == null && mode.equals("member")) {
            commandEvent.reply("This user is not on this server!");
        } else {
            commandEvent.reply(gatherMainInfo(commandEvent, user).build());
        }
    }

    /**
     * Gather main info, the main info. main
     * @param commandEvent the command event
     * @param user the user
     * @return an embed
     */
    public EmbedBuilder gatherMainInfo(CommandEvent commandEvent, User user) {
        // Get the member
        Member member = commandEvent.getGuild().getMemberById(user.getId());
        boolean onServer = false;
        if(member != null) {
            onServer = true;
        }
        boolean self = user == commandEvent.getAuthor();

        EmbedBuilder e = new EmbedBuilder();
        // If executor == member
        if (self)
            e.setTitle("User info for you!");
        else
            e.setTitle("User info for " + user.getAsTag());
        if(user.getAvatarUrl() != null)
            e.setThumbnail(user.getAvatarUrl() + "?size=2048");
        e.addField("Name#Discrim", user.getAsTag(), true);
        e.addField("User ID", user.getId(), true);

        // If they're on the server, we can get their presence
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
            // And their nick
            if (member.getNickname() != null) {
                e.addField("Nickname", member.getNickname(), true);
            }

            // And their activities
            List<CharSequence> activities = new ArrayList<>();
            for (int i = 0; i < member.getActivities().size(); i++) {
                Activity activity = member.getActivities().get(i);
                if (activity.getType() == Activity.ActivityType.CUSTOM_STATUS) {
                    Activity.Emoji emoji = activity.getEmoji();
                    if(emoji == null)
                        activities.add(activity.getName());
                    else
                        activities.add(activity.getEmoji().getAsMention() + " " + activity.getName());
                } else
                    activities.add(activity.getName());
            }

            if (activities.size() > 0)
                e.addField("Activities - " + activities.size(), String.join("\n", activities), true);
        }

        // Get their bio from discord.bio, if they have one.
        var dbio = new DBioAPI().getUser(user.getId());
        if(dbio != null) {
            e.setDescription(dbio.getDescription());
            if(dbio.getBirthday() != null)
                e.addField("Birthday", dateParser(dbio.getBirthday()), true);
            e.addField("Gender", capitalize(dbio.getGender().name()), true);
            if(dbio.getLocation() != null)
                e.addField("Location", dbio.getLocation(), true);
            if(dbio.getOccupation() != null)
                e.addField("Occupation", dbio.getOccupation(), true);
            e.setFooter("Profile info provided by discord.bio");
        }

        if(onServer)
            e.addField("More Information", "Add `--member` to see more about this Member.", true);

        return e;
    }

    /**
     * Gather server specific info
     * @param server the server
     * @param member the member
     * @return an embed
     */
    public EmbedBuilder gatherMemberInfo(Guild server, Member member) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Member info for " + member.getEffectiveName());
        // Find their join position
        int position = 0;
        List<Member> members = server.getMemberCache().asList();
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
        embed.addField("Display Name", member.getEffectiveName() + "\n" + member.getAsMention(), true);
        embed.addField("Join Position", String.valueOf(position), true);
        embed.addField("Joined", DateTime.timeAgo(Instant.now().toEpochMilli() - member.getTimeJoined().toInstant().toEpochMilli()) + " ago", false);

        return embed;
    }

    /**
     * Date parser because java is weird
     * @param date the date
     * @return a parsed date
     */
    public String dateParser(Date date) {
        SimpleDateFormat outputFormat = new SimpleDateFormat("EEEE, MMM dd, yyyy");
        return outputFormat.format(date);
    }
}
