package obieuz.nether_potion;

import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
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
    private final HashMap<UUID, Integer> effectedPlayers = new HashMap<UUID, Integer>();
    private final HashSet<UUID> playersInNether = new HashSet<UUID>();

    @Override
    public void onEnable() {
        new BukkitRunnable(){
            @Override
            public void run(){
                EffectPlayers();
            }
        }.runTaskTimer(this, 0, 20);

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

        player.sendMessage("pijesz "+consumed_item);

        if(consumed_item != Material.POTION)
        {
            player.sendMessage("to nie jest potion");
            return;
        }

        PotionMeta potionMeta = (PotionMeta) event.getItem().getItemMeta();

        //dodac metadane do sprwadzania
        if(potionMeta.getBasePotionType() != PotionType.WATER)
        {
            player.sendMessage("to nie jest woda");
            return;
        }

        //in seconds, later get from meta data of the potion
        int duration = 120;

        player.addPotionEffect(PotionEffectType.WEAVING.createEffect(duration*20, 0));
        effectedPlayers.put(player.getUniqueId(), duration);
        player.sendMessage("powinno dzialac");
    }

    private void EffectPlayers()
    {
        for(UUID playerUUID : effectedPlayers.keySet())
        {
            effectedPlayers.put(playerUUID, effectedPlayers.get(playerUUID) - 1);

            if(!playersInNether.contains(playerUUID))
            {
                continue;
            }

            if(effectedPlayers.get(playerUUID) > 0)
            {
                continue;
            }
            effectedPlayers.remove(playerUUID);

            Player player = getServer().getPlayer(playerUUID);
            player.addPotionEffect(PotionEffectType.INSTANT_DAMAGE.createEffect(1, 0));


        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
