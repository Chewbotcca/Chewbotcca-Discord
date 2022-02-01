package pw.chew.chewbotcca.models

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Lob
import javax.persistence.Table

@Entity
@Table(name = "chewbotcca_profiles")
open class Profile {
    @Id
    @Column(name = "userid", nullable = false, length = 20)
    open var id: String? = null

    @Lob
    @Column(name = "lastfm")
    open var lastfm: String? = null

    @Lob
    @Column(name = "github")
    open var github: String? = null

    fun setString(info: String, newValue: String) {
        // Dynamically call the method based on the info
        this.javaClass.getMethod("set${info.capitalize()}", String::class.java).invoke(this, newValue)
    }
}
