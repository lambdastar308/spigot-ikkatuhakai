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
        statusdef = config.getBoolean("defaultstatus", false)
        version = config.getString("version","v1_12_R1")
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
        if(e.player == null) return
        if(e.block == null) return
        if(e.player.name == null) return
        
        if(status[e.player.name]?.not() ?: true) return
        if (tools.contains(e.player.inventory.itemInMainHand.type.name))
            recursiveBlocks(e.block, e.player.inventory.itemInMainHand, limit, true)
        if (tools.contains(e.player.inventory.itemInOffHand.type.name))
            recursiveBlocks(e.block, e.player.inventory.itemInOffHand, limit, true)
    }

    @EventHandler
    fun onPlayerJoined(e: PlayerJoinEvent) {
        status[e.player.name] = statusdef
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
            //ライブラリ追加するよりこっちのほうが楽
             val version = "v1_12_R1"
            //クラスを読み込む
//            val loader = ClassLoader.getSystemClassLoader()
            val loader = b.javaClass.classLoader
            val CCraftBlock = loader.loadClass("org.bukkit.craftbukkit.$version.block.CraftBlock")
            val CCraftWorld = loader.loadClass("org.bukkit.craftbukkit.$version.CraftWorld")
            val CWorld = loader.loadClass("net.minecraft.server.$version.World")
            val CBlock = loader.loadClass("net.minecraft.server.$version.Block")
//            Arrays.stream(CCraftBlock.methods).map{it.name}.filter{it[0]=='g'}.forEach(plugin.logger::info)
            //メゾットを読み込む
            val MgetNMSBlock: Method = CCraftBlock.getDeclaredMethod("getNMSBlock")
            val MgetHandle: Method = CCraftWorld.getMethod("getHandle")
            val MgetBlockData: Method = CBlock.getMethod("getBlockData")
            val MgetExpDrop: Method = CBlock.getMethod("getExpDrop", CWorld, MgetBlockData.returnType, Int::class.java)
            //getNMSBlock()が隠れてるので露わにする
            MgetNMSBlock.isAccessible = true
            //デバッグ 本当に一緒？？？？
//            plugin.logger.info("b: ${b.javaClass.name} + hash=${b.javaClass.hashCode()}") // ブロックの型
//            plugin.logger.info("getNMSBlock(): ${MgetNMSBlock.declaringClass.name} + hash=${MgetNMSBlock.declaringClass.hashCode()}") // getNMSBlockの元クラスの型
//            plugin.logger.info("b instanceOf ${MgetNMSBlock.declaringClass.name} = ${MgetNMSBlock.declaringClass.isInstance(b)}") // 型が一緒か
//            plugin.logger.info("b class == getNMSBlock Return = ${b.javaClass == MgetNMSBlock.declaringClass}") // 本当に一緒か？？？？

            val nmsBlock = MgetNMSBlock.invoke(b) //ここでエラーが出る
            val handler = MgetHandle.invoke(CCraftWorld.cast(b.world))
            val blockData = MgetBlockData.invoke(nmsBlock)

            val bonusLevel = holding?.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS) ?: 0

            val exp = MgetExpDrop.invoke(nmsBlock, handler, blockData, bonusLevel) as Int
            dropExp(exp, b.world, b.location)
        }catch(e:Exception) {
            plugin.logger.info("failed to calculate experience. ${e.javaClass.name}:${e.message}")
            e.printStackTrace()
        }

        b.breakNaturally(holding)
    }

    fun dropExp(value: Int, world: World, pos: Location){
        var e = value
        val r = Random()
        while(0 < e){
            val drop = min(r.nextInt(1)+1,e)
            e -= drop
            val exp = world.spawn(pos, ExperienceOrb::class.java)
            exp.experience = drop
            entityVelocity(exp, r)
        }
    }

    fun entityVelocity(entity: Entity, r: Random){
        entity.velocity = Vector(r.nextFloat(), r.nextFloat(),r.nextFloat()).normalize().multiply(0.5)
    }
}
