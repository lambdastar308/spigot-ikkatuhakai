package lambdastar314.ikkatuhakai

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class IkkatuCommand : CommandExecutor {
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

            }
            else -> {
                if (sender !is Player) {
                    sender!!.sendMessage("コンソールからは使えません！")
                    return false
                }
            }
        }

        return false
    }
}