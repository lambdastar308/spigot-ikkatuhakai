package lambdastar314.ikkatuhakai.ikkatu

import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.util.*


open class Ikkatu(plugin: JavaPlugin, name: String) :Listener{
    var status = Collections.synchronizedMap(HashMap<String, Boolean>())
    var leaves = Collections.synchronizedSet(HashSet<String>())
    var blocks = Collections.synchronizedSet(HashSet<String>())
    var tools = Collections.synchronizedSet(HashSet<String>())
    var limit: Int = 10

    init {
        load(plugin, name)
    }

    fun load(plugin: JavaPlugin, name: String) {
        plugin.logger.info("loading $name's config")
        val configF = File(plugin.dataFolder, "$name.yml")
        plugin.saveResource("$name.yml", false)
        leaves.clear()
        blocks.clear()
        tools.clear()
        val config = YamlConfiguration.loadConfiguration(configF)
        config.getStringList("leaves").stream().forEach(leaves::add)
        config.getStringList("blocks").stream().forEach(blocks::add)
        config.getStringList("tools").stream().forEach(tools::add)
        limit = config.getInt("limit", 5)
    }

    @EventHandler
    fun onBlockBroken(e: BlockBreakEvent) {
        val material = e.block.type
    }
}