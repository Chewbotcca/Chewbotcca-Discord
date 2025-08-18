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

package pw.chew.chewbotcca.util;

import net.dv8tion.jda.api.interactions.InteractionContextType;

public class CommandContext {
    public static InteractionContextType[] GLOBAL = {InteractionContextType.GUILD, InteractionContextType.BOT_DM, InteractionContextType.PRIVATE_CHANNEL};
    public static InteractionContextType[] SERVER = {InteractionContextType.GUILD};
    public static InteractionContextType[] NON_SERVER = {InteractionContextType.BOT_DM, InteractionContextType.PRIVATE_CHANNEL};
    public static InteractionContextType[] DM_ONLY = {InteractionContextType.BOT_DM};
}
