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
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.cache.SnowflakeCacheView;
import pw.chew.chewbotcca.util.JDAUtilUtil;
import pw.chew.chewbotcca.util.ResponseHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// %^discrim command
public class DiscrimCommand extends SlashCommand {

    public DiscrimCommand() {
        this.name = "discrim";
        this.help = "Find users with the same discriminator as you, or specify one";
        this.botPermissions = new Permission[]{Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
        this.children = new SlashCommand[]{
            new DiscrimListSubCommand(),
            new DiscrimRankSubCommand()
        };
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        // SlashCommands with children don't have root command
    }

    @Override
    protected void execute(CommandEvent event) {
        event.getChannel().sendTyping().queue();
        String discrim = event.getAuthor().getDiscriminator();
        if (event.getArgs().length() == 4 && event.getArgs().matches("[0-9]{4}")) {
            discrim = event.getArgs();
        } else if (event.getArgs().contains("--rank")) {
            buildRankPaginator(event.getJDA()).paginate(event.getChannel(), 1);
            return;
        }
        Paginator p = buildDiscrimPaginator(discrim, event.getJDA());
        p.paginate(event.getChannel(), 1);
    }

    public Paginator buildDiscrimPaginator(String discrim, JDA jda) {
        Paginator.Builder pbuilder = JDAUtilUtil.makePaginator();
        pbuilder.setText("Users with discriminator #" + discrim
            + "\nCached users: " + jda.getUserCache().size());
        for (User user : jda.getUserCache()) {
            if (user.getDiscriminator().equals(discrim)) {
                pbuilder.addItems(user.getAsTag());
            }
        }

        return pbuilder.build();
    }

    public Paginator buildRankPaginator(JDA jda) {
        Map<String, Integer> ranking = getDiscrimRanking(jda.getUserCache());
        Map<String, Integer> ranked = sortByValue(ranking);
        Paginator.Builder pbuilder = JDAUtilUtil.makePaginator();
        pbuilder.setText("Users discriminator ranking"
            + "\nCached users: " + jda.getUserCache().size());
        for (String discriminator : ranked.keySet()) {
            pbuilder.addItems("#" + discriminator + " - " + ranked.get(discriminator) + " users");
        }
        return pbuilder.build();
    }

    public Map<String, Integer> getDiscrimRanking(SnowflakeCacheView<User> cache) {
        HashMap<String, Integer> mapping = new HashMap<>();
        for (User user : cache) {
            mapping.put(user.getDiscriminator(), mapping.getOrDefault(user.getDiscriminator(), 0) + 1);
        }
        return mapping;
    }

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Map.Entry.comparingByValue());
        Collections.reverse(list);

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

    public class DiscrimListSubCommand extends SlashCommand {

        public DiscrimListSubCommand() {
            this.name = "list";
            this.help = "List users with a given discriminator, or blank for your own";
            this.botPermissions = new Permission[]{Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_EMBED_LINKS};
            this.guildOnly = false;
            this.options = Collections.singletonList(
                new OptionData(OptionType.STRING, "discriminator", "The discriminator you want to lookup, or blank for yours")
            );
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            String discrim = ResponseHelper.guaranteeStringOption(event, "discriminator", event.getUser().getDiscriminator());
            if (!(discrim.length() == 4 && discrim.matches("[0-9]{4}"))) {
                event.reply("Invalid discriminator provided!").setEphemeral(true).queue();
                return;
            }
            // Send message then edit it
            event.replyEmbeds(new EmbedBuilder().setDescription("Checking...").build()).queue(interactionHook -> {
                interactionHook.retrieveOriginal().queue(message -> {
                    buildDiscrimPaginator(discrim, event.getJDA()).paginate(message, 1);
                });
            });
        }
    }

    public class DiscrimRankSubCommand extends SlashCommand {

        public DiscrimRankSubCommand() {
            this.name = "rank";
            this.help = "View most used discriminators, based on users in this bot's servers";
            this.botPermissions = new Permission[]{Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_EMBED_LINKS};
            this.guildOnly = false;
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            // Send message then edit it
            event.replyEmbeds(new EmbedBuilder().setDescription("Checking...").build()).queue(interactionHook -> {
                interactionHook.retrieveOriginal().queue(message -> {
                    buildRankPaginator(event.getJDA()).paginate(message, 1);
                });
            });
        }
    }
}
