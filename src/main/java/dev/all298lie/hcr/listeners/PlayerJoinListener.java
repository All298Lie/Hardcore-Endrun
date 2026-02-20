package dev.all298lie.hcr.listeners;

import dev.all298lie.hcr.hcr;
import dev.all298lie.hcr.utils.PlayerUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.List;

public class PlayerJoinListener implements Listener {
    private final hcr plugin;

    // 생성자
    public PlayerJoinListener(hcr plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        FileConfiguration data = plugin.getDataConfig();

        // 1. 엔더런이 진행 중인지 확인
        int tryCount = data.getInt("try_count", 0);
        if (tryCount == 0) return;

        // 2. 스코어보드 설정이 되어있는지 확인
        if (plugin.getConfig().getBoolean("use_scoreboard", true)) {
            plugin.getScoreboardManager().applyScoreboard(player);
        }

        List<String> players = data.getStringList("joined_players");
        String playerUUID = player.getUniqueId().toString();

        // 3. 현재 지구에 플레이어가 접속한 기록이 있는지 확인
        if (!players.contains(playerUUID)) {
            player.setGameMode(GameMode.SPECTATOR);

            PlayerUtil.resetPlayerData(player);
            teleportToSpawn(player, tryCount);

            player.setGameMode(GameMode.SURVIVAL);

            players.add(playerUUID);
            data.set("joined_players", players);
            plugin.saveDataFile();

        } else {
            String worldName = player.getWorld().getName();

            if (!worldName.startsWith("hcr_") || !worldName.endsWith(String.valueOf(tryCount))) {
                teleportToSpawn(player, tryCount);
                player.sendMessage("§f잘못된 세계에 있어 " + tryCount + "지구 스폰으로 이동되었습니다.");
            }
        }
    }

    // 스폰 지점으로 이동시키는 함수
    private void teleportToSpawn(Player player, int tryCount) {
        FileConfiguration data = plugin.getDataConfig();

        double x = data.getDouble("spawn_x");
        double y = data.getDouble("spawn_y");
        double z = data.getDouble("spawn_z");

        World world = Bukkit.getWorld("hcr_world_" + tryCount);
        if (world != null) {
            player.teleport(new Location(world, x, y, z));
        }
    }
}
