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
                Ikkatuhakai.ikkatuhalais["mine"]!!.load(plugin, "mine")
                Ikkatuhakai.ikkatuhalais["dig"]!!.load(plugin, "dig")
                Ikkatuhakai.ikkatuhalais["cut"]!!.load(plugin, "cut")
            }
            else -> {
                if (sender !is Player) {
                    sender!!.sendMessage("コンソールからは使えません！")
                    return false
                }
                val cmd = if (args.size != 2) "" else args[1]
                when(args[0]){
                    "mine" -> Ikkatuhakai.ikkatuhalais["mine"]!!.command(sender, cmd)
                    "dig" -> Ikkatuhakai.ikkatuhalais["dig"]!!.command(sender, cmd)
                    "cut" -> Ikkatuhakai.ikkatuhalais["cut"]!!.command(sender, cmd)
                    "all" ->{
                        Ikkatuhakai.ikkatuhalais["mine"]!!.command(sender, cmd)
                        Ikkatuhakai.ikkatuhalais["dig"]!!.command(sender, cmd)
                        Ikkatuhakai.ikkatuhalais["cut"]!!.command(sender, cmd)
                    }
                }
                return true
            }
        }

        return false
    }
}