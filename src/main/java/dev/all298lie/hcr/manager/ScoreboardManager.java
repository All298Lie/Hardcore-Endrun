package dev.all298lie.hcr.manager;

import dev.all298lie.hcr.hcr;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

public class ScoreboardManager {
    private final hcr plugin;

    // 클래스 생성자
    public ScoreboardManager(hcr plugin) {
        this.plugin = plugin;
    }

    // 스코어보드 적용 함수
    public void applyScoreboard(Player player) {
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();

        Objective obj = board.registerNewObjective("hcr_board", Criteria.DUMMY, toComponent("§f[ §c§lHCR §f]"));
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        createTeam(board, "try_count", "§5", 5);
        createTeam(board, "life", "§4", 4);
        createTeam(board, "protection_time", "§3", 3);
        createTeam(board, "world_time", "§2", 2);
        createTeam(board, "total_time", "§1", 1);

        player.setScoreboard(board);
        updateScoreboard(player);
    }

    // 스코어보드 업데이트 함수
    public void updateScoreboard(Player player) {
        Scoreboard board = player.getScoreboard();

        if (board.getObjective("hcr_board") == null) return;

        FileConfiguration data = plugin.getDataConfig();

        int tryCount = data.getInt("try_count", 0);
        int life = data.getInt("life", 1);
        int protectionTime = data.getInt("protection_time", 0);
        int worldTime = data.getInt("world_time", 0);
        int totalTime = data.getInt("total_time", 0);

        updateTeamText(board, "try_count", "§f현재 지구: ", "§e" + tryCount + "지구");
        updateTeamText(board, "life", "§f남은 목숨: ", "§c" + life + "개");

        if (protectionTime > 0) {
            updateTeamText(board, "protection_time", "§f보호 시간: ", "§a" + formatTime(protectionTime));
        } else {
            updateTeamText(board, "protection_time", "§f보호 시간: ", "§c종료됨");
        }

        updateTeamText(board, "world_time", "§f월드 시간: ", "§f" + formatTime(worldTime));
        updateTeamText(board, "total_time", "§f토탈 시간: ", "§f" + formatTime(totalTime));
    }

    // Team 생성 함수
    private void createTeam(Scoreboard board, String teamName, String entry, int score) {
        Team team = board.registerNewTeam(teamName);
        team.addEntry(entry);
        board.getObjective("hcr_board").getScore(entry).setScore(score);
    }

    // Team 텍스트 업데이트 함수
    private void updateTeamText(Scoreboard board, String teamName, String prefix, String suffix) {
        Team team = board.getTeam(teamName);

        if (team != null) {
            team.prefix(toComponent(prefix));
            team.suffix(toComponent(suffix));
        }
    }

    // 시간 포맷 함수
    private String formatTime(int totalSecs) {
        int hours = totalSecs / 3600;
        int minutes = (totalSecs % 3600) / 60;
        int seconds = totalSecs % 60;

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }

    private Component toComponent(String text) {
        return LegacyComponentSerializer.legacySection().deserialize(text);
    }
}
