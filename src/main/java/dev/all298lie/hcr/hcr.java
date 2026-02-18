package dev.all298lie.hcr;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class hcr extends JavaPlugin {

    private File dataFile;
    private FileConfiguration dataConfig;

    // 플러그인이 켜졌을 경우
    @Override
    public void onEnable() {
        // 1. config.yml 파일 불러오기
        saveDefaultConfig();

        // 2. 기록용 파일 불러오기
        loadDataFile();

        // 3. 전용 스코어보드 설정

        // 4. 플러그인이 처음 실행된 것인지 확인
        int currentTry = dataConfig.getInt("try_count", 0);

        if (currentTry > 0) {
            getLogger().info("진행중이던 " + currentTry + "지구를 로드하였습니다.");
        }
        else {
            getLogger().info("하드코어 엔더런을 진행한 기록을 확인하지 못했습니다. /hcr start 명령어 실행 시 하드코어 엔더런을 시작합니다.");
        }

        // 5. 기존에 실패한 월드 삭제

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
}