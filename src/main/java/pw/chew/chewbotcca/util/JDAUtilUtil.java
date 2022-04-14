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
package pw.chew.chewbotcca.util;

import com.jagrosh.jdautilities.menu.ButtonEmbedPaginator;
import com.jagrosh.jdautilities.menu.EmbedPaginator;
import com.jagrosh.jdautilities.menu.Paginator;
import net.dv8tion.jda.api.exceptions.PermissionException;
import pw.chew.chewbotcca.objects.Memory;

import java.util.concurrent.TimeUnit;

public class JDAUtilUtil {
    /**
     * @return a Paginator.Builder object
     */
    public static Paginator.Builder makePaginator() {
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
            .setEventWaiter(Memory.getWaiter())
            .setTimeout(1, TimeUnit.MINUTES)
            .clearItems();
    }

    public static EmbedPaginator.Builder makeEmbedPaginator() {
        return new EmbedPaginator.Builder()
            .waitOnSinglePage(false)
            .setFinalAction(m -> {
                try {
                    m.clearReactions().queue();
                } catch(PermissionException | IllegalStateException ignored) { }
            })
            .setEventWaiter(Memory.getWaiter())
            .setTimeout(1, TimeUnit.MINUTES)
            .clearItems();
    }

    public static ButtonEmbedPaginator.Builder makeButtonEmbedPaginator() {
        return new ButtonEmbedPaginator.Builder()
            .waitOnSinglePage(false)
            .setFinalAction(m -> m.editMessageComponents().queue())
            .setEventWaiter(Memory.getWaiter())
            .setTimeout(1, TimeUnit.MINUTES)
            .clearItems();
    }
}
