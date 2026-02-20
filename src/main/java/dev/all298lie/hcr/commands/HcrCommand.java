package dev.all298lie.hcr.commands;

import dev.all298lie.hcr.hcr;
import dev.all298lie.hcr.manager.WorldManager;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class HcrCommand implements CommandExecutor {
    private final hcr plugin;
    private final WorldManager worldManager;

    public HcrCommand(hcr plugin, WorldManager worldManager) {
        this.plugin = plugin;
        this.worldManager = worldManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // 1. OP 권한 확인
        if (!sender.isOp()) {
            sender.sendMessage(Component.text("§c이 명령어를 사용할 권한이 없습니다."));
            return true;
        }

        // 2. /hcr 명령어를 입력했을 경우
        if (args.length == 0) {
            sender.sendMessage(Component.text("§f/hcr start - 하드코어 엔더런 1지구를 생성하고 시작합니다."));
            return true;
        }

        // 3. /hcr start 명령어를 입력했을 경우
        if (args[0].equalsIgnoreCase("start")) {
            int tryCount = plugin.getDataConfig().getInt("try_count", 0);

            // a. 이미 엔드런이 진행 중일 경우
            if (tryCount > 0) {
                sender.sendMessage(Component.text("§c이미 " + tryCount + "지구가 진행 중 입니다."));
                return true;
            }

            sender.sendMessage(Component.text("§a하드코어 엔더런 1지구 생성을 시작합니다."));

            worldManager.resetAndCreateWorld(1);
            return true;
        }

        return false;
    }
}
