package dev.all298lie.hcr.utils;

import org.bukkit.Bukkit;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import java.util.Iterator;

public class PlayerUtil {
    public static void resetPlayerData(Player p) {
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
}
