package dev.all298lie.hcr.manager;

import dev.all298lie.hcr.hcr;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

public class WorldManager {
    private final hcr plugin;

    public WorldManager(hcr plugin) {
        this.plugin = plugin;
    }

    // 월드 리셋 함수
    public void resetAndCreateWorld(int newTryCount) {
        FileConfiguration data = plugin.getDataConfig();
        int tryCount = data.getInt("try_count", 0);

        data.set("is_generating", true);
        data.set("joined_players", new ArrayList<String>());

        // 1. 월드 생성 메세지 출력
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.setGameMode(GameMode.SPECTATOR);
            p.showTitle(net.kyori.adventure.title.Title.title(
                    Component.text("§c§l월드 생성 중..."),
                    Component.text("§7 " + newTryCount + "지구를 준비하고 있습니다.")
            ));
        }

        // 월드 생성 스케쥴러 생성
        new BukkitRunnable() {
            @Override
            public void run() {
                String overworldName = "hcr_world_" + newTryCount;
                String netherName = "hcr_nether_" + newTryCount;
                String endName = "hcr_the_end_" + newTryCount;

                boolean lockSeed = plugin.getConfig().getBoolean("lock_seed", false);
                long seedToUse = 0;

                // 1. 월드 생성
                WorldCreator wcOverworld = new WorldCreator(overworldName).environment(World.Environment.NORMAL);
                WorldCreator wcNether = new WorldCreator(netherName).environment(World.Environment.NETHER);
                WorldCreator wcEnd = new WorldCreator(endName).environment(World.Environment.THE_END);

                // 시드 고정 설정이 되어있을 경우
                if (lockSeed) {
                    if (newTryCount == 1) {
                        seedToUse = new java.util.Random().nextLong();
                        data.set("fixed_seed", seedToUse);
                    } else {
                        seedToUse = data.getLong("fixed_seed");
                    }

                    wcOverworld.seed(seedToUse);
                    wcNether.seed(seedToUse);
                    wcEnd.seed(seedToUse);
                }

                plugin.getLogger().info(newTryCount + "지구 오버월드 생성 시작...");
                World newOverworld = Bukkit.createWorld(wcOverworld);
                plugin.getLogger().info(newTryCount + "지구 네더 생성 시작...");
                World newNether = Bukkit.createWorld(wcNether);
                plugin.getLogger().info(newTryCount + "지구 엔드 생성 시작...");
                World newEnd = Bukkit.createWorld(wcEnd);

                // 2. 월드 스폰 지점 확인 및 저장
                Location spawnLoc = newOverworld.getSpawnLocation();
                data.set("spawn_x", spawnLoc.getX());
                data.set("spawn_y", spawnLoc.getY());
                data.set("spawn_z", spawnLoc.getZ());

                // 3. 접속 중인 플레이어 데이터 초기화 및 텔레포트, 스코어보드 갱신
                ArrayList<String> players = new ArrayList<String>();

                for (Player p : Bukkit.getOnlinePlayers()) {
                    resetPlayerData(p);
                    p.teleport(spawnLoc);
                    p.setGameMode(GameMode.SURVIVAL);
                    p.showTitle(net.kyori.adventure.title.Title.title(
                            Component.text("§a§l" + newTryCount + "지구"),
                            Component.text("§f새 월드가 생성되었습니다.")
                    ));

                    players.add(p.getUniqueId().toString());
                    plugin.getScoreboardManager().updateScoreboard(p);
                }

                data.set("joined_players", players);

                // 4. 기록용 파일에 데이터 최신화
                data.set("try_count", newTryCount);
                data.set("life", plugin.getConfig().getInt("limit_count", 1));
                data.set("world_time", 0);
                data.set("protection_time", plugin.getConfig().getInt("protection_time", 0));

                // 5. 데이터 저장 및 생성 완료 설정
                data.set("is_generating", false);
                plugin.saveDataFile();

                // 6. 이전 월드 비활성화
                if (tryCount > 0) {
                    Bukkit.unloadWorld("hcr_world_" + tryCount, false);
                    Bukkit.unloadWorld("hcr_nether_" + tryCount, false);
                    Bukkit.unloadWorld("hcr_the_end_" + tryCount, false);
                    plugin.getLogger().info(tryCount + "지구 월드를 메모리에서 언로드했습니다.");
                }
            }
        }.runTaskLater(plugin, 60L);
    }

    public void removeOldWorlds(int tryCount) {
        FileConfiguration data = plugin.getDataConfig();

        int lastDeleted = data.getInt("last_deleted_try", 0);

        // 지울게 없을 경우, 리턴
        if (lastDeleted >= tryCount - 1) return;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            for (int i = lastDeleted + 1; i < tryCount; i++) {
                deleteFolder(new File(Bukkit.getWorldContainer(), "hcr_world_" + i));
                deleteFolder(new File(Bukkit.getWorldContainer(), "hcr_nether_" + i));
                deleteFolder(new File(Bukkit.getWorldContainer(), "hcr_the_end_" + i));
                plugin.getLogger().info(i + "지구 월드 폴더를 물리적으로 삭제했습니다.");
            }

            // 삭제 후 삭제 번호 업데이트
            Bukkit.getScheduler().runTask(plugin, () -> {
                data.set("last_deleted_try", tryCount - 1);
                plugin.saveDataFile();
            });
        });
    }

    // 플레이어 데이터 초기화 함수
    private void resetPlayerData(Player p) {
        p.getInventory().clear();
        p.getEnderChest().clear();
        p.setHealth(20.0);
        p.setFoodLevel(20);
        p.setSaturation(5.0f);
        p.setExp(0f);
        p.setLevel(0);

        // 포션 초기화
        for (PotionEffect effect : p.getActivePotionEffects()) {
            p.removePotionEffect(effect.getType());
        }

        // 발전과제 초기화
        Iterator<Advancement> it = Bukkit.advancementIterator();
        while (it.hasNext()) {
            AdvancementProgress progress = p.getAdvancementProgress((it.next()));
            for (String criteria : progress.getAwardedCriteria()) {
                progress.revokeCriteria(criteria);
            }
        }
    }

    // 파일(폴더)를 삭제해주는 함수
    private void deleteFolder(File folder) {
        if (folder.exists()) {
            File[] files = folder.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteFolder(file);
                    } else {
                        file.delete();
                    }
                }
            }

            folder.delete();
        }
    }
}
