/*
 * Copyright (C) 2024 Chewbotcca
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
package pw.chew.chewbotcca.menus.message;

import com.jagrosh.jdautilities.command.MessageContextMenu;
import com.jagrosh.jdautilities.command.MessageContextMenuEvent;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import org.jetbrains.annotations.Nullable;
import org.slf4j.LoggerFactory;
import pw.chew.chewbotcca.unfurls.GenericUnfurler;
import pw.chew.chewbotcca.unfurls.GitHubUnfurler;
import pw.chew.chewbotcca.unfurls.MCIssueUnfurler;
import pw.chew.chewbotcca.unfurls.MemeratorUnfurler;
import pw.chew.chewbotcca.unfurls.YouTubeLinkUnfurler;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class UnfurlMessageContextMenu extends MessageContextMenu {
    private static final List<GenericUnfurler> unfurlers = new ArrayList<>();

    static {
        unfurlers.add(new GitHubUnfurler());
        unfurlers.add(new MCIssueUnfurler());
        unfurlers.add(new MemeratorUnfurler());
        unfurlers.add(new YouTubeLinkUnfurler());
    }

    public UnfurlMessageContextMenu() {
        this.name = "Unfurl Link";
        this.contexts = new InteractionContextType[]{InteractionContextType.GUILD, InteractionContextType.BOT_DM, InteractionContextType.PRIVATE_CHANNEL};
    }

    @Override
    protected void execute(MessageContextMenuEvent event) {
        Message message = event.getTarget();

        List<MessageEmbed> unfurl = unfurlMessage(message);

        if (unfurl != null) {
            event.replyEmbeds(unfurl).setEphemeral(true).queue();
        } else {
            event.reply("""
                Could not find a link to unfurl.
                
                Supported sites: YouTube (videos), GitHub (issues/PRs), Mojira/Spigot Jira (bugs), and Memerator (memes/users).
                
                If you think this is a bug, or you think it should be unfurled, please [report it](https://github.com/Chewbotcca/Discord/issues).
                """).setEphemeral(true).queue();
        }
    }

    /**
     * Returns an Embed of the unfurled message
     *
     * @param msg The message to be unfurled
     * @return an embed of the message
     */
    @Nullable
    public List<MessageEmbed> unfurlMessage(Message msg) {
        String content = msg.getContentStripped().replace(">", "");

        // Find all the links in the message
        List<String> validLinks = new ArrayList<>();

        for (String link : content.split(" ")) {
            // Check if it's a valid link
            try {
                new URL(link);
                validLinks.add(link);
                LoggerFactory.getLogger(this.getClass()).debug("Found link: " + link);
            } catch (MalformedURLException ignored) {
            }
        }

        if (validLinks.isEmpty()) {
            LoggerFactory.getLogger(this.getClass()).debug("No links found");
            return null;
        }

        List<MessageEmbed> embeds = new ArrayList<>();
        for (String link : validLinks) {
            for (GenericUnfurler unfurler : unfurlers) {
                if (!unfurler.checkLink(link)) continue;

                MessageEmbed embed = unfurler.unfurl(link);
                if (embed != null) {
                    embeds.add(embed);
                }
            }
        }

        if (!embeds.isEmpty()) {
            return embeds;
        }

        return null;
    }
}
