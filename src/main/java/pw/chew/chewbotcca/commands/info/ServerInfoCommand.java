/*
 * Copyright (C) 2021 Chewbotcca
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
import com.jagrosh.jdautilities.menu.Paginator;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.StageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.TimeFormat;
import pw.chew.chewbotcca.objects.IKnowWhatIAmDoingISwearException;
import pw.chew.chewbotcca.util.DateTime;
import pw.chew.chewbotcca.util.JDAUtilUtil;
import pw.chew.chewbotcca.util.MiscUtil;
import pw.chew.chewbotcca.util.ResponseHelper;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

// %^sinfo command
public class ServerInfoCommand extends SlashCommand {

    public ServerInfoCommand() {
        this.name = "serverinfo";
        this.help = "Gathers general information about the server";
        this.aliases = new String[]{"sinfo"};
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = true;
        this.children = new SlashCommand[]{
            new ServerGeneralInfoSubCommand(),
            new ServerBoostsInfoSubCommand(),
            new ServerBotsSubCommand(),
            new ServerChannelsInfoSubCommand(),
            new ServerMemberByJoinSubCommand(),
            new ServerRolesInfoSubCommand(),
            new ServerMemberMilestoneSubCommand(),
            new ServerMemberStatsSubCommand()
        };
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        // SlashCommands with children don't have root command
    }

    @Override
    protected void execute(CommandEvent event) {
        event.reply(gatherGeneralInfo(event.getGuild(), event.getPrefix() + "sinfo").build());
    }

    private EmbedBuilder gatherGeneralInfo(Guild server, String prefix) {
        EmbedBuilder e = new EmbedBuilder();
        e.setTitle("Server Information");
        if (server.getDescription() != null)
            e.setDescription(server.getDescription());
        e.setAuthor(server.getName(), null, server.getIconUrl());

        if (server.getIconUrl() != null)
            e.setThumbnail(server.getIconUrl() + "?size=2048");

        // Retrieve server info
        e.addField("Server Owner", server.retrieveOwner(true).complete().getAsMention(), true);
        e.addField("Created", TimeFormat.DATE_TIME_SHORT.format(server.getTimeCreated()), true);
        e.addField("Locale", server.getLocale().getDisplayName(), true);

        // Get bot / member count
        List<Member> members = server.getMembers();
        int bots = 0;
        for (Member member : members) {
            if (member.getUser().isBot())
                bots += 1;
        }

        int membercount = server.getMemberCount();
        int humans = membercount - bots;

        e.addField("Member Count", "Total: " + membercount + "\n" +
            "Bots: " + bots + " - (" + generatePercent(bots, membercount) + ")\n" +
            "Users: " + humans + " - (" + generatePercent(humans, membercount) + ")", true);

        // Channel counts
        int textchans = server.getTextChannels().size();
        int newschans = 0;

        for (TextChannel channel : server.getTextChannels()) {
            if (channel.isNews()) {
                newschans++;
                textchans--;
            }
        }

        List<CharSequence> counts = new ArrayList<>();
        counts.add("Total: " + server.getTextChannels().size());
        counts.add("Text: " + textchans);
        counts.add("Voice: " + server.getVoiceChannels().size());
        counts.add("Stages: " + server.getStageChannels().size());
        counts.add("Categories: " + server.getCategories().size());
        if (server.getFeatures().contains("COMMERCE"))
            counts.add("Store Pages: " + server.getStoreChannels().size());
        if (server.getFeatures().contains("NEWS"))
            counts.add("News: " + newschans);

        e.addField("Channel Count", String.join("\n", counts), true);

        // Server Boosting Stats
        if (server.getBoostCount() > 0 && server.getBoostRole() != null) {
            e.addField("Server Boosting",
                "Level: " + server.getBoostTier().getKey() +
                    "\nBoosts: " + server.getBoostCount() +
                    "\nBoosters: " + server.getBoosters().size() +
                    "\nRole: " + server.getBoostRole().getAsMention(), true);
        }

        // Gather perk info
        List<String> perks = new ArrayList<>();
        List<String> feats = new ArrayList<>();
        String[] features = server.getFeatures().toArray(new String[0]);
        Arrays.sort(features);
        for (String perk : features) {
            if (ignorePerk(perk)) continue;

            if (isFeature(perk)) {
                feats.add(perkParser(perk, server));
            } else {
                perks.add(perkParser(perk, server));
            }
        }
        if (e.getFields().size() == 5) {
            e.addBlankField(true);
        }
        if (perks.size() > 0) {
            e.addField("Perks", String.join("\n", perks), true);
        }
        if (feats.size() > 0) {
            e.addField("Features", String.join("\n", feats), true);
        }

        e.addField("View More Info", """
            Roles - `%^sinfo roles`
            Boosts - `%^sinfo boosts`
            Bots - `%^sinfo bots`
            Channels - `%^sinfo channels`
            Member Milestones - `%^sinfo milestones`
            Member Stats - `%^sinfo memberstats`
            """.replaceAll("%\\^sinfo", prefix), false);

        e.setFooter("Server ID: " + server.getId());

        return e;
    }

    /**
     * Parse the perk list and make it fancy if necessary
     *
     * @param feature the feature
     * @param server  the server
     * @return the perks as nice list
     */
    public String perkParser(String feature, Guild server) {
        return switch (feature) {
            default -> MiscUtil.capitalize(feature);
            case "BANNER" -> "[Banner](" + server.getBannerUrl() + "?size=2048)";
            case "COMMERCE" -> "<:store_tag:725504846924611584> Store Channels";
            case "NEWS" -> "<:news:725504846937063595> News Channels";
            case "INVITE_SPLASH" -> "[Invite Splash](" + server.getSplashUrl() + "?size=2048)";
            case "PARTNERED" -> "<:partner:753433398005071872> Partnered Server";
            case "VANITY_URL" -> parseVanityUrl(server.getVanityCode());
            case "VERIFIED" -> "<:verifiedserver:753433397933899826> Verified";
            case "COMMUNITY" -> ":white_check_mark: Community";
            case "MEMBER_VERIFICATION_GATE_ENABLED" -> ":white_check_mark: Membership Screening";
            case "WELCOME_SCREEN_ENABLED" -> ":white_check_mark: Welcome Screen";
        };
    }

    /**
     * We split perks and features so this determines which column they should go into.
     * Generally, these features can be enabled by anyone.
     *
     * @param perk the perk to handle
     * @return whether this is an enabled feature, not a perk
     */
    private boolean isFeature(String perk) {
        return switch (perk) {
            // All enabled in server settings, by anyone, if they want
            case "COMMUNITY", "MEMBER_VERIFICATION_GATE_ENABLED", "WELCOME_SCREEN_ENABLED" -> true;
            // Not a feature
            default -> false;
        };
    }

    /**
     * A simple function to filter out the noise among the many perks servers can have.
     * Generally, anything here is so widespread or so useless they don't need to show up.
     *
     * @param perk the perk to parse
     * @return true if it should be ignored
     */
    private boolean ignorePerk(String perk) {
        return switch (perk) {
            // Useless information
            case "ENABLED_DISCOVERABLE_BEFORE" -> true;
            // Thread info. Temporary because every server has it.
            case "PRIVATE_THREADS", "SEVEN_DAY_THREAD_ARCHIVE", "THREE_DAY_THREAD_ARCHIVE" -> true;
            // Every server has threads
            case "THREADS_ENABLED" -> true;
            // Continue as normal
            default -> false;
        };
    }

    /**
     * Parses the vanity URL to determine what should be shown back to the user.
     * If there is a vanity URL (why wouldn't you set one?) it will notify. Otherwise, render it inline.
     *
     * @param vanity the vanity code
     * @return the friendly string
     */
    private String parseVanityUrl(String vanity) {
        if (vanity == null) {
            return "Vanity URL: None Set!";
        } else {
            return "Vanity URL: " + "[" + vanity + "](https://discord.gg/" + vanity + ")";
        }
    }

    /**
     * Helper method because it doesn't understand guildOnly really does mean guildOnly
     *
     * @param event the slash command event
     * @return the NEVER NULL guild
     */
    private static Guild guaranteeGuild(SlashCommandEvent event) {
        Guild server = event.getGuild();
        if (server == null) {
            throw new IKnowWhatIAmDoingISwearException();
        }
        return server;
    }

    /**
     * Generates a percentage based on given values.
     * 5, 20 => "25%"
     *
     * @param value the numerator
     * @param total the denominator
     * @return a friendly percentage
     */
    private String generatePercent(int value, int total) {
        DecimalFormat df = new DecimalFormat("#.##");
        return df.format((float) value / (float) total * 100) + "%";
    }

    /**
     * Gathers general info about the server.
     * Only required for slash command
     */
    private class ServerGeneralInfoSubCommand extends SlashCommand {

        public ServerGeneralInfoSubCommand() {
            this.name = "general";
            this.help = "Gathers general information about this server";
            this.guildOnly = true;
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            event.replyEmbeds(gatherGeneralInfo(guaranteeGuild(event), "/serverinfo").build()).queue();
        }
    }

    /**
     * Gathers info about boosters and how long they've been boosting
     */
    private static class ServerBoostsInfoSubCommand extends SlashCommand {

        public ServerBoostsInfoSubCommand() {
            this.name = "boosts";
            this.help = "Gathers information about this server's boosts.";
            this.aliases = new String[]{"boost"};
            this.guildOnly = true;
            this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            event.replyEmbeds(gatherInfo(guaranteeGuild(event)).build()).queue();
        }

        @Override
        protected void execute(CommandEvent event) {
            event.reply(gatherInfo(event.getGuild()).build());
        }

        private EmbedBuilder gatherInfo(Guild server) {
            // Get boosters
            List<Member> boosters = server.getBoosters();
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("Boosters for " + server.getName());
            List<CharSequence> boostString = new ArrayList<>();
            // Get a now time for a basis of comparison
            for (Member booster : boosters) {
                OffsetDateTime timeBoosted = booster.getTimeBoosted();
                // If they're still boosting (in case they stop boosting between gathering boosters and finding how long they're boosting
                if (timeBoosted != null)
                    boostString.add(booster.getAsMention() + " since " + TimeFormat.DATE_SHORT.format(timeBoosted.toInstant()) + " (" + DateTime.timeAgoShort(timeBoosted.toInstant(), true) + ")");
            }
            embed.setDescription(String.join("\n", boostString));
            if (boostString.isEmpty()) {
                embed.setDescription("No one is boosting! Will you be the first?");
            }

            return embed;
        }
    }

    /**
     * Gather role information
     */
    private static class ServerRolesInfoSubCommand extends SlashCommand {

        public ServerRolesInfoSubCommand() {
            this.name = "roles";
            this.help = "Gathers information about this server's roles.";
            this.guildOnly = true;
            this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_ADD_REACTION};

            this.options = Arrays.asList(
                new OptionData(OptionType.BOOLEAN, "display_role", "Show amount of members whose display role is this."),
                new OptionData(OptionType.BOOLEAN, "online", "Shows amount of members with this role currently online")
            );
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            boolean displayMode = ResponseHelper.guaranteeBooleanOption(event, "display_role", false);
            boolean onlineMode = ResponseHelper.guaranteeBooleanOption(event, "online", false);

            event.replyEmbeds(new EmbedBuilder().setDescription("Gathering the details...").build()).queue(interactionHook -> {
                interactionHook.retrieveOriginal().queue(message -> {
                    run(guaranteeGuild(event), event.getTextChannel(), displayMode, onlineMode);
                });
            });
        }

        @Override
        protected void execute(CommandEvent event) {
            boolean displayMode = event.getArgs().contains("--display");
            boolean onlineMode = event.getArgs().contains("--online");

            run(event.getGuild(), event.getTextChannel(), displayMode, onlineMode);
        }

        public void run(Guild server, TextChannel channel, boolean displayMode, boolean onlineMode) {
            Paginator.Builder pbuilder = JDAUtilUtil.makePaginator().clearItems();

            List<CharSequence> roleNames = new ArrayList<>();

            roleNames.add("Role List for " + server.getName());
            String format = "Format: %ONLINE%Total members with this role%DISPLAY% - Role Mention";
            if (onlineMode) {
                format = format.replace("%ONLINE%", "Online Members / ");
            } else {
                format = format.replace("%ONLINE%", "");
            }
            if (displayMode) {
                format = format.replace("%DISPLAY%", " (Members with this role as display role)");
            } else {
                format = format.replace("%DISPLAY%", "");
            }
            roleNames.add(format);
            roleNames.add("Note: Bot roles and everyone role are skipped!");

            // Gather roles and iterate over each to find stats
            List<Role> roles = server.getRoles();
            for (Role role : roles) {
                // Skip if bot role
                if (role.getTags().isBot())
                    continue;
                // Skip @everyone role
                if (role.isPublicRole())
                    continue;

                List<Member> membersWithRole = server.getMembersWithRoles(role);
                if (membersWithRole.isEmpty()) {
                    pbuilder.addItems("0" + " - " + role.getAsMention());
                    continue;
                }

                int online = 0;
                int total = membersWithRole.size();
                int display = 0;

                if (!displayMode && !onlineMode) {
                    pbuilder.addItems(total + " - " + role.getAsMention());
                    continue;
                }

                for (Member member : membersWithRole) {
                    if (onlineMode && member.getOnlineStatus() == OnlineStatus.ONLINE) {
                        online++;
                    }

                    if (displayMode) {
                        List<Role> userRoles = member.getRoles().stream().filter(Role::isHoisted).collect(Collectors.toList());
                        if (!userRoles.isEmpty() && userRoles.get(0).equals(role)) {
                            display++;
                        }
                    }
                }

                String rowFormat = "%ONLINE%%TOTAL%%DISPLAY% - %MENTION%";
                if (onlineMode) {
                    rowFormat = rowFormat.replace("%ONLINE%", online + " / ");
                } else {
                    rowFormat = rowFormat.replace("%ONLINE%", "");
                }
                if (displayMode) {
                    rowFormat = rowFormat.replace("%DISPLAY%", " (" + display + ")");
                } else {
                    rowFormat = rowFormat.replace("%DISPLAY%", "");
                }
                rowFormat = rowFormat.replace("%MENTION%", role.getAsMention());
                rowFormat = rowFormat.replace("%TOTAL%", total + "");
                pbuilder.addItems(rowFormat);
            }

            Paginator p = pbuilder.setText(String.join("\n", roleNames)).build();

            p.paginate(channel, 1);
        }
    }

    /**
     * Gather server bots
     */
    private static class ServerBotsSubCommand extends SlashCommand {

        public ServerBotsSubCommand() {
            this.name = "bots";
            this.help = "Gathers information about this server's bots.";
            this.aliases = new String[]{"bot"};
            this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_ADD_REACTION};
            this.guildOnly = true;

            this.options = Collections.singletonList(
                new OptionData(OptionType.BOOLEAN, "render_mention", "Whether or not to render as a mention.").setRequired(true)
            );
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            event.replyEmbeds(new EmbedBuilder().setDescription("Gathering the details...").build()).queue(interactionHook -> {
                interactionHook.retrieveOriginal().queue(message -> {
                    gatherData(guaranteeGuild(event), event.getUser(), event.getTextChannel(), ResponseHelper.guaranteeBooleanOption(event, "render_mention", false))
                        .paginate(message, 1);
                });
            });
        }

        @Override
        protected void execute(CommandEvent event) {
            gatherData(event.getGuild(), event.getAuthor(), event.getTextChannel(), event.getArgs().contains("--mention")).paginate(event.getChannel(), 1);
        }

        private Paginator gatherData(Guild server, User author, TextChannel channel, boolean renderMention) {
            Paginator.Builder pbuilder = JDAUtilUtil.makePaginator().clearItems();

            pbuilder.setText("Bots on " + server.getName() + "\n" + "Newest bots on the bottom");
            // Get all members as an array an sort it by join time
            Member[] members = server.getMembers().toArray(new Member[0]);
            Arrays.sort(members, (o1, o2) -> {
                if (o1.getTimeJoined().toEpochSecond() > o2.getTimeJoined().toEpochSecond())
                    return 1;
                else if (o1.getTimeJoined() == o2.getTimeJoined())
                    return 0;
                else
                    return -1;
            });
            // Iterate over each bot to find how long they've been on
            for (Member member : members) {
                if (member.getUser().isBot()) {
                    if (renderMention) {
                        pbuilder.addItems(member.getAsMention() + " added " + TimeFormat.DATE_SHORT.format(member.getTimeJoined()) + " (" + DateTime.timeAgoShort(member.getTimeJoined().toInstant(), false) + " ago)");
                    } else {
                        pbuilder.addItems(member.getUser().getAsTag() + " added " + TimeFormat.DATE_SHORT.format(member.getTimeJoined()) + " (" + DateTime.timeAgoShort(member.getTimeJoined().toInstant(), false) + " ago)");
                    }
                }
            }

            return pbuilder.setUsers(author).build();
        }
    }

    /**
     * Get a member by join position
     */
    private static class ServerMemberByJoinSubCommand extends SlashCommand {

        public ServerMemberByJoinSubCommand() {
            this.name = "member";
            this.help = "Search for a member by a join position.";
            this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
            this.guildOnly = true;

            this.options = Collections.singletonList(
                new OptionData(OptionType.INTEGER, "position", "The join position to reverse-search the Member.").setRequired(true)
            );
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            try {
                event.replyEmbeds(UserInfoCommand.gatherMainInfo(guaranteeGuild(event), gatherMember(guaranteeGuild(event), (int) event.getOption("position").getAsLong()).getUser(), event.getUser()).build()).queue();
            } catch (IllegalArgumentException e) {
                event.replyEmbeds(ResponseHelper.generateFailureEmbed(null, e.getMessage())).queue();
            }
        }

        @Override
        protected void execute(CommandEvent event) {
            int position;
            try {
                position = Integer.parseInt(event.getArgs());
            } catch (NumberFormatException e) {
                event.reply(ResponseHelper.generateFailureEmbed("Error occurred!", "Invalid input! Must be an integer!"));
                return;
            }

            try {
                event.reply(UserInfoCommand.gatherMainInfo(event.getGuild(), gatherMember(event.getGuild(), position).getUser(), event.getAuthor()).build());
            } catch (IllegalArgumentException e) {
                event.reply(ResponseHelper.generateFailureEmbed("Error occurred!", e.getMessage()));
            }
        }

        public Member gatherMember(Guild server, int position) {
            if (position > server.getMemberCache().size() || position <= 0) {
                throw new IllegalArgumentException("Invalid input! Must be 1 <= x <=" + server.getMemberCache().size());
            }

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
            return bruh[position - 1];
        }
    }

    /**
     * Get channel info, like count and other info
     */
    private class ServerChannelsInfoSubCommand extends SlashCommand {

        public ServerChannelsInfoSubCommand() {
            this.name = "channels";
            this.help = "Gathers information about this server's channels.";
            this.aliases = new String[]{"channel"};
            this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
            this.guildOnly = true;
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            event.replyEmbeds(gatherChannelInfo(guaranteeGuild(event)).build()).queue();
        }

        @Override
        protected void execute(CommandEvent event) {
            event.reply(gatherChannelInfo(event.getGuild()).build());
        }

        private EmbedBuilder gatherChannelInfo(Guild server) {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("Channel Info and Stats for " + server.getName());
            embed.setDescription("Total Channels: " + server.getChannels().size());

            // Channel counts
            int totalChannels = server.getChannels().size();
            int textChannels = server.getTextChannels().size();
            int voiceChannels = server.getVoiceChannels().size();
            int stageChannels = server.getStageChannels().size();
            int categories = server.getCategories().size();
            int storeChannels = server.getStoreChannels().size();
            int newsChannels = 0;
            int nsfw = 0;
            int inVoice = 0;
            int inStage = 0;

            for (TextChannel channel : server.getTextChannels()) {
                if (channel.isNews()) {
                    newsChannels++;
                    textChannels--;
                }
                if (channel.isNSFW()) {
                    nsfw++;
                }
            }

            for (VoiceChannel vc : server.getVoiceChannels()) {
                inVoice += vc.getMembers().size();
            }

            for (StageChannel sc : server.getStageChannels()) {
                inStage += sc.getMembers().size();
            }

            embed.addField("Text Channels", String.format("Total: %s (%s)\nNSFW: %s", textChannels, generatePercent(textChannels, totalChannels), nsfw), true);
            embed.addField("Voice Channels", String.format("Total: %s (%s)\nMembers in Voice: %s", voiceChannels, generatePercent(voiceChannels, totalChannels), inVoice), true);
            embed.addField("Stage Channels", String.format("Total: %s (%s)\nMembers in Stages: %s", stageChannels, generatePercent(stageChannels, totalChannels), inStage), true);
            embed.addField("Categories", String.format("Total: %s (%s)", categories, generatePercent(categories, totalChannels)), true);

            if (server.getFeatures().contains("COMMERCE")) {
                embed.addField("Store", String.format("Total: %s (%s)", storeChannels, generatePercent(storeChannels, totalChannels)), true);
            }

            if (server.getFeatures().contains("NEWS")) {
                embed.addField("News Channels", String.format("Total: %s (%s)", newsChannels, generatePercent(newsChannels, totalChannels)), true);
            }

            return embed;
        }
    }

    /**
     * Get member milestone (estimated)
     */
    private static class ServerMemberMilestoneSubCommand extends SlashCommand {
        final static int[] milestones = new int[]{10, 25, 50, 100, 500, 1000, 5000, 10000, 20000, 50000, 100000, 200000, 300000, 400000, 500000, 1000000};

        public ServerMemberMilestoneSubCommand() {
            this.name = "milestones";
            this.help = "Gathers information about this server's upcoming member milestones.";
            this.aliases = new String[]{"milestone"};
            this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
            this.guildOnly = true;
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            event.replyEmbeds(gatherMilestoneStats(guaranteeGuild(event)).build()).queue();
        }

        @Override
        protected void execute(CommandEvent event) {
            event.reply(gatherMilestoneStats(event.getGuild()).build());
        }

        private EmbedBuilder gatherMilestoneStats(Guild server) {
            EmbedBuilder embed = new EmbedBuilder();

            embed.setTitle("Upcoming Member Milestones for " + server.getName());

            int members = server.getMemberCount();
            int days = Math.round((float) (Instant.now().toEpochMilli() - server.getTimeCreated().toInstant().toEpochMilli()) / 1000 / 60 / 60 / 24);

            float membersPerDay = (float) members / (float) days;

            List<String> daysToMilestone = new ArrayList<>();

            daysToMilestone.add("Members per day (linear): " + membersPerDay);
            daysToMilestone.add("Dates are based on average members per day. Dates may vary based on any number of circumstances.");
            daysToMilestone.add("If year is >100 years in the future, it will not be included.");
            daysToMilestone.add("");

            for (int milestone : milestones) {
                if (milestone < members)
                    continue;

                float daysNeeded = ((float) milestone / membersPerDay);
                Instant timeAtMilestone = server.getTimeCreated().toInstant().plusMillis((long) (daysNeeded * 24 * 60 * 60 * 1000));
                OffsetDateTime date = timeAtMilestone.atOffset(server.getTimeCreated().getOffset());
                int year = date.getYear();
                if (year - 100 > OffsetDateTime.now().getYear())
                    continue;

                daysToMilestone.add(NumberFormat.getNumberInstance(Locale.US).format(milestone) + " Members - " + TimeFormat.DATE_TIME_SHORT.format(date));
            }

            embed.setDescription(String.join("\n", daysToMilestone));

            return embed;
        }
    }

    private static class ServerMemberStatsSubCommand extends SlashCommand {

        public ServerMemberStatsSubCommand() {
            this.name = "memberstats";
            this.help = "Gathers general statistics about this server's member.";
            this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
            this.guildOnly = true;
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            event.replyEmbeds(gatherMemberStats(guaranteeGuild(event)).build()).queue();
        }

        @Override
        protected void execute(CommandEvent event) {
            event.reply(gatherMemberStats(event.getGuild()).build());
        }

        private EmbedBuilder gatherMemberStats(Guild server) {
            List<Member> members = server.getMemberCache().asList();
            Map<LocalDate, Integer> bestDays = new TreeMap<>();

            // Toss it all into the hashmap
            for (Member member : members) {
                LocalDate date = member.getTimeJoined().toLocalDate();
                int amount = bestDays.getOrDefault(date, 0);
                bestDays.put(date, amount + 1);
            }

            // Find longest slump
            long slumpDays = 0;
            LocalDate startSlump = LocalDate.now();

            LocalDate[] keys = bestDays.keySet().toArray(new LocalDate[0]);

            for (int i = 1; i < bestDays.size(); i++) {
                LocalDate base = keys[i - 1];
                LocalDate compare = keys[i];

                if (compare.toEpochDay() - base.toEpochDay() > slumpDays) {
                    slumpDays = compare.toEpochDay() - base.toEpochDay();
                    startSlump = base;
                }
            }

            int best = bestDays.values().stream().max(Comparator.comparingInt(Integer::intValue)).get();
            List<LocalDate> bestDay = new ArrayList<>();
            for (LocalDate date : keys) {
                if (bestDays.get(date) == best) {
                    bestDay.add(date);
                }
            }

            EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Member Stats for " + server.getName())
                .setDescription("A collection of simple stats for the server!");

            embed.addField("Most Members in A Day", "Members: " + best + "\n" + bestDay.stream().map(LocalDate::toString).collect(Collectors.joining("\n")), true);
            embed.addField("Largest Slump", "Days: " + slumpDays + "\nRange: " + startSlump + " - " + startSlump.atStartOfDay().plusDays(slumpDays).toLocalDate().toString(), true);

            return embed;
        }
    }
}