/*
 * Copyright (C) 2025 Chewbotcca
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
package pw.chew.chewbotcca.commands.services;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.container.ContainerChildComponent;
import net.dv8tion.jda.api.components.replacer.ComponentReplacer;
import net.dv8tion.jda.api.components.section.Section;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.components.thumbnail.Thumbnail;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.json.JSONArray;
import org.json.JSONObject;
import pw.chew.chewbotcca.util.CommandContext;
import pw.chew.chewbotcca.util.EmojiUtil;
import pw.chew.chewbotcca.util.MiscUtil;
import pw.chew.chewbotcca.util.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ToSDRCommand extends SlashCommand {
    private static final Map<Integer, JSONObject> cache = new HashMap<>();

    public ToSDRCommand() {
        this.name = "tosdr";
        this.help = "Look up information about a service on Terms of Service; Didn't Read";
        this.contexts = CommandContext.GLOBAL;
        this.options = Collections.singletonList(
            new OptionData(OptionType.INTEGER, "service", "The service to look up", true, true)
        );
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        int service = (int) event.optLong("service", 0);

        JSONObject data = retrieveServiceDetails(service);
        cache.put(service, data);

        event.replyComponents(buildContainer(data)).useComponentsV2().queue();
    }

    /**
     * Retrieves info about a service from ToS;DR
     *
     * @param id the service ID
     * @return details
     */
    public static JSONObject retrieveServiceDetails(int id) {
        JSONObject data = RestClient.get("https://api.tosdr.org/service/v3/?id=" + id).asJSONObject();

        if (data.has("detail")) {
            throw new IllegalArgumentException("Could not get service! " + data.getString("detail"));
        }

        return data;
    }

    public static void handleInteraction(ButtonInteractionEvent event, String[] parts) {
        // well, parts is "tosdr:cont:id:page:pagenum"
        int id = MiscUtil.asInt(parts[2]);
        int pageNum = MiscUtil.asInt(parts[4]);

        // Get data from cache, or retrieve if we don't have it.
        JSONObject data = cache.get(id);
        if (data == null) {
            data = retrieveServiceDetails(id);
        }

        var tree = event.getMessage().getComponentTree()
            .replace(ComponentReplacer.byUniqueId(67, buildPointsSection(data, pageNum).withUniqueId(67)));

        var pageButtons = buildPointsButtons(data, pageNum);
        if (pageButtons != null) {
            tree = tree.replace(ComponentReplacer.byUniqueId(68, pageButtons.withUniqueId(68)));
        }

        event.editComponents(tree).useComponentsV2().queue();
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event) {
        // Get input
        String input = event.getOptionsByName("service").getFirst().getAsString();
        String encoded = URLEncoder.encode(input, StandardCharsets.UTF_8);

        // Sanity
        if (input.isBlank()) {
            event.replyChoices().queue();
            return;
        }

        // Query API
        JSONObject results = RestClient.get("https://api.tosdr.org/search/v5?query=" + encoded).asJSONObject();

        // We only care about name and ID
        JSONArray services = results.getJSONArray("services");
        List<JSONObject> serviceList = MiscUtil.toList(services, JSONObject.class);

        List<Command.Choice> choices = serviceList.stream()
            .map(j -> new Command.Choice(j.getString("name"), j.getInt("id")))
            .distinct()
            .limit(25)
            .toList();

        event.replyChoices(choices).queue();
    }

    /**
     * Builds the container for the embed.
     *
     * @param data the service data
     * @return the container
     */
    private static Container buildContainer(JSONObject data) {
        List<ContainerChildComponent> componentParts = new ArrayList<>(List.of(
            Section.of(
                // Due to a bug in JDA, we can't show the actual image since it might be a 404,
                // which means we can't edit the component. TODO: Replace with below when fixed.
                // Thumbnail.fromUrl(data.getString("image")),
                // This is the ToS;DR icon; we'll use it for now.
                Thumbnail.fromUrl("https://s3.tosdr.org/branding/tosdr-icon-128.png"),
                TextDisplay.of("## [ToS;DR Lookup](%s)\n### Service: %s\nRating: %s"
                    .formatted(
                        "https://tosdr.org/en/service/%s".formatted(data.getInt("id")),
                        data.getString("name"),
                        data.getString("rating")
                    ))
            ),
            Separator.createDivider(Separator.Spacing.SMALL),
            buildPointsSection(data, 1).withUniqueId(67)
        ));

        // Add buttons if needed
        ActionRow buttons = buildPointsButtons(data, 1);
        if (buttons != null) componentParts.add(buttons.withUniqueId(68));

        componentParts.add(Separator.createDivider(Separator.Spacing.SMALL));

        // Add doc buttons
        componentParts.addAll(buildDocsEmbed(data));

        return Container.of(componentParts);
    }

    /**
     * Builds a section of points to be displayed based on the provided JSON data and the specified page number.
     *
     * @param data the service data
     * @param page the page number to display
     * @return a {@code TextDisplay} object containing the formatted points for the specified page
     */
    private static TextDisplay buildPointsSection(JSONObject data, int page) {
        final int pointsPerPage = 5;
        int totalPoints = data.getJSONArray("points").length();
        List<JSONObject> pointList = MiscUtil.toList(data.getJSONArray("points"), JSONObject.class);

        if (totalPoints == 0) {
            return TextDisplay.of("No points found!");
        }

        // All points formatted
        List<String> points = new ArrayList<>();
        for (JSONObject point : pointList) {
            EmojiUtil.Emoji emoji = switch (point.getJSONObject("case").getString("classification")) {
                case "bad" -> EmojiUtil.Emoji.BAD_TOS;
                case "good" -> EmojiUtil.Emoji.GOOD_TOS;
                case "blocker" -> EmojiUtil.Emoji.BLOCKER_TOS;
                case null, default -> EmojiUtil.Emoji.NEUTRAL_TOS;
            };

            String title = point.getString("title");
            String description = point.getJSONObject("case").getString("description").trim().split("\n")[0];

            StringBuilder builder = new StringBuilder();
            builder.append(emoji.mention()).append(" ").append(title);

            if (!description.isBlank()) {
                builder.append("\n");
                builder.append("-# ").append(description);
            }

            builder.append("\n");

            points.add(builder.toString());
        }

        // Calculate start and end points
        int start = (page - 1) * pointsPerPage;
        int end = Math.min(start + pointsPerPage, totalPoints);

        return TextDisplay.of(String.join("\n", points.subList(start, end)));
    }

    /**
     * Constructs an {@code ActionRow} containing buttons for navigating points pages.
     *
     * @param data the service data
     * @param page the current page number
     * @return an {@code ActionRow} object containing navigation buttons
     */
    private static ActionRow buildPointsButtons(JSONObject data, int page) {
        int id = data.getInt("id");
        int totalPoints = data.getJSONArray("points").length();

        int totalPages = (int) Math.ceil((double) totalPoints / 5);

        if (totalPages <= 1) {
            return null;
        }

        int prevPage = Math.max(page - 1, 1);
        int nextPage = Math.min(page + 1, totalPages);

        return ActionRow.of(
            Button.primary("tosdr:cont:%s:points:1:first".formatted(id), "<<").withDisabled(page == 1),
            Button.primary("tosdr:cont:%s:points:%s".formatted(id, prevPage), "<").withDisabled(page == 1),
            Button.secondary("tosdr:cont:%s:points:page", "Page %s/%s".formatted(page, totalPages)).asDisabled(),
            Button.primary("tosdr:cont:%s:points:%s".formatted(id, nextPage), ">").withDisabled(page == totalPages),
            Button.primary("tosdr:cont:%s:points:%s:last".formatted(id, totalPages), ">>").withDisabled(page == totalPages)
        );
    }

    /**
     * Builds a list of {@code ActionRow} objects containing buttons for accessing document links.
     *
     * @param data the service data
     * @return a list of {@code ActionRow} objects, each containing buttons grouped in sets of up to 5
     */
    private static List<ActionRow> buildDocsEmbed(JSONObject data) {
        List<ActionRow> rows = new ArrayList<>();
        List<Button> buttons = new ArrayList<>();

        List<JSONObject> docList = MiscUtil.toList(data.getJSONArray("documents"), JSONObject.class);
        List<JSONObject> pointList = MiscUtil.toList(data.getJSONArray("points"), JSONObject.class);

        // To save on room let's only get documents that are used
        List<Integer> docIds = pointList.stream()
            .map(j -> j.optInt("document_id", 0))
            .filter(id -> id != 0)
            .distinct().toList();

        for (JSONObject doc : docList) {
            int id = doc.getInt("id");

            if (docIds.contains(id) || docIds.isEmpty()) {
                buttons.add(Button.link(doc.getString("url"), doc.getString("name")));
            }
        }

        // Group buttons into groups of 5
        for (int i = 0; i < buttons.size(); i += 5) {
            rows.add(ActionRow.of(buttons.subList(i, Math.min(i + 5, buttons.size()))));
        }

        return rows;
    }
}
