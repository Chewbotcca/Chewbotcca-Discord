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
package pw.chew.chewbotcca.commands.fun;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.mediagallery.MediaGallery;
import net.dv8tion.jda.api.components.mediagallery.MediaGalleryItem;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import pw.chew.chewbotcca.util.CommandContext;
import pw.chew.chewbotcca.util.RestClient;

/**
 * <h2><code>/dog</code> Command</h2>
 *
 * <a href="https://help.chew.pro/bots/discord/chewbotcca/commands/dog">Docs</a>
 */
public class DogCommand extends SlashCommand {
    public DogCommand() {
        this.name = "dog";
        this.help = "Gets a random dog picture! Bark, woof, purr?";
        this.contexts = CommandContext.GLOBAL;
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        // Get a dog and bark it, i mean send it, am not furry i swear
        String dog = RestClient.get("https://random.dog/woof.json").asJSONObject().getString("url");

        event.replyComponents(Container.of(
            TextDisplay.of("Adorable."),
            MediaGallery.of(MediaGalleryItem.fromUrl(dog))
        )).useComponentsV2().queue();
    }
}
