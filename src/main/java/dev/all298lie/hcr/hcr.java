package dev.all298lie.hcr;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class hcr extends JavaPlugin {

    private File dataFile;
    private FileConfiguration dataConfig;

    // 플러그인이 켜졌을 경우
    @Override
    public void onEnable() {
        // 1. config.yml 파일 인식
        saveDefaultConfig();

        int limit_death = getConfig().getInt("limit_death", 1);
        boolean useScoreboard = getConfig().getBoolean("use_scoreboard", true);
        getLogger().info("목숨 제한 설정 : " + limit_death + "개");

        createDataFile();

        int currentTry = dataConfig.getInt("try_count", 1);
        getLogger().info("진행중이던 " + currentTry + "지구를 로드하였습니다.");
    }

    // 플러그인이 꺼졌을 경우
    @Override
    public void onDisable() {
        // 1. 끄기 전, 현재 정보를 기록용 파일에 저장
    }

    // 데이터 파일 생성 함수
    private void createDataFile() {

    }

    // 데이터 파일 Getter 함수
    public FileConfiguration getDataConfig() {
        return this.dataConfig;
    }

    // 데이터 파일 저장 함수
    public void saveDataFile() {

    }
}
