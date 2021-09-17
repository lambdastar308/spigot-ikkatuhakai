package lambdastar314.ikkatuhakai.ikkatu

import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Item
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.util.Vector
import java.io.File
import java.util.*


open class Ikkatu(plugin: JavaPlugin, name: String) : Listener {
    var status = Collections.synchronizedMap(HashMap<String, Boolean>())
    var leaves = Collections.synchronizedSet(HashSet<String>())
    var blocks = Collections.synchronizedSet(HashSet<String>())
    var tools = Collections.synchronizedSet(HashSet<String>())
    var limit: Int = 10

    companion object {
        val neighbors = arrayOf(
            Vector(0, 0, -1), Vector(0, 0, 1),
            Vector(0, -1, 0), Vector(0, 1, 0),
            Vector(-1, 0, 0), Vector(1, 0, 0)
        )
    }

    init {
        load(plugin, name)
        plugin.pluginLoader.createRegisteredListeners(this, plugin)
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
        val drop = LinkedList<ItemStack>()
        if (tools.contains(e.player.inventory.itemInMainHand.type.name))
            drop.addAll(recursiveBlocks(e.block, e.player.inventory.itemInMainHand, limit, true))
        if (tools.contains(e.player.inventory.itemInOffHand.type.name))
            drop.addAll(recursiveBlocks(e.block, e.player.inventory.itemInOffHand, limit, true))
        for (i in drop) {
            var eitem = e.block.world.spawn(e.block.location, Item::class.java)
            eitem.itemStack = i
            val r = Random()
            eitem.velocity = Vector(r.nextFloat(), r.nextFloat(), r.nextFloat()).normalize()
        }
    }

    fun recursiveBlocks(b: Block, holding: ItemStack, limit: Int, ispBlock: Boolean): Collection<ItemStack> {
        if (blocks.contains(b.type.name)) {
            val drops = b.getDrops(holding)
            b.type = Material.AIR
            if (limit != 0)
                for (neighbor in neighbors) {
                    drops.addAll(
                        recursiveBlocks(
                            b.world.getBlockAt(b.location.add(neighbor)),
                            holding,
                            limit - 1,
                            true
                        )
                    )
                }
            return drops
        }
        if (leaves.contains(b.type.name)) {
            val drops = b.drops
            b.type = Material.AIR
            if (limit != 0)
                for (neighbor in neighbors) {
                    drops.addAll(
                        recursiveBlocks(
                            b.world.getBlockAt(b.location.add(neighbor)),
                            holding,
                            if (ispBlock) this.limit else limit - 1,
                            false
                        )
                    )
                }
            return drops
        }
        return Collections.emptyList()
    }
}