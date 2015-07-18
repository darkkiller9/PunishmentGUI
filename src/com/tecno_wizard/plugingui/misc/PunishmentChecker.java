package com.tecno_wizard.plugingui.misc;

import com.tecno_wizard.plugingui.core.Resources;
import com.tecno_wizard.plugingui.data.Infraction;
import com.tecno_wizard.plugingui.data.PlayerFile;
import com.tecno_wizard.plugingui.data.PunishType;
import com.tecno_wizard.plugingui.data.TemporaryPunishType;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Timestamp;

/**
 * Created by Ethan Zeigler on 7/16/2015 for PunismentGUI.
 */
public class PunishmentChecker implements Listener {

    public PunishmentChecker(JavaPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        PlayerFile file = new PlayerFile(player.getUniqueId());

        if(file.isPunishmentActive(PunishType.TEMP_BAN) || file.isPunishmentActive(PunishType.PERM_BAN)) {
            event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
            String reason = null;
            Infraction infraction = null;
            String expirationTime;
            TemporaryPunishType type;

            for (Infraction frac: file.getInfractionHistory()) {
                if(frac.getType().equals(PunishType.TEMP_BAN) || frac.getType().equals(PunishType.PERM_BAN)) {
                    reason = frac.getReason();
                    infraction = frac;
                    break;
                }
            }
            if(infraction == null) {
                event.setKickMessage("You have been banned for an unknown reason and an undeclared time." + Resources.getPunishmentMessageSuffix());
                return;
            }
            try {
                type = TemporaryPunishType.valueOf(infraction.getType().toString());
                expirationTime = new Timestamp(file.getExpirationOfTemporaryPunishment(type)).toString();
                event.setKickMessage(String.format("You are banned until %s for \"%s\". %s", expirationTime, reason, Resources.getPunishmentMessageSuffix()));
            } catch(IllegalArgumentException exception) {
                event.setKickMessage(String.format("You have been permanently banned for %s. %s", reason, Resources.getPunishmentMessageSuffix()));
            }
        }

        MetadataHandler.getInstance().applyNeccesaryData(player);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        MetadataHandler meta = MetadataHandler.getInstance();
        if(meta.isPlayerMuted(e.getPlayer())) {
            e.setCancelled(true);
            if(meta.isMutePermanent(e.getPlayer())) {
                e.getPlayer().sendMessage(ChatColor.RED + "You are permanently muted. " + Resources.getPunishmentMessageSuffix());
            } else {
                e.getPlayer().sendMessage(ChatColor.RED + "You are muted until " + meta.getMuteExpirationAsString(e.getPlayer()));
            }
        }
    }
}
