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
package pw.chew.chewbotcca.util;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.Paginator;
import net.dv8tion.jda.api.exceptions.PermissionException;

import java.util.concurrent.TimeUnit;

public class JDAUtilUtil {
    /**
     * @return a Paginator.Builder object
     */
    public static Paginator.Builder makePaginator(EventWaiter waiter) {
        return new Paginator.Builder().setColumns(1)
            .setItemsPerPage(10)
            .showPageNumbers(true)
            .waitOnSinglePage(false)
            .useNumberedItems(false)
            .setFinalAction(m -> {
                try {
                    m.clearReactions().queue();
                } catch(PermissionException ignored) { }
            })
            .setEventWaiter(waiter)
            .setTimeout(1, TimeUnit.MINUTES)
            .clearItems();
    }
}
