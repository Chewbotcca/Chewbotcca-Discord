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

package pw.chew.chewbotcca.menus.message;

import com.jagrosh.jdautilities.command.MessageContextMenu;
import com.jagrosh.jdautilities.command.MessageContextMenuEvent;
import net.dv8tion.jda.api.Permission;
import pw.chew.chewbotcca.util.CommandContext;

public class UnsuppressEmbedsMessageContextMenu extends MessageContextMenu {
    public UnsuppressEmbedsMessageContextMenu() {
        this.name = "Unsuppress Embeds";
        this.contexts = CommandContext.SERVER;
        this.botPermissions = new Permission[]{Permission.MESSAGE_MANAGE};
        this.userPermissions = new Permission[]{Permission.MESSAGE_MANAGE};
    }

    @Override
    protected void execute(MessageContextMenuEvent event) {
        event.getTarget().suppressEmbeds(false).queue(
            unused -> event.reply("Unsuppressed embeds! Note that this does not unsuppress link embeds.").setEphemeral(true).queue(),
            failure -> event.reply("Failed to unsuppress embeds! " + failure.getMessage()).setEphemeral(true).queue()
        );
    }
}
