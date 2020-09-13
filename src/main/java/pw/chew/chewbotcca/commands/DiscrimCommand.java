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
package pw.chew.chewbotcca.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.Paginator;
import net.dv8tion.jda.api.entities.User;
import pw.chew.chewbotcca.util.JDAUtilUtil;

// %^discrim command
public class DiscrimCommand extends Command {
    private final EventWaiter waiter;

    public DiscrimCommand(EventWaiter waiter) {
        this.name = "discrim";
        this.guildOnly = false;
        this.waiter = waiter;
    }

    @Override
    protected void execute(CommandEvent event) {
        event.getChannel().sendTyping().queue();
        String discrim = event.getAuthor().getDiscriminator();
        if (event.getArgs().length() == 4 && event.getArgs().matches("[0-9]{4}")) {
            discrim = event.getArgs();
        }

        Paginator.Builder pbuilder = JDAUtilUtil.makePaginator(waiter);
        pbuilder.setText("Users with discriminator #" + discrim
            + "\nCached users: " + event.getJDA().getUserCache().size());
        for (User user : event.getJDA().getUserCache()) {
            if (user.getDiscriminator().equals(discrim)) {
                pbuilder.addItems(user.getAsTag());
            }
        }

        Paginator p = pbuilder.build();
        p.paginate(event.getChannel(), 1);
    }
}
