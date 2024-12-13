package obieuz.nether_potion;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionBrewer;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionEffectTypeWrapper;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public final class Nether_potion extends JavaPlugin implements Listener {
    public final HashMap<UUID, Integer> effectedPlayers = new HashMap<UUID, Integer>();
    public final HashSet<UUID> playersInNether = new HashSet<UUID>();

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);

        new BukkitRunnable(){
            @Override
            public void run(){
                EffectPlayers();
            }
        }.runTaskTimer(this, 0, 20);

    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (player.getWorld().getName().equals("world_nether")) {
            playersInNether.add(player.getUniqueId());
        }
    }


    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {

        if (event.getPlayer().getWorld().getName().equals("world_nether")) {
            playersInNether.add(event.getPlayer().getUniqueId());
        } else {
            playersInNether.remove(event.getPlayer().getUniqueId());
        }
    }
    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event)
    {
        Material consumed_item = event.getItem().getType();
        Player player = event.getPlayer();

        if(consumed_item != Material.POTION)
        {
            return;
        }

        PotionMeta potionMeta = (PotionMeta) event.getItem().getItemMeta();

        if(potionMeta.getBasePotionType() != PotionType.WATER)
        {
            return;
        }

        int duration = 120 + effectedPlayers.getOrDefault(player.getUniqueId(), 0);

        player.addPotionEffect(PotionEffectType.WEAVING.createEffect(duration*20, 0));
        effectedPlayers.put(player.getUniqueId(),duration + effectedPlayers.getOrDefault(player.getUniqueId(), 0));
    }

    private void EffectPlayers()
    {
        for(UUID playerUUID : playersInNether)
        {
            if(effectedPlayers.containsKey(playerUUID))
            {
                effectedPlayers.put(playerUUID, effectedPlayers.get(playerUUID) - 1);

                if(effectedPlayers.get(playerUUID) > 0)
                {
                    continue;
                }

                effectedPlayers.remove(playerUUID);
            }

            Player player = getServer().getPlayer(playerUUID);

            player.addPotionEffect(PotionEffectType.INSTANT_DAMAGE.createEffect(1, 0));


        }
    }

    @Override
    public void onDisable() {
        //zapisuje dane
        // Plugin shutdown logic
    }
}
