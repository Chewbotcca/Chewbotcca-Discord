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

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.menu.Paginator;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.utils.cache.SnowflakeCacheView;
import pw.chew.chewbotcca.util.JDAUtilUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// %^discrim command
public class DiscrimCommand extends Command {

    public DiscrimCommand() {
        this.name = "discrim";
        this.botPermissions = new Permission[]{Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        event.getChannel().sendTyping().queue();
        String discrim = event.getAuthor().getDiscriminator();
        if (event.getArgs().length() == 4 && event.getArgs().matches("[0-9]{4}")) {
            discrim = event.getArgs();
        } else if (event.getArgs().contains("--rank")) {
            Map<String, Integer> ranking = getDiscrimRanking(event.getJDA().getUserCache());
            Map<String, Integer> ranked = sortByValue(ranking);
            Paginator.Builder pbuilder = JDAUtilUtil.makePaginator();
            pbuilder.setText("Users discriminator ranking"
                + "\nCached users: " + event.getJDA().getUserCache().size());
            for (String discriminator : ranked.keySet()) {
                pbuilder.addItems("#" + discriminator + " - " + ranked.get(discriminator) + " users");
            }
            pbuilder.build().paginate(event.getChannel(), 1);
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
}
