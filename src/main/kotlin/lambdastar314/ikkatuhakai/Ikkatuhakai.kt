package lambdastar314.ikkatuhakai

import org.bukkit.plugin.java.JavaPlugin

class Ikkatuhakai : JavaPlugin() {
    override fun onEnable() {
        // Plugin startup logic
        logger.info("Plugin waked")
    }

    override fun onDisable() {
        // Plugin shutdown logic
        logger.info("Plugin shutdown")
    }
}