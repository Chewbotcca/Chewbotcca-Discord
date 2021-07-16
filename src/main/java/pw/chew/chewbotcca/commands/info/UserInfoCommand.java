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
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.TimeFormat;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import pw.chew.chewbotcca.util.DateTime;
import pw.chew.chewbotcca.util.RestClient;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static pw.chew.chewbotcca.util.MiscUtil.capitalize;

// %^uinfo command
public class UserInfoCommand extends SlashCommand {

    public UserInfoCommand() {
        this.name = "userinfo";
        this.help = "Returns some useful info about you or another user";
        this.aliases = new String[]{"uinfo"};
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
        this.options = Collections.singletonList(
            new OptionData(OptionType.USER, "user", "The user to lookup").setRequired(true)
        );
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        // Attempt to gather Member
        User user = Objects.requireNonNull(event.getOption("user")).getAsUser();
        Member member = Objects.requireNonNull(event.getOption("user")).getAsMember();

        // Generate and respond
        event.replyEmbeds(gatherMainInfo(member == null ? null : member.getGuild(), user, event.getUser()).build()).queue();
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        commandEvent.getChannel().sendTyping().queue();
        // gather args
        String args = commandEvent.getArgs();
        User user = null;

        // Attempt to find the author if they exist
        if (args.isBlank()) {
            user = commandEvent.getAuthor();
        } else {
            List<User> mentions = commandEvent.getMessage().getMentionedUsers();
            if (!mentions.isEmpty()) {
                user = mentions.get(0);
            } else {
                try {
                    user = commandEvent.getJDA().getUserById(args);
                    if (user == null) {
                        try {
                            user = commandEvent.getJDA().retrieveUserById(args).complete();
                        } catch (ErrorResponseException ignored) {
                        }
                    }
                } catch (NullPointerException | NumberFormatException e) {
                    if (commandEvent.isFromType(ChannelType.TEXT)) {
                        List<Member> users = commandEvent.getGuild().getMembersByName(args, true);
                        List<Member> byNick = commandEvent.getGuild().getMembersByEffectiveName(args, true);
                        if (users.isEmpty() && byNick.isEmpty()) {
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
        }
        if(user == null) {
            commandEvent.reply("No users found for the given input");
            return;
        }

        // Generate and respond
        commandEvent.reply(gatherMainInfo(commandEvent.getGuild(), user, commandEvent.getAuthor()).build());
    }

    /**
     * Gather main info, the main info. main
     *
     * @param server the server to check for members in
     * @param user   the user
     * @param author the author of the message
     * @return an embed
     */
    public static EmbedBuilder gatherMainInfo(@Nullable Guild server, User user, User author) {
        // Get the member
        Member member = null;
        if (server != null) {
            member = server.getMember(user);
        }
        boolean onServer = member != null;
        boolean self = user == author;

        EmbedBuilder e = new EmbedBuilder();
        // If executor == member
        e.setTitle(self ? "User info for you!" : "User info for " + user.getAsTag());
        if (user.getAvatarUrl() != null) e.setThumbnail(user.getAvatarUrl() + "?size=2048");
        List<String> nameInfo = new ArrayList<>(Arrays.asList(
            "Tag: " + user.getAsTag(),
            "ID: " + user.getId(),
            "Mention: " + user.getAsMention()
        ));
        if (onServer && !member.getEffectiveName().equals(user.getName())) {
            nameInfo.add("Nickname: " + member.getEffectiveName());
        }

        e.addField("Names", String.join("\n", nameInfo), true);

        // If they're on the server, we can get their presence
        if (onServer) {
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
                    status = self ? "Invisible" : "Offline";
                    e.setColor(Color.decode("#747F8D"));
                }
                default -> {
                    status = member.getOnlineStatus().getKey();
                    e.setColor(Color.decode("#747F8D"));
                }
            }
            e.addField("Status", status, true);

            // Join position
            e.addField("Join Position", getJoinPosition(server, member) + "", true);

            // And their activities
            List<CharSequence> activities = new ArrayList<>();
            for (int i = 0; i < member.getActivities().size(); i++) {
                Activity activity = member.getActivities().get(i);
                if (activity.getType() == Activity.ActivityType.CUSTOM_STATUS) {
                    Activity.Emoji emoji = activity.getEmoji();
                    activities.add((emoji == null ? "" : emoji.getAsMention() + " ") + activity.getName());
                } else
                    activities.add(activity.getName());
            }

            if (activities.size() > 0)
                e.addField("Activities - " + activities.size(), String.join("\n", activities), false);
        }

        // Get pronoun if they have it
        JSONObject pronounData = new JSONObject(RestClient.get("https://pronoundb.org/api/v1/lookup?platform=discord&id=" + user.getId()));
        String pronouns = null;
        if (pronounData.has("pronouns")) {
            pronouns = PRONOUN.valueOf(pronounData.getString("pronouns")).detailed;
        }

        // Get their bio from discord.bio, if they have one.
        var dbio = new DBioAPI().getUser(user.getId());
        if (dbio != null) {
            e.setDescription(dbio.getDescription());
            if (dbio.getBirthday() != null)
                e.addField("Birthday", TimeFormat.DATE_LONG.format(dbio.getBirthday().getTime()), true);
            String gender = capitalize(dbio.getGender().name());
            if (pronouns != null) gender += String.format("\n(%s)", pronouns);
            e.addField("Gender", gender, true);
            if (dbio.getLocation() != null) e.addField("Location", dbio.getLocation(), true);
            if (dbio.getOccupation() != null) e.addField("Occupation", dbio.getOccupation(), true);

        } else if (pronouns != null) {
            e.addField("Pronouns", pronouns, true);
        }

        // Give credit
        List<String> credits = new ArrayList<>();
        if (dbio != null) credits.add("Profile info provided by discord.bio");
        if (pronouns != null) credits.add("Pronoun data from pronoundb.org");
        if (!credits.isEmpty()) e.setFooter(String.join(" - ", credits));

        if (onServer) {
            e.addField("Joined", TimeFormat.DATE_TIME_SHORT.format(member.getTimeJoined()) + "\n" + DateTime.timeAgoShort(member.getTimeJoined().toInstant(), true) + " ago", true);
        }
        e.addField("Created", TimeFormat.DATE_TIME_SHORT.format(user.getTimeCreated()) + "\n" + DateTime.timeAgoShort(user.getTimeCreated().toInstant(), true), true);

        return e;
    }

    /**
     * Gather server specific info
     *
     * @param server the server
     * @param member the member
     * @return an embed
     */
    public static int getJoinPosition(Guild server, Member member) {
        // Find their join position
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
        for (int i = 0; i < bruh.length; i++) {
            if (member.getId().equals(bruh[i].getId())) {
                return i + 1;
            }
        }
        return -1;
    }

    private enum PRONOUN {
        unspecified("Unspecified"),
        hh("he/him"),
        hi("he/it"),
        hs("he/she"),
        ht("he/they"),
        ih("it/him"),
        ii("it/its"),
        is("it/she"),
        it("it/they"),
        shh("she/he"),
        sh("she/her"),
        si("she/it"),
        st("she/they"),
        th("they/he"),
        ti("they/it"),
        ts("they/she"),
        tt("they/them"),
        any("Any pronouns"),
        other("Other pronouns"),
        ask("Ask me my pronouns"),
        avoid("Avoid pronouns, use my name");

        public String detailed;

        PRONOUN(String detail) {
            this.detailed = detail;
        }
    }
}
