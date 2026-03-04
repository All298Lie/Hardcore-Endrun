package dev.all298lie.hcr.listeners;

import dev.all298lie.hcr.hcr;
import dev.all298lie.hcr.manager.WorldManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerDeathListener implements Listener {
    private final hcr plugin;
    private final WorldManager worldManager;

    // 생성자
    public PlayerDeathListener(hcr plugin, WorldManager worldManager) {
        this.plugin = plugin;
        this.worldManager = worldManager;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        event.setShowDeathMessages(false);

        // 1. 월드 생성 이전이거나 클리어한 이후일 경우, 리턴
        if (plugin.getDataConfig().getBoolean("is_generating", false)) return;
        if (plugin.getDataConfig().getBoolean("is_cleared", false)) return;
        if (!plugin.getDataConfig().getBoolean("is_started", false)) return;

        // 2. 보호 시간이 남아있을 경우, 무시
        int protectionTime = plugin.getDataConfig().getInt("protection_time", 0);
        if (protectionTime > 0) {
            player.sendMessage(Component.text("§f[§a안내§f] 초반 보호 시간 중이므로 목숨이 깎이지 않았습니다!"));

            // 플레이어가 리스폰하지 않았다면 강제 리스폰
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (player.isDead()) {
                    player.spigot().respawn();
                }
            }, 1L);
            return;
        }

        // 3. 스코어보드 데이터에서 현재 목숨과 트라이 횟수 가져오기
        int life = plugin.getDataConfig().getInt("life", 1);
        int tryCount = plugin.getDataConfig().getInt("try_count", 1);

        life = Math.max(life - 1, 0);
        plugin.getDataConfig().set("life", life);
        plugin.saveDataFile();

        boolean showDeathLog = plugin.getConfig().getBoolean("show_death_log", false);

        String nickname = showDeathLog ? player.getName() : "누군가";

        if (life > 0) { // 목숨이 1이상일 경우
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.playSound(p.getLocation(), Sound.ENTITY_WITHER_HURT, 1.0f, 1.0f);
                p.sendMessage(Component.text("§c[!] §e" + nickname + "§f이(가) 사망했습니다! 남은 목숨: §c" + life + "개"));
            }
        } else { // 목숨이 0일 경우
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_DESTROY, 1.0f, 1.0f);
                p.showTitle(net.kyori.adventure.title.Title.title(
                        Component.text("§c§l지구 멸망"),
                        Component.text("§f" + nickname + "의 사망으로 목숨을 모두 소진했습니다...")
                ));
            }

            worldManager.resetAndCreateWorld(tryCount + 1);
        }

        // 플레이어가 리스폰하지 않았다면 강제 리스폰
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isDead()) {
                player.spigot().respawn();
            }
        }, 1L);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        int tryCount = plugin.getDataConfig().getInt("try_count");

        if (tryCount > 0) {
            World world = Bukkit.getWorld("hcr_world_" + tryCount);

            if (event.getRespawnLocation().getWorld().getName().equals("hcr_world_" + tryCount)) return;

            if (world != null) {
                double x = plugin.getDataConfig().getDouble("spawn_x");
                double y = plugin.getDataConfig().getDouble("spawn_y");
                double z = plugin.getDataConfig().getDouble("spawn_z");

                event.setRespawnLocation(new Location(world, x, y, z));
            }
        }
    }
}
