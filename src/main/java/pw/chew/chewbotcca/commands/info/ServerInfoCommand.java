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

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.Paginator;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import org.awaitility.core.ConditionTimeoutException;
import pw.chew.chewbotcca.util.DateTime;
import pw.chew.chewbotcca.util.JDAUtilUtil;

import java.text.DecimalFormat;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

// %^sinfo command
public class ServerInfoCommand extends Command {
    private final EventWaiter waiter;

    public ServerInfoCommand(EventWaiter waiter) {
        this.name = "serverinfo";
        this.aliases = new String[]{"sinfo"};
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = true;
        this.waiter = waiter;
    }

    @Override
    protected void execute(CommandEvent event) {
        // Get args and the guild
        String args = event.getArgs();
        Guild server = event.getGuild();

        // Load members
        new Thread(() -> event.getGuild().loadMembers().get());
        await().atMost(30, TimeUnit.SECONDS).until(() -> event.getGuild().getMemberCache().size() == event.getGuild().getMemberCount());

        boolean renderMention = false;
        if(args.contains(" --mention")) {
            args = args.replace(" --mention", "");
            renderMention = true;
        }

        // Find the method they want
        if(args.contains("boost")) {
            event.reply(gatherBoostInfo(server).build());
        } else if(args.contains("role")) {
            gatherRoles(event, server);
        } else if(args.contains("bot")) {
            gatherBots(event, server, renderMention);
        } else if(args.contains("member")) {
            event.reply(gatherMemberByJoin(server, args.split(" ")[1]).build());
        } else {
            event.reply(gatherMainInfo(event, server).build());
        }

        event.getGuild().pruneMemberCache();
    }

    /**
     * Gather basic info about a server
     * @param event the command event
     * @param server the server
     * @return the embed
     */
    public EmbedBuilder gatherMainInfo(CommandEvent event, Guild server) {
        EmbedBuilder e = new EmbedBuilder();
        e.setTitle("Server Information");
        if(server.getDescription() != null)
            e.setDescription(server.getDescription());
        e.setAuthor(server.getName(), null, server.getIconUrl());

        e.setThumbnail(server.getIconUrl() + "?size=2048");

        // Retrieve the owner in sync
        server.retrieveOwner(true).queue();
        try {
            await().atMost(3, TimeUnit.SECONDS).until(() -> server.getOwner() != null);
            assert server.getOwner() != null;
            e.addField("Server Owner", server.getOwner().getAsMention(), true);
        } catch (ConditionTimeoutException error) {
            e.addField("Server Owner", "Timed out retrieving owner...", true);
        }

        e.addField("Server ID", server.getId(), true);
        e.addField("Voice Region", server.getRegion().getEmoji() + " " + server.getRegion().getName(), true);
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

        DecimalFormat df = new DecimalFormat("#.##");

        String botpercent = df.format((float)bots / (float)membercount * 100);
        String humanpercent = df.format((float)humans / (float)membercount * 100);
        
        e.addField("Member Count", "Total: " + membercount + "\n" +
                "Bots: " + bots + " - (" + botpercent + "%)\n" +
                "Users: " + humans + " - (" + humanpercent + "%)", true);
        
        // Channel counts
        int totalchans = server.getChannels().size();
        int textchans = server.getTextChannels().size();
        int voicechans = server.getVoiceChannels().size();
        int categories = server.getCategories().size();
        int storechans = server.getStoreChannels().size();
        int newschans = 0;

        for(TextChannel channel : server.getTextChannels()) {
            if(channel.isNews()) {
                newschans++;
                textchans--;
            }
        }

        String textpercent = df.format((float)textchans / (float)totalchans * 100);
        String voicepercent = df.format((float)voicechans / (float)totalchans * 100);
        String catepercent = df.format((float)categories / (float)totalchans * 100);
        String storepercent = df.format((float)storechans / (float)totalchans * 100);
        String newspercent = df.format((float)newschans / (float)totalchans * 100);

        List<CharSequence> counts = new ArrayList<>();
        counts.add("Total: " + totalchans);
        counts.add("Text: " + textchans + " (" + textpercent + "%)");
        counts.add("Voice: " + voicechans + " (" + voicepercent + "%)");
        counts.add("Categories: " + categories + " (" + catepercent + "%)");
        if(server.getFeatures().contains("COMMERCE"))
            counts.add("Store Pages: " + storechans + " (" + storepercent + "%)");
        if(server.getFeatures().contains("NEWS"))
            counts.add("News: " + newschans + " (" + newspercent + "%)");

        e.addField("Channel Count", String.join("\n", counts), true);

        // Server Boosting Stats
        if(server.getBoostCount() > 0)
            e.addField("Server Boosting",
                    "Level: " + server.getBoostTier().getKey() +
                            "\nBoosts: " + server.getBoostCount() +
                            "\nBoosters: " + server.getBoosters().size(), true);

        // Gather perk info
        String perks = perkParser(server);
        if(perks.length() > 0)
            e.addField("Features", perks, true);

        e.addField("View More Info", "Roles - `%^sinfo roles`\nBoosts - `%^sinfo boosts`\nBots - `%^sinfo bots`", false);

        e.setFooter("Server Created on");
        e.setTimestamp(server.getTimeCreated());

        e.setColor(event.getSelfMember().getColor());

        return e;
    }

