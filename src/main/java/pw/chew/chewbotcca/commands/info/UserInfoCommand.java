/*
 * Copyright (C) 2025 Chewbotcca
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

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.TimeFormat;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import pw.chew.chewbotcca.util.DateTime;
import pw.chew.chewbotcca.util.MiscUtil;
import pw.chew.chewbotcca.util.RestClient;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Userinfo Command.
 * <br>
 * <a href="https://help.chew.pro/bots/discord/chewbotcca/commands/userinfo">View information</a>
 */
public class UserInfoCommand extends SlashCommand {

    public UserInfoCommand() {
        this.name = "userinfo";
        this.help = "Returns some useful info about you or another user";
        this.aliases = new String[]{"uinfo"};
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.contexts = new InteractionContextType[]{InteractionContextType.GUILD, InteractionContextType.BOT_DM, InteractionContextType.PRIVATE_CHANNEL};
        this.options = Collections.singletonList(
            new OptionData(OptionType.USER, "user", "The user to lookup")
        );
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        // Attempt to gather Member
        User user = event.optUser("user", event.getUser());
        Member member = event.optMember("user", event.getMember());

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
            List<User> mentions = commandEvent.getMessage().getMentions().getUsers();
            if (!mentions.isEmpty()) {
                user = mentions.getFirst();
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
                        } else if (!users.isEmpty() && byNick.isEmpty()) {
                            user = users.getFirst().getUser();
                        } else {
                            user = byNick.getFirst().getUser();
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
        Member member = server == null ? null : server.getMember(user);
        boolean onServer = member != null;
        boolean self = user == author;

        EmbedBuilder e = new EmbedBuilder();
        // If executor == member
        e.setTitle(self ? "User info for you!" : "User info for " + MiscUtil.getTag(user));

        // Set server/user avatar
        if (user.getAvatarUrl() != null) {
            e.setThumbnail(user.getAvatarUrl() + "?size=2048");
            e.setAuthor(MiscUtil.getTag(user), null, user.getAvatarUrl());
        }
        if (onServer && member.getAvatarUrl() != null) {
            e.setThumbnail(member.getAvatarUrl() + "?size=2048");
        }

        List<String> nameInfo = new ArrayList<>(Arrays.asList(
            "ID: " + user.getId(),
            "Mention: " + user.getAsMention()
        ));
        // Since bots can get a userinfo response, and they still have discriminators, we need to keep this check :/
        if (user.getDiscriminator().equals("0000")) {
            nameInfo.addFirst("Username: " + user.getName());
        } else {
            nameInfo.addFirst("Tag: " + user.getAsTag());
        }

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

            // Add a blank field to pad the first row
            e.addBlankField(true);

            // And their activities
            List<CharSequence> activities = new ArrayList<>();
            for (int i = 0; i < member.getActivities().size(); i++) {
                Activity activity = member.getActivities().get(i);
                if (activity.getType() == Activity.ActivityType.CUSTOM_STATUS) {
                    Emoji emoji = activity.getEmoji();
                    activities.add((emoji == null ? "" : emoji.getFormatted() + " ") + activity.getName());
                } else
                    activities.add(activity.getName());
            }

            if (!activities.isEmpty())
                e.addField("Activities - " + activities.size(), String.join("\n", activities), false);
        }

        // Get pronoun if they have it
        JSONObject pronounData = RestClient.get("https://pronoundb.org/api/v2/lookup?platform=discord&ids=" + user.getId()).asJSONObject();
        boolean hasPronouns = false;
        if (pronounData.has(user.getId()) && !pronounData.getJSONObject(user.getId()).getJSONObject("sets").getJSONArray("en").isEmpty()) {
            // en set
            JSONArray set = pronounData.getJSONObject(user.getId()).getJSONObject("sets").getJSONArray("en");
            hasPronouns = true;

            e.addField("Pronouns", String.join("/", MiscUtil.toList(set, String.class)), true);
        }

        int missingFields;
        int cur = 0;
        for (MessageEmbed.Field field : e.getFields()) {
            if(field.isInline()) cur++;
        }
        missingFields = 3 - cur % 3;
        if (missingFields == 3) missingFields = 0;
        for (int i = 0; i < missingFields; i++) {
            e.addBlankField(true);
        }

        // Give credit
        List<String> credits = new ArrayList<>();
        if (hasPronouns) credits.add("Pronoun data from pronoundb.org");
        if (!credits.isEmpty()) e.setFooter(String.join(" - ", credits));

        List<String> createInfo = new ArrayList<>();
        createInfo.add(TimeFormat.DATE_TIME_SHORT.format(user.getTimeCreated()));
        createInfo.add(DateTime.timeAgoShort(user.getTimeCreated().toInstant(), true));

        if (onServer) {
            List<String> joinInfo = Arrays.asList(
                "Join Position: " + getJoinPosition(member),
                TimeFormat.DATE_TIME_SHORT.format(member.getTimeJoined()),
                DateTime.timeAgoShort(member.getTimeJoined().toInstant(), true) + " ago"
            );

            e.addField("Joined", String.join("\n", joinInfo), true);

            int agePosition = getAgePosition(member);
            // If they are more new than old
            if (agePosition > (server.getMembers().size() / 2)) {
                int position = server.getMembers().size() - agePosition;
                createInfo.addFirst("#" + position + " youngest (this server)");
            } else {
                createInfo.addFirst("#" + agePosition + " oldest (this server)");
            }
        }

        e.addField("Created", String.join("\n", createInfo), true);

        return e;
    }

    /**
     * Gather the join position of a specified member
     *
     * @param member the member
     * @return an embed
     */
    public static int getJoinPosition(Member member) {
        // Find their join position
        Guild server = member.getGuild();
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

    /**
     * Gather the age position of a specified member
     *
     * @param member the member
     * @return an embed
     */
    public static int getAgePosition(Member member) {
        // Find their join position
        Guild server = member.getGuild();
        List<Member> members = server.getMemberCache().asList();
        Member[] bruh = members.toArray(new Member[0]);
        Arrays.sort(bruh, (o1, o2) -> {
            if (o1.getTimeCreated().toEpochSecond() > o2.getTimeCreated().toEpochSecond())
                return 1;
            else if (o1.getTimeCreated() == o2.getTimeCreated())
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
}
