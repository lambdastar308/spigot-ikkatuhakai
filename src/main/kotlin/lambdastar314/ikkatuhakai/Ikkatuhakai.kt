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
        getCommand("ikkatu").executor = IkkatuCommand(this)

        ikkatuhalais["mine"] = Ikkatu(this, "mine")
        ikkatuhalais["dig"] = Ikkatu(this, "dig")
        ikkatuhalais["cut"] = Ikkatu(this, "cut")

        try {

            //バージョンによってはないこともある
            val version = "v1_12_R1"
            val loader = ClassLoader.getSystemClassLoader()
            val CCraftBlock = loader.loadClass("org.bukkit.craftbukkit.$version.block.CraftBlock")
            val CCraftWorld = loader.loadClass("org.bukkit.craftbukkit.$version.CraftWorld")
            val CBlock = loader.loadClass("net.minecraft.server.$version.Block")
            val MgetNMSBlock: Method = CCraftBlock.getDeclaredMethod("getNMSBlock")
            val MgetHandle: Method = CCraftWorld.getDeclaredMethod("getHandle")
            val MgetExpDrop: Method = CCraftBlock.getDeclaredMethod("getExpDrop")
            val MgetBlockData: Method = CCraftBlock.getDeclaredMethod("getBlockData")
        }catch(e: Exception){
            logger.warning("failed to calculate experience. ${e.javaClass.name}:${e.message}")
            logger.warning("So, The experience orb won't be dropped in this server.")
        }
    }

    override fun onDisable() {
        // Plugin shutdown logic
        logger.info("Plugin shutdown")
    }
}