    /**
     * Gathers info about boosters and how long they've been boosting
     * @param server the server
     * @return an embed
     */
    public EmbedBuilder gatherBoostInfo(Guild server) {
        // Get boosters
        List<Member> boosters = server.getBoosters();
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Boosters for " + server.getName());
        List<CharSequence> boostString = new ArrayList<>();
        // Get a now time for a basis of comparison
        Instant now = Instant.now();
        for (Member booster : boosters) {
            OffsetDateTime timeBoosted = booster.getTimeBoosted();
            // If they're still boosting (in case they stop boosting between gathering boosters and finding how long they're boosting
            if(timeBoosted != null)
                boostString.add(booster.getAsMention() + " for " + DateTime.timeAgo(now.toEpochMilli() - timeBoosted.toInstant().toEpochMilli()));
        }
        embed.setDescription(String.join("\n", boostString));
        if(boostString.size() == 0) {
            embed.setDescription("No one is boosting! Will you be the first?");
        }
        return embed;
    }

    /**
     * Gather role information
     * @param event the command event
     * @param server the server
     */
    public void gatherRoles(CommandEvent event, Guild server) {
        Paginator.Builder pbuilder = JDAUtilUtil.makePaginator(waiter).clearItems();

        List<CharSequence> roleNames = new ArrayList<>();

        roleNames.add("Role List for " + server.getName());
        roleNames.add("Members - Role Mention");
        roleNames.add("Note: Roles that are integrations are skipped!");

        // Gather roles and iterate over each to find stats
        List<Role> roles = server.getRoles();
        for (Role role : roles) {
            List<Member> membersWithRole = server.getMembersWithRoles(role);
            int members = membersWithRole.size();
            // Skip if it's a bot role
            boolean skip = false;
            if (role.isManaged() && members == 1 && membersWithRole.get(0).getUser().isBot())
                skip = true;
            if (role.isPublicRole())
                skip = true;

            if (!skip)
                pbuilder.addItems(membersWithRole.size() + " - " + role.getAsMention());
        }

        Paginator p = pbuilder.setText(String.join("\n", roleNames))
                .build();

        p.paginate(event.getChannel(), 1);
    }

    /**
     * Gather server bots
     * @param event the command event
     * @param server the server
     * @param renderMention whether or not to render a mention
     */
    public void gatherBots(CommandEvent event, Guild server, boolean renderMention) {
        Paginator.Builder pbuilder = JDAUtilUtil.makePaginator(waiter).clearItems();

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
                if(renderMention) {
                    pbuilder.addItems(member.getAsMention() + " added " + DateTime.timeAgo(Instant.now().toEpochMilli() - member.getTimeJoined().toInstant().toEpochMilli(), false) + " ago");
                } else {
                    pbuilder.addItems(member.getUser().getAsTag() + " added " + DateTime.timeAgo(Instant.now().toEpochMilli() - member.getTimeJoined().toInstant().toEpochMilli(), false) + " ago");
                }
            }
        }

        pbuilder.setUsers(event.getAuthor())
                .build()
                .paginate(event.getChannel(), 1);
    }

    /**
     * Get a member by join position
     * @param server the server
     * @param positionString the join position
     * @return an embed
     */
    public EmbedBuilder gatherMemberByJoin(Guild server, String positionString) {
        int position;
        try {
            position = Integer.parseInt(positionString);
        } catch (NumberFormatException e) {
            return new EmbedBuilder().setTitle("Error occurred!").setDescription("Invalid input! Must be an integer!");
        }

        if (position > server.getMemberCache().size() || position <= 0)
            return new EmbedBuilder().setTitle("Error occurred!").setDescription("Invalid input! Must be 1 <= x <=" + server.getMemberCache().size());

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
        return new UserInfoCommand().gatherMemberInfo(server, bruh[position - 1]);
    }

    /**
     * Parse the perk list and make it fancy if necessary
     * @param server the server
     * @return the perks as nice list
     */
    public String perkParser(Guild server) {
        List<CharSequence> perks = new ArrayList<>();
        String[] features = server.getFeatures().toArray(new String[0]);
        Arrays.sort(features);
        for(String feature : features) {
            switch (feature) {
                default -> perks.add(capitalize(feature));
                case "BANNER" -> perks.add("[Banner](" + server.getBannerUrl() + "?size=2048)");
                case "COMMERCE" -> perks.add("<:store_tag:725504846924611584> Store Channels");
                case "NEWS" -> perks.add("<:news:725504846937063595> News Channels");
                case "INVITE_SPLASH" -> perks.add("[Invite Splash](" + server.getSplashUrl() + "?size=2048)");
                case "PARTNERED" -> perks.add("<:partner:753433398005071872> Partnered Server");
                case "VANITY_URL" -> perks.add("Vanity URL: " + "[" + server.getVanityCode() + "](https://discord.gg/" + server.getVanityCode() + ")");
                case "VERIFIED" -> perks.add("<:verifiedserver:753433397933899826> Verified");
            }
        }

        return String.join("\n", perks);
    }

    /*
    Source: https://github.com/ChewMC/TransmuteIt/blob/2b86/src/pw/chew/transmuteit/DiscoveriesCommand.java#L174-L186
    Capitalizes a String, e.g. "BRUH_MOMENT" -> "Bruh Moment"
     */
    public static String capitalize(String to) {
        if(to.equals("")) {
            return "";
        }
        String[] words = to.split("_");
        StringBuilder newword = new StringBuilder();
        for (String word : words) {
            String rest = word.substring(1).toLowerCase();
            String first = word.substring(0, 1).toUpperCase();
            newword.append(first).append(rest).append(" ");
        }
        return newword.toString();
    }
}

