package pw.chew.Chewbotcca.commands.fun;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import pw.chew.Chewbotcca.util.RestClient;

public class NumberFactCommand extends Command {
    public NumberFactCommand() {
        this.name = "numberfact";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        String[] args = commandEvent.getArgs().split(" ");
        if(args.length == 0) {
            commandEvent.reply("Please specify a number to find a fact for! Also, optionally specify what type of fact, choices: `trivia`, `year`, `date`, `math`.");
            return;
        }
        String number = args[0];
        try {
            Long.parseLong(args[0]);
        } catch (NumberFormatException e) {
            commandEvent.reply("Invalid number input!");
            return;
        }
        String type;
        if(args.length > 1) {
            type = args[1].toLowerCase();
        } else {
            type = "trivia";
        }

        String url = switch (type) {
            default -> "http://numbersapi.com/" + number + "?notfound=";
            case "math", "year", "date" -> "http://numbersapi.com/" + number + "/" + type + "?notfound=";
        };

        EmbedBuilder embed = new EmbedBuilder();

        String facto = RestClient.get(url + "floor");
        if(facto.equals("-Infinity is negative infinity."))
            facto = RestClient.get(url + "ceil");

        embed.setTitle("Did you know?");
        embed.setColor(0x85bae7);
        embed.setDescription(facto);
        embed.setAuthor("Number Facts!");
        if (!facto.split(" ")[0].equals(number) && !type.equals("date"))
            embed.setFooter("Your number didn't have a fact, so a number was approximated for you.");

        commandEvent.reply(embed.build());
    }
}
