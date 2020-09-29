package pw.chew.chewbotcca.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.menu.Paginator;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import pw.chew.chewbotcca.util.JDAUtilUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class UserStatsCommand extends Command {

    public UserStatsCommand() {
        this.name = "userstats";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        Map<User.UserFlag, Integer> stats = new HashMap<>();
        Map<User.UserFlag, List<User>> userFlagUserList = new HashMap<>();
        for (User user : event.getJDA().getUserCache()) {
            for (User.UserFlag flag : user.getFlags()) {
                int num = stats.getOrDefault(flag, 0);
                stats.put(flag, num + 1);
                List<User> list = userFlagUserList.getOrDefault(flag, new ArrayList<>());
                list.add(user);
                userFlagUserList.put(flag, list);
            }
        }
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
        Map<User.UserFlag, Integer> ranked = sortByValue(stats);
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("User Flags Ranking");
        List<String> rankings = new ArrayList<>();
        embed.setFooter("Cached users: " + event.getJDA().getUserCache().size());
        int i = 1;
        for (User.UserFlag flag : ranked.keySet()) {
            rankings.add("#" + i + ": " + toFriendlyString(flag) + " - " + ranked.get(flag) + " users");
            i++;
        }
        embed.setDescription(String.join("\n", rankings));
        event.reply(embed.build());
    }

    public String toFriendlyString(User.UserFlag flag) {
        return switch (flag) {
            case STAFF -> "Staff";
            case SYSTEM -> "System";
            case PARTNER -> "Partnered Server Owner";
            case HYPESQUAD -> "Hypesquad Events";
            case VERIFIED_BOT -> "Verified Bot";
            case EARLY_SUPPORTER -> "Early Supporter";
            case BUG_HUNTER_LEVEL_1 -> "Bug Hunter Level 1";
            case BUG_HUNTER_LEVEL_2 -> "Bug Hunter Level 2";
            case HYPESQUAD_BALANCE -> "Hypesquad: Balance";
            case HYPESQUAD_BRAVERY -> "Hypesquad: Bravery";
            case HYPESQUAD_BRILLIANCE -> "Hypesquad: Brilliance";
            case VERIFIED_DEVELOPER -> "Early Verified Bot Developer";
            case TEAM_USER -> "Team User";
            default -> "Unknown";
        };
    }

    public Paginator buildUserPaginator(List<User> list, User.UserFlag flag, JDA jda) {
        Paginator.Builder pbuilder = JDAUtilUtil.makePaginator();
        pbuilder.setText("Users with user flag " + toFriendlyString(flag)
            + "\nCached users: " + jda.getUserCache().size());
        for (User user : list) {
            pbuilder.addItems(user.getAsTag());
        }

        return pbuilder.build();
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
