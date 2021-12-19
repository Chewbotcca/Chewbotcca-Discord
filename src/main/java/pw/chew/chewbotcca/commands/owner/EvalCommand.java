/*
 * Copyright (C) 2021 Chewbotcca
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
/*
 * This code was adapted from the Groovy discord bot and modified to fit
 * this software.
 *
 * @see https://groovy.bot
 */
package pw.chew.chewbotcca.commands.owner;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class EvalCommand extends Command {

    public EvalCommand() {
        this.name = "eval";
        this.ownerCommand = true;
    }

    @Override
    protected void execute(CommandEvent event) {
        Map<String, Object> variables = Map.of(
            "event", event,
            "message", event.getMessage()
        );
        Binding binding = new Binding(variables);
        GroovyShell shell = new GroovyShell(binding);

        try {
            String args = event.getArgs();
            // Strip ``` from the beginning and end of the args
            args = args.trim();
            if (args.startsWith("```java") && args.endsWith("```")) {
                args = args.substring(7, args.length() - 3);
            }

            Object resp = shell.evaluate(args);
            String respString = String.valueOf(resp);
            event.reply(new EmbedBuilder()
                .setTitle("Evaluated successfully!")
                .setDescription(respString)
                .setColor(Color.GREEN)
                .build());
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String stackTrace = sw.toString();
            InputStream stream = new ByteArrayInputStream(stackTrace.getBytes(StandardCharsets.UTF_8));

            // Send the file to the discord channel
            event.getChannel().sendMessage("Error occurred!").addFile(stream, "error.txt").queue();
        }
    }
}
