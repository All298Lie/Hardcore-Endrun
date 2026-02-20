package dev.all298lie.hcr.listeners;

import dev.all298lie.hcr.hcr;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerPortalEvent;

public class PortalListener implements Listener {
    private final hcr plugin;

    // 생성자
    public PortalListener(hcr plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerPortal(PlayerPortalEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();

        if (to == null) return;

        String fromWorldName = from.getWorld().getName();

        if (!fromWorldName.startsWith("hcr_")) return;

        int tryCount = plugin.getDataConfig().getInt("try_count", 1);
        World targetWorld = null;

        switch (event.getCause()) {
            case NETHER_PORTAL: // 지옥문
                if (fromWorldName.contains("world")) {
                    targetWorld = Bukkit.getWorld("hcr_nether_" + tryCount);
                } else {
                    targetWorld = Bukkit.getWorld("hcr_world_" + tryCount);
                }
                break;
            case END_PORTAL: // 엔드포탈
                if (fromWorldName.contains("world")) {
                    targetWorld = Bukkit.getWorld("hcr_the_end_" + tryCount);
                } else {
                    targetWorld = Bukkit.getWorld("hcr_world_" + tryCount);

                    double x = plugin.getDataConfig().getDouble("spawn_x");
                    double y = plugin.getDataConfig().getDouble("spawn_y");
                    double z = plugin.getDataConfig().getDouble("spawn_z");
                    to = new Location(targetWorld, x, y, z);
                }
                break;
            default:
                break;
        }

        // 도착월드 조정
        if (targetWorld != null) {
            to.setWorld(targetWorld);
            event.setTo(to);
        }
    }

    @EventHandler
    public void onEntityPortal(EntityPortalEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();

        if (to == null) return;

        String fromWorldName = from.getWorld().getName();

        if (!fromWorldName.startsWith("hcr_")) return;

        int tryCount = plugin.getDataConfig().getInt("try_count", 1);
        World targetWorld = null;

        switch (to.getWorld().getEnvironment()) {
            case NETHER: // 지옥으로 가는 중
                targetWorld = Bukkit.getWorld("hcr_nether_" + tryCount);
                break;
            case THE_END: // 엔드로 가는 중
                targetWorld = Bukkit.getWorld("hcr_the_end_" + tryCount);
                break;
            case NORMAL: // 오버월드로 돌아오는 중
                targetWorld = Bukkit.getWorld("hcr_world_" + tryCount);

                if (from.getWorld().getEnvironment() == World.Environment.THE_END) {
                    double x = plugin.getDataConfig().getDouble("spawn_x");
                    double y = plugin.getDataConfig().getDouble("spawn_y");
                    double z = plugin.getDataConfig().getDouble("spawn_z");
                    to = new Location(targetWorld, x, y, z);
                }
                break;
        }

        // 도착월드 조정
        if (targetWorld != null) {
            to.setWorld(targetWorld);
            event.setTo(to);
        }
    }
}
