/*
 * Copyright (C) 2023 Chewbotcca
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
package pw.chew.chewbotcca.objects.services;

import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import pw.chew.chewbotcca.util.MiscUtil;

import java.awt.Color;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Represents a Modrinth project (mod/plugin, etc).
 *
 * @param data The JSON data of the project.
 */
public record ModrinthProject(JSONObject data) {
    public String title() {
        return data.getString("title");
    }

    public String description() {
        return data.getString("description");
    }

    public String slug() {
        return data.getString("slug");
    }

    public String projectType() {
        return data.getString("project_type");
    }

    public String author() {
        return data.getString("author");
    }

    public String authorURL() {
        return String.format("https://www.modrinth.com/user/%s", author());
    }

    public OffsetDateTime createdDate() {
        return OffsetDateTime.parse(data.getString("date_created"));
    }

    public OffsetDateTime modifiedDate() {
        return OffsetDateTime.parse(data.getString("date_modified"));
    }

    public String clientSide() {
        return MiscUtil.capitalize(data.getString("client_side"));
    }

    public String serverSide() {
        return MiscUtil.capitalize(data.getString("server_side"));
    }

    public long downloads() {
        return data.getLong("downloads");
    }

    public long followers() {
        return data.getLong("follows");
    }

    public Color color() {
        return new Color(data.getInt("color"));
    }

    public String pageURL() {
        return String.format("https://modrinth.com/%s/%s", this.projectType(), this.slug());
    }

    @Nullable
    public String iconURL() {
        return data.getString("icon_url").isBlank() ? null : data.getString("icon_url");
    }

    public List<String> categories() {
        return data.getJSONArray("categories").toList().stream().map(Object::toString).map(MiscUtil::capitalize).toList();
    }

    public String latestVersion() {
        return data.getString("latest_version");
    }
}
