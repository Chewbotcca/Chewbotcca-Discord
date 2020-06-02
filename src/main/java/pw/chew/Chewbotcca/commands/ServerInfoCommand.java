package pw.chew.Chewbotcca.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.awaitility.core.ConditionTimeoutException;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

public class ServerInfoCommand extends Command {

    public ServerInfoCommand() {
        this.name = "serverinfo";
        this.aliases = new String[]{"sinfo"};
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        String id = event.getArgs();

        Guild server;
        if(id.length() > 0) {
            server = event.getJDA().getGuildById(id);
            if (server == null) {
                event.reply("I am not on that server and are therefore unable to view that server's stats. Try getting them to add me by sending them this invite link: <http://bit.ly/Chewbotcca>");
                return;
            }
        } else {
            if (event.getChannelType() == ChannelType.PRIVATE) {
                event.reply("You silly meme, you can't do SERVERinfo in a private message!!! haha, trying to bamboozle, who got bamboozled NOW? But seriously, try running this on a server or supplying an ID. Better yet invite me to one: <http://bit.ly/Chewbotcca>");
                return;
            }
            server = event.getGuild();
        }

        EmbedBuilder e = new EmbedBuilder();
        e.setTitle("Server Information");
        e.setAuthor(server.getName(), null,server.getIconUrl());

        e.setThumbnail(server.getIconUrl());

        server.retrieveOwner(true).queue();
        try {
            await().atMost(3, TimeUnit.SECONDS).until(() -> server.getOwner() != null);
            assert server.getOwner() != null;
            e.addField("Server Owner", server.getOwner().getAsMention(), true);
        } catch (ConditionTimeoutException error) {
            e.addField("Server Owner", "Timed out retrieving owner...", true);
        }

        e.addField("Server ID", server.getId(), true);

        switch(server.getRegion()) {
            case VIP_AMSTERDAM:
                e.addField("Server Region", "<:region_amsterdam:426902668871467008> <:vip_region:426902668909477898> Amsterdam", true);
                break;
            case BRAZIL:
                e.addField("Server Region", "<:region_brazil:426902668561219605> Brazil", true);
                break;
            case EU_CENTRAL:
                e.addField("Server Region", "<:region_eu:426902669110673408> Central Europe", true);
                break;
            case HONG_KONG:
                e.addField("Server Region", "<:region_hongkong:426902668636585985> Hong Kong", true);
                break;
            case JAPAN:
                e.addField("Server Region", "<:region_japan:426902668578127884> Japan", true);
                break;
            case RUSSIA:
                e.addField("Server Region", "<:region_russia:426902668859015169> Russia", true);
                break;
            case SINGAPORE:
                e.addField("Server Region", "<:region_singapore:426902668951158784> Singapore", true);
                break;
            case SYDNEY:
                e.addField("Server Region", "<:region_sydney:426902668934643722> Sydney", true);
                break;
            case US_CENTRAL:
                e.addField("Server Region", "<:region_us:426902668900827146> US Central", true);
                break;
            case US_EAST:
                e.addField("Server Region", "<:region_us:426902668900827146> US East", true);
                break;
            case VIP_US_EAST:
                e.addField("Server Region", "<:region_us:426902668900827146> <:vip_region:426902668909477898> US East", true);
                break;
            case US_SOUTH:
                e.addField("Server Region", "<:region_us:426902668900827146> US South", true);
                break;
            case US_WEST:
                e.addField("Server Region", "<:region_us:426902668900827146> US West", true);
                break;
            case VIP_US_WEST:
                e.addField("Server Region", "<:region_us:426902668900827146> <:vip_region:426902668909477898> US West", true);
                break;
            case EU_WEST:
                e.addField("Server Region", "<:region_eu:426902669110673408> Western Europe", true);
                break;
            case INDIA:
            case EUROPE:
            case LONDON:
            case UNKNOWN:
            case AMSTERDAM:
            case FRANKFURT:
            case VIP_JAPAN:
            case VIP_BRAZIL:
            case VIP_LONDON:
            case VIP_SYDNEY:
            case SOUTH_KOREA:
            case VIP_EU_WEST:
            case SOUTH_AFRICA:
            case VIP_US_SOUTH:
            case VIP_FRANKFURT:
            case VIP_SINGAPORE:
            case VIP_EU_CENTRAL:
            case VIP_US_CENTRAL:
            case VIP_SOUTH_KOREA:
            case VIP_SOUTH_AFRICA:
                e.addField("Server Region", server.getRegionRaw(), true);
                break;
        }

        List<Member> members = server.getMembers();
        int bots = 0;
        for (Member member : members) {
            if (member.getUser().isBot())
                bots += 1;
        }

        int membercount = server.getMemberCount();
        int humans = membercount - bots;

        float botpercent = ((float)bots / (float)membercount * 100);
        float humanpercent = ((float)humans / (float)membercount * 100);

        e.addField("Member Count", "Total: " + membercount + "\n" +
                "Bots: " + bots + " - (" + botpercent + "%)\n" +
                "Users: " + humans + " - (" + humanpercent + "%)", true);

        int totalchans = server.getChannels().size();
        int textchans = server.getTextChannels().size();
        int voicechans = server.getVoiceChannels().size();
        int categories = server.getCategories().size();
        int storechans = server.getStoreChannels().size();

        float textpercent = ((float)textchans / (float)totalchans * 100);
        float voicepercent = ((float)voicechans / (float)totalchans * 100);
        float catepercent = ((float)categories / (float)totalchans * 100);
        float storepercent = ((float)storechans / (float)totalchans * 100);

        e.addField("Channel Count", "Total: " + totalchans + "\n" +
                "Text: " + textchans + " (" + textpercent + "%)\n" +
                "Voice: " + voicechans + " (" + voicepercent + "%)\n" +
                "Categories: " + categories + " (" + catepercent + "%)\n" +
                "Store Pages: " + storechans + " (" + storepercent + "%)", true);

        StringBuilder roleNames = new StringBuilder();

        List<Role> roles = server.getRoles();
        for (int i=0; i < roles.size() && i < 50; i++) {
            Role role = roles.get(i);
            roleNames.append(role.getAsMention()).append(" ");
        }

        e.addField("Roles - " + roles.size(), roleNames.toString(), true);

        e.setFooter("Server Created on");
        e.setTimestamp(server.getTimeCreated());

        e.setColor(Color.decode("#00FF00"));

        event.reply(e.build());

    }
}

