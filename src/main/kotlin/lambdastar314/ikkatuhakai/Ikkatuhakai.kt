package lambdastar314.ikkatuhakai

import lambdastar314.ikkatuhakai.ikkatu.Ikkatu
import org.bukkit.Keyed
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.plugin.java.JavaPlugin
import java.lang.reflect.Method
import java.util.*

class Ikkatuhakai : JavaPlugin() {

    companion object{
        var ikkatuhalais = Collections.synchronizedMap(HashMap<String, Ikkatu>())

    }

    override fun onEnable() {
        // Plugin startup logic
        logger.info("Plugin waked")
        getCommand("ikkatu")!!.setExecutor(IkkatuCommand(this))

        ikkatuhalais["mine"] = Ikkatu(this, "mine")
        ikkatuhalais["dig"] = Ikkatu(this, "dig")
        ikkatuhalais["cut"] = Ikkatu(this, "cut")
    }

    override fun onDisable() {
        // Plugin shutdown logic
        logger.info("Plugin shutdown")
    }
}
