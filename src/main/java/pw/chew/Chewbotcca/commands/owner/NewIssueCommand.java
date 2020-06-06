package pw.chew.Chewbotcca.commands.owner;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import pw.chew.Chewbotcca.Main;

import java.io.IOException;

public class NewIssueCommand extends Command {

    public NewIssueCommand() {
        this.name = "issue";
        this.guildOnly = false;
        this.ownerCommand = true;
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        GitHub github;
        GHRepository repo;
        commandEvent.getChannel().sendTyping().queue();
        try {
            github = new GitHubBuilder().withOAuthToken(Main.getProp().getProperty("github")).build();
            repo = github.getRepository("Chewbotcca/Discord");
        } catch (IOException e) {
            e.printStackTrace();
            commandEvent.reply("Error occurred initializing GitHub. How did this happen?");
            return;
        }

        try {
            GHIssue issue = repo.createIssue(commandEvent.getArgs()).assignee(github.getMyself()).create();
            commandEvent.reply("Issue created @ " + issue.getUrl());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
