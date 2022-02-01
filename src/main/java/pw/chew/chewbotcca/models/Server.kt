package pw.chew.chewbotcca.models

import javax.persistence.*

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
        // Dynamically call the method based on the info
        this.javaClass.getMethod("set${info.capitalize()}", String::class.java).invoke(this, newValue)
    }
}
