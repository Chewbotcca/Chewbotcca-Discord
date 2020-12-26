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
package pw.chew.chewbotcca.commands.owner;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHIssueBuilder;
import org.kohsuke.github.GHRepository;
import pw.chew.chewbotcca.objects.Memory;
import pw.chew.chewbotcca.util.FlagParser;

import java.io.IOException;
import java.util.Map;

// %^newissue command
public class NewIssueCommand extends Command {

    public NewIssueCommand() {
        this.name = "issue";
        this.guildOnly = false;
        this.ownerCommand = true;
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        GHRepository repo;
        commandEvent.getChannel().sendTyping().queue();
        try {
            repo = Memory.getGithub().getRepository("Chewbotcca/Discord");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error occurred getting repo!");
            return;
        }

        String args = commandEvent.getArgs();
        String title = args.split(" --")[0];
        Map<String, String> flags = FlagParser.parse(args);

        try {
            GHIssueBuilder issueBuilder = repo.createIssue(title).assignee(Memory.getGithub().getMyself());
            if(flags.containsKey("description"))
                issueBuilder.body(flags.get("description"));
            if(flags.containsKey("label"))
                issueBuilder.label(flags.get("label"));
            GHIssue issue = issueBuilder.create();
            commandEvent.reply("Issue created @ " + issue.getHtmlUrl());
        } catch (IOException e) {
            e.printStackTrace();
            commandEvent.reply("Error occurred creating Issue, check console for more information.");
        }
    }
}
