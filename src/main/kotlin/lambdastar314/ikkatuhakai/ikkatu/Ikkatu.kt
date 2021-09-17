package lambdastar314.ikkatuhakai.ikkatu

import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.util.Vector
import java.io.File
import java.lang.reflect.Method
import java.util.*


open class Ikkatu(var plugin: JavaPlugin, var name: String) : Listener {
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
        plugin.server.pluginManager.registerEvents(this, plugin)
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

    fun command(player: Player, s: String){
        when(s){
            "on","enable" ->{
                status[player.name] = true
            }
            "off","disable" ->{
                status[player.name] = false
            }
            "toggle" ->{
                status[player.name] = status[player.name]!!.not()
            }
        }
        player.sendMessage("${ChatColor.BLUE}$name ${if(status[player.name]!!) "${ChatColor.AQUA}ON" else "${ChatColor.GREEN}OFF"}")
    }

    @EventHandler
    fun onBlockBroken(e: BlockBreakEvent) {
        if(status[e.player.name]!!.not()) return
        if (tools.contains(e.player.inventory.itemInMainHand.type.name))
            recursiveBlocks(e.block, e.player.inventory.itemInMainHand, limit, true)
        if (tools.contains(e.player.inventory.itemInOffHand.type.name))
            recursiveBlocks(e.block, e.player.inventory.itemInOffHand, limit, true)

    }

    @EventHandler
    fun onPlayerJoined(e: PlayerJoinEvent) {
        status[e.player.name] = true
    }

    /**
     * 再帰的にブロックを破壊していく
     */
    fun recursiveBlocks(b: Block, holding: ItemStack, limit: Int, ispBlock: Boolean){
        //対象がメインのブロックだった場合
        if (blocks.contains(b.type.name)) {
            breakBlock(b, holding)
            b.type = Material.AIR
            if (limit != 0)
                for (neighbor in neighbors) {
                        recursiveBlocks(
                            b.world.getBlockAt(b.location.add(neighbor)),
                            holding,
                            limit - 1,
                            true
                        )
                }
        }
        //対象が葉など、サブブロックだった場合
        if (leaves.contains(b.type.name)) {
            breakBlock(b, null)
            if (limit != 0)
                for (neighbor in neighbors) {
                        recursiveBlocks(
                            b.world.getBlockAt(b.location.add(neighbor)),
                            holding,
                            if (ispBlock) this.limit else limit - 1,
                            false
                        )
                }
        }
    }

    fun breakBlock(b: Block, holding: ItemStack?){

        try {

            //バージョンによってはないこともある
            val version = "v1_12_R1"
            val loader = ClassLoader.getSystemClassLoader()
            val CCraftBlock = loader.loadClass("org.bukkit.craftbukkit.$version.block.CraftBlock")
            val CCraftWorld = loader.loadClass("org.bukkit.craftbukkit.$version.CraftWorld")
            val CBlock = loader.loadClass("net.minecraft.server.$version.Block")
            val MgetNMSBlock: Method = CCraftBlock.getDeclaredMethod("getNMSBlock")
            val MgetHandle: Method = CCraftWorld.getDeclaredMethod("getHandle")
//            Arrays.stream(CCraftBlock.methods).map{it.name}.filter{it[0]=='g'}.forEach(plugin.logger::info)
            val MgetExpDrop: Method = CCraftBlock.getDeclaredMethod("getExpDrop")
            val MgetBlockData: Method = CCraftBlock.getDeclaredMethod("getBlockData")
            MgetNMSBlock.isAccessible = true
            val nmsBlock = MgetNMSBlock.invoke(b)
            val handler = MgetHandle.invoke(CCraftWorld.cast(b.world))
            val blockData = MgetBlockData.invoke(nmsBlock)

            val bonusLevel = holding?.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS) ?: 0

            MgetExpDrop.invoke(nmsBlock, handler, blockData, bonusLevel)
        }catch(e:Exception) {
            plugin.logger.info("failed to calculate experience. ${e.javaClass.name}:${e.message}")
        }

        b.breakNaturally(holding)
    }
}