package dev.all298lie.hcr.listeners;

import dev.all298lie.hcr.hcr;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class GameClearListener implements Listener {
    private final hcr plugin;

    // 생성자
    public GameClearListener(hcr plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDeathEvent(EntityDeathEvent event) {
        // 1. 엔더드래곤이 죽은게 아닐 경우, 리턴
        if (!(event.getEntity() instanceof EnderDragon)) return;

        // 2. 엔더드래곤을 잡은 월드 확인
        String worldName = event.getEntity().getWorld().getName();
        if (!worldName.startsWith("hcr_the_end_")) return;

        // 3. 게임 클리어 처리
        plugin.getDataConfig().set("is_cleared", true);
        plugin.saveDataFile();

        // 4. 모든 플레이어에게 클리어 연출
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.setGameMode(GameMode.SPECTATOR);
            p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
            p.showTitle(net.kyori.adventure.title.Title.title(
                    Component.text("§a§lGAME CLEAR!"),
                    Component.text("§f하드코어 엔더런을 클리어했습니다!")
            ));
        }

        Bukkit.getLogger().info(plugin.getDataConfig().getInt("try_count") + "지구에서 엔더드래곤을 처치하였습니다.");
    }
}
