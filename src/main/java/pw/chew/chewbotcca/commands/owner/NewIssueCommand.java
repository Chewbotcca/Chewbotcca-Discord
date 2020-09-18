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

import java.io.IOException;

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

        boolean hasDescription = args.contains("--description");
        boolean hasLabel = args.contains("--label");

        String title;
        String description = null;
        String label = null;
        if(!hasDescription && !hasLabel) {
            title = args;
        } else if (hasDescription && !hasLabel) {
            title = args.split(" --description")[0];
            description = args.split("--description ")[1];
        } else {
            title = args.split(" --description")[0];
            description = args.split("--description ")[1].split(" --label")[0];
            label = args.split("--label ")[1];
        }

        try {
            GHIssueBuilder issueBuilder = repo.createIssue(title).assignee(Memory.getGithub().getMyself());
            if(hasDescription)
                issueBuilder.body(description);
            if(hasLabel)
                issueBuilder.label(label);
            GHIssue issue = issueBuilder.create();
            commandEvent.reply("Issue created @ " + issue.getHtmlUrl());
        } catch (IOException e) {
            e.printStackTrace();
            commandEvent.reply("Error occurred creating Issue, check console for more information.");
        }
    }
}
