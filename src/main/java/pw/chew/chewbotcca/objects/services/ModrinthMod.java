/*
 * Copyright (C) 2021 Chewbotcca
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

import java.time.OffsetDateTime;
import java.util.List;

public record ModrinthMod(JSONObject data) {
    public String getTitle() {
        return data.getString("title");
    }

    public String getDescription() {
        return data.getString("description");
    }

    public String getAuthor() {
        return data.getString("author");
    }

    public String getAuthorURL() {
        return data.getString("author_url");
    }

    public OffsetDateTime getDateCreated() {
        return OffsetDateTime.parse(data.getString("date_created"));
    }

    public OffsetDateTime getDateModified() {
        return OffsetDateTime.parse(data.getString("date_modified"));
    }

    public String getStatus() {
        return data.getString("status");
    }

    public String getClientSide() {
        return MiscUtil.capitalize(data.getString("client_side"));
    }

    public String getServerSide() {
        return MiscUtil.capitalize(data.getString("server_side"));
    }

    public long getDownloads() {
        return data.getLong("downloads");
    }

    public String getPageURL() {
        return data.getString("page_url");
    }

    @Nullable
    public String getIconURL() {
        return data.getString("icon_url").isBlank() ? null : data.getString("icon_url");
    }

    public List<String> getCategories() {
        return data.getJSONArray("categories").toList().stream().map(Object::toString).map(MiscUtil::capitalize).toList();
    }

    public String getLatestVersion() {
        return data.getString("latest_version");
    }
}
