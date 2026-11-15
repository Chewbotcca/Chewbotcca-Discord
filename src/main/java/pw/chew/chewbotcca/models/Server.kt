/*
 * Copyright (C) 2026 Chewbotcca
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

package pw.chew.chewbotcca.models

import pw.chew.chewbotcca.util.MiscUtil
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Lob
import jakarta.persistence.Table

@Entity
@Table(name = "chewbotcca_servers")
class Server {
    @Id
    @Column(name = "serverid", nullable = false)
    var id: Long? = null

    @Lob
    @Column(name = "prefix")
    var prefix: String? = null

    @Column(name = "bypass_urban_nsfw", nullable = false)
    var bypassUrbanNsfw: Boolean? = false

    fun setString(info: String, newValue: String) {
        val name = MiscUtil.capitalize(info).replace(" ", "")
        // Dynamically call the method based on the info
        this.javaClass.getMethod("set${name}", String::class.java).invoke(this, newValue)
    }
}
