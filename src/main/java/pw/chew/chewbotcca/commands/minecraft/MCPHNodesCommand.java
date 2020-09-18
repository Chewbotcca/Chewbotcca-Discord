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
import com.mcprohosting.objects.Node;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import pw.chew.chewbotcca.objects.Memory;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// %^mcphnodes command
public class MCPHNodesCommand extends Command {

    public MCPHNodesCommand() {
        this.name = "mcphnodes";
        this.aliases = new String[]{"mcphnode", "mcpronodes"};
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        // If they specified a node or not
        if (commandEvent.getArgs().length() == 0) {
            commandEvent.reply(allNodes().build());
        } else {
            commandEvent.reply(specificNode(commandEvent.getArgs()).build());
        }
    }

    /**
     * Gather info on all nodes
     *
     * @return an embed
     */
    public EmbedBuilder allNodes() {
        // Gather info
        List<Node> downNodes = Memory.getMcproAPI().getNodeStatuses().stream().filter(Node::isOnline).collect(Collectors.toList());
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("MCProHosting Node Statuses", "https://panel.mcprohosting.com/status");
        embed.setDescription("Only showing status for locations with at least 1 down node.");
        if (downNodes.isEmpty()) {
            embed.setDescription("All Nodes are currently functional and responding!");
            return embed;
        }
        Map<String, List<Node>> nodeMap = new HashMap<>();
        // Iterate through each down node
        for (Node node : downNodes) {
            String location = node.getLocation();
            List<Node> list = nodeMap.getOrDefault(location, new ArrayList<>());
            list.add(node);
            nodeMap.put(location, list);
        }
        for (String location : nodeMap.keySet()) {
            List<Node> down = nodeMap.get(location);
            // If there's more than one down node
            List<CharSequence> nodeList = new ArrayList<>();
            for (Node downNode : down) {
                nodeList.add(String.valueOf(downNode.getId()));
            }
            embed.addField(name, "Outages: " + String.join(", ", nodeList), true);
        }
        return embed;
    }

    /**
     * Find info on a specific node
     *
     * @param input the node id
     * @return an embed
     */
    public EmbedBuilder specificNode(String input) {
        // Make sure the input is valid
        int nodeId;
        try {
            nodeId = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return new EmbedBuilder().setTitle("Error occurred!").setDescription("Invalid input!").setColor(Color.decode("#ff0000"));
        }
        // Gather info
        List<Node> nodes = Memory.getMcproAPI().getNodeStatuses();
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("MCProHosting Status for Node " + nodeId, "https://panel.mcprohosting.com/status");
        Node node = getNode(nodes, nodeId);
        // If it doesn't exist
        if (node == null) {
            return new EmbedBuilder().setTitle("Error occurred!").setDescription("Invalid node!").setColor(Color.decode("#ff0000"));
        }
        // Otherwise return info for it
        embed.addField("Location", node.getLocation(), true);
        if (node.isOnline()) {
            embed.addField("Status", "Online", true);
            embed.setColor(Color.decode("#00ff00"));
        } else {
            embed.addField("Status", "Offline", true);
            embed.setColor(Color.decode("#ff0000"));
            embed.setDescription(node.getMessage());
        }
        embed.setFooter("Last Heartbeat");
        embed.setTimestamp(node.getLastHeartbeat());
        return embed;
    }

    /**
     * Get a specific node from a list of nodes
     *
     * @param nodes  the node list
     * @param nodeId the node you want to get
     * @return the node if exists, otherwise null
     */
    public Node getNode(List<Node> nodes, int nodeId) {
        for (Node requestedNode : nodes) {
            if (requestedNode.getId() == nodeId)
                return requestedNode;
        }
        return null;
    }
}
