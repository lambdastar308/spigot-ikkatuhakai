package lambdastar314.ikkatuhakai

import lambdastar314.ikkatuhakai.ikkatu.Ikkatu
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class IkkatuCommand(val plugin: Ikkatuhakai) : CommandExecutor {
    override fun onCommand(
        sender: CommandSender?,
        command: Command?,
        label: String?,
        args: Array<out String>?
    ): Boolean {

        if (args == null || args.isEmpty()) {
            return false
        }
        when (args[0]) {
            "reload" -> {
                Ikkatuhakai.ikkatuhalais["mine"] = Ikkatu(plugin, "mine")
                Ikkatuhakai.ikkatuhalais["dig"] = Ikkatu(plugin, "dig")
                Ikkatuhakai.ikkatuhalais["cut"] = Ikkatu(plugin, "cut")
            }
            else -> {
                if (sender !is Player) {
                    sender!!.sendMessage("コンソールからは使えません！")
                    return false
                }
                if (args.size == 2) {
                    return false
                }
                when(args[0]){
                    "mine" -> Ikkatuhakai.ikkatuhalais["mine"]!!.command(sender as Player, args[1])
                    "dig" -> Ikkatuhakai.ikkatuhalais["dig"]!!.command(sender as Player, args[1])
                    "cut" -> Ikkatuhakai.ikkatuhalais["cut"]!!.command(sender as Player, args[1])
                    "all" ->{
                        Ikkatuhakai.ikkatuhalais["mine"]!!.command(sender as Player, args[1])
                        Ikkatuhakai.ikkatuhalais["dig"]!!.command(sender as Player, args[1])
                        Ikkatuhakai.ikkatuhalais["cut"]!!.command(sender as Player, args[1])
                    }
                }
                return true
            }
        }

        return false
    }
}