package pw.chew.chewbotcca.models

import pw.chew.chewbotcca.util.MiscUtil
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Lob
import javax.persistence.Table

@Entity
@Table(name = "chewbotcca_servers")
open class Server {
    @Id
    @Column(name = "serverid", nullable = false)
    open var id: Long? = null

    @Lob
    @Column(name = "prefix")
    open var prefix: String? = null

    @Column(name = "bypass_urban_nsfw", nullable = false)
    open var bypassUrbanNsfw: Boolean? = false

    fun setString(info: String, newValue: String) {
        val name = MiscUtil.capitalize(info).replace(" ", "")
        // Dynamically call the method based on the info
        this.javaClass.getMethod("set${name}", String::class.java).invoke(this, newValue)
    }
}
