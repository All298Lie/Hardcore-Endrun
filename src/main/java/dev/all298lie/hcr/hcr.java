package dev.all298lie.hcr;

import dev.all298lie.hcr.commands.HcrCommand;
import dev.all298lie.hcr.listeners.GameClearListener;
import dev.all298lie.hcr.listeners.PlayerDeathListener;
import dev.all298lie.hcr.listeners.PlayerJoinListener;
import dev.all298lie.hcr.listeners.PortalListener;
import dev.all298lie.hcr.manager.ScoreboardManager;
import dev.all298lie.hcr.manager.WorldManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class hcr extends JavaPlugin {
    private File dataFile;
    private FileConfiguration dataConfig;

    private ScoreboardManager scoreboardManager;
    private WorldManager worldManager;

    private boolean useScoreboard;

    // 플러그인이 켜졌을 경우
    @Override
    public void onEnable() {
        worldManager = new WorldManager(this);

        // 0. 이벤트 리스너 등록
        getServer().getPluginManager().registerEvents(new GameClearListener(this), this);
        getServer().getPluginManager().registerEvents(new PortalListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(this, worldManager), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);

        getCommand("hcr").setExecutor(new HcrCommand(this, worldManager));

        // 1. config.yml 파일 불러오기
        saveDefaultConfig();

        // 2. 기록용 파일 불러오기
        loadDataFile();

        // 3. 전용 스코어보드 설정
        useScoreboard = getConfig().getBoolean("use_scoreboard", true);

        if (useScoreboard) {
            scoreboardManager = new ScoreboardManager(this);

            startSystemTimer();

            getLogger().info("설정이 활성화 되어있으므로, 스코어보드를 사용합니다.");
        }
        else {
            getLogger().info("설정이 비활성화 되어있으므로, 스코어보드를 사용하지 않습니다.");
        }

        // 4. 플러그인이 처음 실행된 것인지 확인
        int tryCount = dataConfig.getInt("try_count", 0);

        if (tryCount > 0) {
            getLogger().info("진행중이던 " + tryCount + "지구를 로드하였습니다.");
        }
        else {
            getLogger().info("하드코어 엔더런을 진행한 기록을 확인하지 못했습니다. /hcr start 명령어 실행 시 하드코어 엔더런을 시작합니다.");
        }

        // 5. 기존에 실패한 월드 삭제
        if (tryCount > 0) {
            worldManager.removeOldWorlds(tryCount);
        }

        getLogger().info("하드코어 엔더런 플러그인이 활성화 되었습니다.");
    }

    // 플러그인이 꺼졌을 경우
    @Override
    public void onDisable() {
        // 1. 끄기 전, 현재 정보를 기록용 파일에 저장
        saveDataFile();
        getLogger().info("하드코어 엔더런 플러그인이 비활성화 되었습니다.");
    }

    // 데이터 파일을 불러오는 함수
    private void loadDataFile() {
        dataFile = new File(getDataFolder(), "data.yml");

        // data.yml 파일이 존재하지 않을 경우, 파일 생성
        if (!dataFile.exists()) {
            dataFile.getParentFile().mkdirs();
            try {
                dataFile.createNewFile();
                getLogger().info("새로운 data.yml 파일을 생성했습니다.");
            } catch (IOException e) {
                getLogger().log(Level.SEVERE, "data.yml 파일 생성 중 오류가 발생했습니다.", e);
            }
        }

        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    private void startSystemTimer() {
        new BukkitRunnable() {
            @Override
            public void run() {
                // 게임을 클리어했거나, 시작되지 않은 경우, 리턴
                if (dataConfig.getBoolean("is_cleared", false)) return;
                if (dataConfig.getBoolean("is_generating", false)) return;
                if (dataConfig.getInt("try_count", 0) < 1) return;

                int protectionTime = dataConfig.getInt("protection_time", 0);
                int worldTime = dataConfig.getInt("world_time", 0);
                int totalTime = dataConfig.getInt("total_time", 0);

                // 1. 보호 시간 1초 차감
                if (protectionTime > 0) {
                    dataConfig.set("protection_time", protectionTime - 1);
                }

                // 2. 진행 시간 1초 증가
                dataConfig.set("world_time", worldTime + 1);
                dataConfig.set("total_time", totalTime + 1);

                // 변경사항이 있다면 모든 온라인 플레이어의 스코어보드 화면 갱신
                for (Player player : Bukkit.getOnlinePlayers()) {
                    scoreboardManager.updateScoreboard(player);
                }
            }
        }.runTaskTimer(this, 20L, 20L);
    }

    // 데이터 파일 Getter 함수
    public FileConfiguration getDataConfig() {
        return this.dataConfig;
    }

    // 데이터 파일 저장 함수
    public void saveDataFile() {
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "data.yml 파일 저장 중 오류가 발생했습니다.", e);
        }
    }

    public boolean useScoreboard() {
        return useScoreboard;
    }

    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }
}