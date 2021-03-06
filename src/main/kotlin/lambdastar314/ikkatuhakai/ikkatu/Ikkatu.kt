package lambdastar314.ikkatuhakai.ikkatu

import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Entity
import org.bukkit.entity.ExperienceOrb
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.util.Vector
import java.io.File
import java.lang.Integer.min
import java.lang.reflect.Method
import java.util.*


open class Ikkatu(var plugin: JavaPlugin, var name: String) : Listener {
    var status = Collections.synchronizedMap(HashMap<String, Boolean>())
    var leaves = Collections.synchronizedSet(HashSet<String>())
    var blocks = Collections.synchronizedSet(HashSet<String>())
    var tools = Collections.synchronizedSet(HashSet<String>())
    var statusdef = true
    var limit: Int = 10
    var version = "v1_12_R1"

    companion object {
        val neighbors = HashSet<Vector>()
        init {
            for(x in -1..1)
                for(y in -1..1)
                    for(z in -1..1)
                        if(!(x == 0 && y == 0 && z == 0))
                        neighbors.add(Vector(x, y, z))
        }
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
        statusdef = config.getBoolean("defaultstatus", false)
        version = config.getString("version", "v1_12_R1")!!
    }

    fun command(player: Player, s: String) {
        when (s) {
            "on", "enable" -> {
                status[player.name] = true
            }
            "off", "disable" -> {
                status[player.name] = false
            }
            "toggle" -> {
                status[player.name] = status[player.name]!!.not()
            }
        }
        player.sendMessage("${ChatColor.BLUE}$name ${if (status[player.name]!!) "${ChatColor.AQUA}ON" else "${ChatColor.GREEN}OFF"}")
    }

    @EventHandler
    fun onBlockBroken(e: BlockBreakEvent) {
        if (status[e.player.name] != true) return
        if (tools.contains(e.player.inventory.itemInMainHand.type.key.toString()))
            recursiveBlocks(e.block, e.player.inventory.itemInMainHand)
        if (tools.contains(e.player.inventory.itemInOffHand.type.key.toString()))
            recursiveBlocks(e.block, e.player.inventory.itemInOffHand)
    }

    @EventHandler
    fun onPlayerJoined(e: PlayerJoinEvent) {
        status[e.player.name] = statusdef
    }

    fun recursiveBlocks(b: Block, holding: ItemStack) {
        if (blocks.contains(b.type.key.toString()).not()) return
        val type = b.type
        breakBlock(b, holding)
        for (neighbor in neighbors) {
            val target = b.world.getBlockAt(b.location.add(neighbor))
            if (target.type == type)
                recursiveBBlocks(
                    target,
                    holding,
                    limit - 1,
                    type
                )
            else if (leaves.contains(target.type.key.toString()))
                recursiveLeaves(target, this.limit)

        }
    }

    fun recursiveBBlocks(b: Block, holding: ItemStack, limit: Int, parentType: Material) {
        if (b.type != parentType) return
        breakBlock(b, holding)
        if (limit != 0)
            for (neighbor in neighbors) {
                val target = b.world.getBlockAt(b.location.add(neighbor))
                if (target.type == parentType)
                    recursiveBBlocks(
                        target,
                        holding,
                        limit - 1,
                        parentType
                    )
                else if (leaves.contains(target.type.key.toString()))
                    recursiveLeaves(target, this.limit)
            }
    }

    fun recursiveLeaves(b: Block, limit: Int) {
        breakBlock(b, null)
        if (limit != 0)
            for (neighbor in neighbors) {
                val target = b.world.getBlockAt(b.location.add(neighbor))
                if (leaves.contains(target.type.key.toString()))
                    recursiveLeaves(
                        target,
                        limit - 1
                    )
            }
    }

    fun breakBlock(b: Block, holding: ItemStack?) {

        try {
            //?????????????????????????????????????????????????????????
            val version = "v1_12_R1"
            //????????????????????????
//            val loader = ClassLoader.getSystemClassLoader()
            val loader = b.javaClass.classLoader
            val CCraftBlock = loader.loadClass("org.bukkit.craftbukkit.$version.block.CraftBlock")
            val CCraftWorld = loader.loadClass("org.bukkit.craftbukkit.$version.CraftWorld")
            var CWorld = loader.loadClass("net.minecraft.server.World")
            var CBlock = loader.loadClass("net.minecraft.server.Block")
//            Arrays.stream(CCraftBlock.methods).map{it.name}.filter{it[0]=='g'}.forEach(plugin.logger::info)
            //???????????????????????????
            val MgetNMSBlock: Method = CCraftBlock.getDeclaredMethod("getNMSBlock")
            //getNMSBlock()????????????????????????????????????
            MgetNMSBlock.isAccessible = true
            CBlock = MgetNMSBlock.returnType
            CWorld = loader.loadClass(CBlock.`package`.name+".World")
            val MgetHandle: Method = CCraftWorld.getMethod("getHandle")
            val MgetBlockData: Method = CBlock.getMethod("getBlockData")
            val MgetExpDrop: Method = CBlock.getMethod("getExpDrop", CWorld, MgetBlockData.returnType, Int::class.java)

            val nmsBlock = MgetNMSBlock.invoke(b)
            val handler = MgetHandle.invoke(CCraftWorld.cast(b.world))
            val blockData = MgetBlockData.invoke(nmsBlock)

            val bonusLevel = holding?.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS) ?: 0

            val exp = MgetExpDrop.invoke(nmsBlock, handler, blockData, bonusLevel) as Int
            dropExp(exp, b.world, b.location)
        } catch (e: Exception) {
            plugin.logger.info("failed to calculate experience. ${e.javaClass.name}:${e.message}")
        }

        b.breakNaturally(holding)
    }

    fun dropExp(value: Int, world: World, pos: Location) {
        var e = value
        val r = Random()
        while (0 < e) {
            val drop = min(r.nextInt(1) + 1, e)
            e -= drop
            val exp = world.spawn(pos, ExperienceOrb::class.java)
            exp.experience = drop
            entityVelocity(exp, r)
        }
    }

    fun entityVelocity(entity: Entity, r: Random) {
        entity.velocity = Vector(r.nextFloat(), r.nextFloat(), r.nextFloat()).normalize().multiply(0.5)
    }
}
