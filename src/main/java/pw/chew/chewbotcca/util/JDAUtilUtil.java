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
