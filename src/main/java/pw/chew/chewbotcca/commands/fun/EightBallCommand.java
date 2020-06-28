package pw.chew.chewbotcca.commands.fun;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;

import java.awt.*;
import java.util.Random;

public class EightBallCommand extends Command {
    String[] goodResponses = new String[]{
            "As I see it, yes",
            "It is certain",
            "It is decidedly so",
            "Most likely",
            "Outlook good",
            "Signs point to yes",
            "One would be wise to think so",
            "Naturally",
            "Without a doubt",
            "Yes",
            "You may rely on it",
            "You can count on it"
    };
    String[] neutralResponses = new String[]{
            "Better not tell you now!",
            "Ask again later.",
            "Cannot predict now",
            "Cool down enabled! Please try again.",
            "Concentrate and ask again.",
            "Rhetorical questions can be answered in solo",
            "Maybe..."
    };
    String[] badResponses = new String[]{
            "You're kidding, right?",
            "Don't count on it.",
            "In your dreams",
            "My reply is no",
            "Outlook not so good",
            "My undisclosed sources say NO",
            "One would be wise to think not",
            "Very doubtful"
    };
    Random rand = new Random();

    public EightBallCommand() {
        this.name = "8ball";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        String question = commandEvent.getArgs();
        int response = rand.nextInt(3);
        EmbedBuilder e = new EmbedBuilder();
        e.setTitle(":question: Question");
        e.setDescription(question);
        String answer = null;
        switch (response) {
            case 0 -> {
                answer = getRandom(goodResponses);
                e.setColor(Color.decode("#00FF00"));
            }
            case 1 -> {
                answer = getRandom(neutralResponses);
                e.setColor(Color.decode("#FFFF00"));
            }
            case 2 -> {
                answer = getRandom(badResponses);
                e.setColor(Color.decode("#FF0000"));
            }
        }
        e.addField(":8ball: 8ball says", answer, false);
        commandEvent.reply(e.build());
    }

    public String getRandom(String[] array) {
        int rnd = new Random().nextInt(array.length);
        return array[rnd];
    }
}
