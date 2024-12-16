package obieuz.nether_potion;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.*;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public final class Nether_potion extends JavaPlugin implements Listener {
    public final HashMap<UUID, Integer> effectedPlayers = new HashMap<UUID, Integer>();
    public final HashSet<UUID> playersInNether = new HashSet<UUID>();

    public static final Integer DEFAULT_DURATION = 300;

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);

        if (getConfig().contains("players")) {
            for (String key : getConfig().getConfigurationSection("players").getKeys(false)) {
                UUID playerUUID = UUID.fromString(key);
                int duration = getConfig().getInt("players." + key);
                effectedPlayers.put(playerUUID, duration);
            }
        }


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

        if(effectedPlayers.containsKey(player.getUniqueId()))
        {
            player.addPotionEffect(PotionEffectType.WEAVING.createEffect(effectedPlayers.get(player.getUniqueId())*20, 0));
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

        if(consumed_item == Material.MILK_BUCKET)
        {
            effectedPlayers.remove(player.getUniqueId());
            return;
        }

        if(consumed_item != Material.POTION)
        {
            return;
        }

        PotionMeta potionMeta = (PotionMeta) event.getItem().getItemMeta();

        if(potionMeta == null)
        {
            return;
        }

        if(potionMeta.getBasePotionType() != PotionType.WATER)
        {
            return;
        }

        if(potionMeta.getLore() == null || !potionMeta.getLore().contains("Potion from nether"))
        {
            return;
        }

        player.sendMessage(effectedPlayers.getOrDefault(player.getUniqueId(),0).toString());

        int _duration = DEFAULT_DURATION + effectedPlayers.getOrDefault(player.getUniqueId(), 0);

        player.addPotionEffect(PotionEffectType.WEAVING.createEffect(_duration*20, 0));

        effectedPlayers.put(player.getUniqueId(),_duration);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(command.getName().equalsIgnoreCase("nether_potion"))
        {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Only players can use this command.");
                return false;
            }
            Player player = (Player) sender;

            if(!player.isOp())
            {
                sender.sendMessage("Only server operators can call this command");
                return false;
            }

            player.getInventory().addItem(createNetherPotion());

            return true;
        }
        return false;
    }

    private void EffectPlayers()
    {
        Collection<? extends Player> players = Bukkit.getOnlinePlayers();

        for(UUID playerUUID : playersInNether)
        {
            boolean isPlayerInCollection = players.stream()
                    .anyMatch(player -> player.getUniqueId().equals(playerUUID));

            if(!isPlayerInCollection)
            {
                continue;
            }

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

    public ItemStack createNetherPotion()
    {

        ItemStack potion = new ItemStack(Material.POTION);
        PotionMeta potionMeta = (PotionMeta) potion.getItemMeta();

        potionMeta.setBasePotionType(PotionType.WATER);
        potionMeta.setColor(Color.RED);
        potionMeta.setDisplayName("Nether potion");
        potionMeta.setItemName("Nether potion");

        potionMeta.setLore(new ArrayList<String>(Arrays.asList("Potion from nether")));

        potion.setItemMeta(potionMeta);
        return potion;
    }

    @Override
    public void onDisable() {
        for (UUID playerUUID : effectedPlayers.keySet()) {
            getConfig().set("players." + playerUUID.toString(), effectedPlayers.get(playerUUID));
        }
        saveConfig();
    }
}
