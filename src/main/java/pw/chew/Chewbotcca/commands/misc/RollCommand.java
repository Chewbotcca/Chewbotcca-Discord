package pw.chew.Chewbotcca.commands.misc;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;

import java.util.concurrent.ThreadLocalRandom;

public class RollCommand extends Command {
    public RollCommand() {
        this.name = "roll";
        this.guildOnly = false;
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        String args = commandEvent.getArgs();
        if(args.equals("")) {
            args = "1d6";
        }

        String[] types = args.split("d");
        int dice = Integer.parseInt(types[0]);
        int sides = 0;
        if(types.length < 2) {
            sides = 6;
        } else {
            sides = Integer.parseInt(types[1]);
        }
        if(dice < 1) {
            commandEvent.reply("You must roll at least 1 die.");
        }
        if(sides < 1) {
            commandEvent.reply("Sides cannot be less than 1!");
            return;
        }
        int total = 0;
        for(int i = 0; i < dice; i++) {
            total += ThreadLocalRandom.current().nextInt(1, sides + 1);
        }

        commandEvent.reply(new EmbedBuilder()
                .setTitle("Dice Roll \uD83C\uDFB2")
                .addField("Dice", String.valueOf(dice), true)
                .addField("Sides", String.valueOf(sides), true)
                .addField("Total", String.valueOf(total), false)
                .build()
        );
    }
}

