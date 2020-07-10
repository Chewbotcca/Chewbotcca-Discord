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
package pw.chew.chewbotcca.commands.minecraft;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import org.json.JSONArray;
import org.json.JSONObject;
import pw.chew.chewbotcca.util.RestClient;

import java.awt.*;
import java.text.DecimalFormat;
import java.time.Instant;
import java.util.ArrayList;

// %^mcphnodes command
public class MCPHNodesCommand extends Command {

    public MCPHNodesCommand() {
        this.name = "mcphnodes";
        this.aliases = new String[]{"mcphnode", "nodestatus"};
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        // If they specified a node or not
        if(commandEvent.getArgs().length() == 0) {
            commandEvent.reply(allNodes().build());
        } else {
            commandEvent.reply(specificNode(commandEvent.getArgs()).build());
        }
    }

    /**
     * Gather info on all nodes
     * @return an embed
     */
    public EmbedBuilder allNodes() {
        // Gather info
        JSONArray data = new JSONArray(RestClient.get("https://chew.pw/mc/pro/status"));
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("MCProHosting Node Statuses", "https://panel.mcprohosting.com/status");
        embed.setDescription("Only showing status for locations with at least 1 down node.");
        // Iterate through each location
        for(int i = 0; i < data.length(); i++) {
            JSONObject loc = data.getJSONObject(i);
            String name = loc.getString("location");
            JSONArray nodes = loc.getJSONArray("nodes");
            int nodeCount = nodes.length();
            int downCount = 0;
            // Go through each node in this location and see if they're online
            ArrayList<CharSequence> down = new ArrayList<>();
            for(int j = 0; j < nodes.length(); j++) {
                JSONObject node = nodes.getJSONObject(j);
                if(!node.getBoolean("online")) {
                    downCount++;
                    down.add(String.valueOf(node.getInt("id")));
                }
            }
            DecimalFormat df = new DecimalFormat("#.##");
            int upCount = nodeCount - downCount;
            String upPercent = df.format((float)upCount / (float)nodeCount * 100);
            // If there's more than one down node
            if(upCount != nodeCount) {
                embed.addField(name, "Status: " + upCount + "/" + nodeCount + " (" + upPercent + "%)\n" +
                        "Outages: " + String.join(", ", down), true);
            }


        }
        return embed;
    }

    /**
     * Find info on a specific node
     * @param nodeId the node id
     * @return an embed
     */
    public EmbedBuilder specificNode(String nodeId) {
        // Make sure the input is valid
        try {
            Integer.parseInt(nodeId);
        } catch(NumberFormatException e) {
            return new EmbedBuilder().setTitle("Error occurred!").setDescription("Invalid input!").setColor(Color.decode("#ff0000"));
        }
        // Gather info
        JSONArray data = new JSONArray(RestClient.get("https://chew.pw/mc/pro/status"));
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("MCProHosting Status for Node " + nodeId, "https://panel.mcprohosting.com/status");
        JSONObject requestedNode = null;
        String location = null;
        // Iterate through each location to find the requested node, if it exists
        for(int i = 0; i < data.length(); i++) {
            JSONObject loc = data.getJSONObject(i);
            String name = loc.getString("location");
            JSONArray nodes = loc.getJSONArray("nodes");
            for(int j = 0; j < nodes.length(); j++) {
                JSONObject node = nodes.getJSONObject(j);
                if(node.getInt("id") == Integer.parseInt(nodeId)) {
                    requestedNode = node;
                    location = name;
                }
            }
        }
        // If it doesn't exist
        if(requestedNode == null) {
            return new EmbedBuilder().setTitle("Error occurred!").setDescription("Invalid node!").setColor(Color.decode("#ff0000"));
        }
        // Otherwise return info for it
        embed.addField("Location", location, true);
        if(requestedNode.getBoolean("online")) {
            embed.addField("Status", "Online", true);
            embed.setColor(Color.decode("#00ff00"));
        } else {
            embed.addField("Status", "Offline", true);
            embed.setColor(Color.decode("#ff0000"));
            embed.setDescription(requestedNode.getString("message"));
        }
        embed.setFooter("Last Heartbeat");
        embed.setTimestamp(Instant.ofEpochSecond(requestedNode.getLong("last_heartbeat_epoch_seconds")));
        return embed;
    }
}
