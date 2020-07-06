package pw.chew.chewbotcca.commands.github;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import org.kohsuke.github.*;
import pw.chew.chewbotcca.util.PropertiesManager;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GHIssueCommand extends Command {
    final static ArrayList<String> describedIds = new ArrayList<>();

    public GHIssueCommand() {
        this.name = "ghissue";
        this.aliases = new String[]{"ghpull"};
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        String[] args = commandEvent.getArgs().split(" ");
        if(args.length < 2) {
            commandEvent.reply("Please provide a Repository and an Issue Number!");
            return;
        }
        String repo = args[0];
        int issueNum;
        try {
            issueNum = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            commandEvent.reply("Invalid number provided for issue number. Must be a number. Make sure you're doing Repo then Number!");
            return;
        }
        GitHub github;
        commandEvent.getChannel().sendTyping().queue();
        try {
            github = new GitHubBuilder().withOAuthToken(PropertiesManager.getGithubToken()).build();
        } catch (IOException e) {
            e.printStackTrace();
            commandEvent.reply("Error occurred initializing GitHub. How did this happen?");
            return;
        }
        GHIssue issue;
        try {
            issue = github.getRepository(repo).getIssue(issueNum);
        } catch (IOException e) {
            commandEvent.reply("Invalid issue number or an invalid repository was provided. Please ensure the issue exists and the repository is public.");
            return;
        }

        commandEvent.reply(issueBuilder(issue, repo, github, issueNum).build());
    }

    public EmbedBuilder issueBuilder(GHIssue issue, String repo, GitHub github, int issueNum) {
        EmbedBuilder e = new EmbedBuilder();
        e.setTitle(issue.getTitle());
        if(issue.getBody() != null) {
            if (issue.getBody().length() > 200) {
                e.setDescription(issue.getBody().substring(0, 199) + "...");
            } else {
                e.setDescription(issue.getBody());
            }
        }
        boolean open = issue.getState() == GHIssueState.OPEN;
        boolean merged = false;
        if(issue.isPullRequest()) {
            e.setAuthor("Information for Pull Request #" + issueNum + " in " + repo, String.valueOf(issue.getUrl()));
            try {
                GHPullRequest pull = github.getRepository(repo).getPullRequest(issueNum);
                merged = pull.isMerged();
            } catch (IOException ioException) {
                ioException.printStackTrace();
                e.setTitle("Error!");
                e.setDescription("Error occurred initializing Pull request. How did this happen?");
                return e;
            }
        } else {
            e.setAuthor("Information for Issue #" + issueNum + " in " + repo, String.valueOf(issue.getUrl()));
        }
        if(merged) {
            e.setColor(Color.decode("#6f42c1"));
            e.addField("Status", "Merged", true);
        } else if(open) {
            e.setColor(Color.decode("#2cbe4e"));
            e.addField("Status", "Open", true);
        } else {
            e.setColor(Color.decode("#cb2431"));
            e.addField("Status", "Closed", true);
        }
        try {
            GHUser author = issue.getUser();
            if(author.getName() != null)
                e.addField("Author", "[" + author.getLogin() + " (" + author.getName() + ")" + "](https://github.com/" + author.getLogin() + ")", true);
            else
                e.addField("Author", "[" + author.getLogin() + "](https://github.com/" + author.getLogin() + ")", true);
        } catch (IOException ioException) {
            e.addField("Author", "Unknown Author", true);
        }
        try {
            e.setFooter("Opened");
            e.setTimestamp(issue.getCreatedAt().toInstant());
            List<CharSequence> labels = new ArrayList<>();
            for(GHLabel label : issue.getLabels()) {
                labels.add(label.getName());
            }
            if(labels.size() > 0)
                e.addField("Labels", String.join(", ", labels), true);
            List<CharSequence> assignees = new ArrayList<>();
            for(GHUser assignee : issue.getAssignees()) {
                assignees.add(assignee.getLogin());
            }
            if(assignees.size() > 0)
                e.addField("Assignees", String.join(", ", assignees), true);
        } catch (IOException ignored) { }
        return e;
    }

    public static boolean didDescribe(String id) {
        return describedIds.contains(id);
    }

    public static void described(String id) {
        describedIds.add(id);
    }
}
