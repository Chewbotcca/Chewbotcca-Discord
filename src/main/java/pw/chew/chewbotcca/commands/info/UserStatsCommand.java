/*
 * Copyright (C) 2022 Chewbotcca
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
import com.jagrosh.jdautilities.menu.Paginator;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import pw.chew.chewbotcca.util.JDAUtilUtil;
import pw.chew.chewbotcca.util.MiscUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserStatsCommand extends SlashCommand {
    private final Map<User.UserFlag, Integer> stats = new HashMap<>();
    private final Map<User.UserFlag, List<User>> userFlagUserList = new HashMap<>();

    public UserStatsCommand() {
        this.name = "userstats";
        this.help = "Finds amount of users with user flags";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        gatherData(event.getJDA());

        event.replyEmbeds(buildEmbed(event.getJDA())).queue();
    }

    @Override
    protected void execute(CommandEvent event) {
        gatherData(event.getJDA());

        if (event.getArgs().contains("--users")) {
            try {
                User.UserFlag flag = User.UserFlag.valueOf(event.getArgs().replace("--users", "").trim());
                buildUserPaginator(userFlagUserList.getOrDefault(flag, new ArrayList<>()), flag, event.getJDA()).paginate(event.getChannel(), 1);
                return;
            } catch (IllegalArgumentException e) {
                event.reply("Not a valid flag!");
                return;
            }
        }

        event.reply(buildEmbed(event.getJDA()));
    }

    public void gatherData(JDA jda) {
        stats.clear();
        userFlagUserList.clear();
        for (User user : jda.getUserCache()) {
            for (User.UserFlag flag : user.getFlags()) {
                int num = stats.getOrDefault(flag, 0);
                stats.put(flag, num + 1);
                List<User> list = userFlagUserList.getOrDefault(flag, new ArrayList<>());
                list.add(user);
                userFlagUserList.put(flag, list);
            }
        }
    }

    public MessageEmbed buildEmbed(JDA jda) {
        Map<User.UserFlag, Integer> ranked = MiscUtil.sortByValue(stats);
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("User Flags Ranking");
        List<String> rankings = new ArrayList<>();
        embed.setFooter("Cached users: " + jda.getUserCache().size());
        int i = 1;
        for (User.UserFlag flag : ranked.keySet()) {
            rankings.add("#" + i + ": " + flag.getName() + " - " + ranked.get(flag) + " users");
            i++;
        }
        embed.setDescription(String.join("\n", rankings));
        return embed.build();
    }

    public Paginator buildUserPaginator(List<User> list, User.UserFlag flag, JDA jda) {
        Paginator.Builder pbuilder = JDAUtilUtil.makePaginator();
        pbuilder.setText("Users with user flag " + flag.getName()
            + "\nCached users: " + jda.getUserCache().size());
        for (User user : list) {
            pbuilder.addItems(user.getAsTag());
        }

        return pbuilder.build();
    }

}
