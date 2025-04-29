package dev.frogmilk;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Frog;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class FrogMilk extends JavaPlugin implements Listener {

	private int glowingDuration;
	private int waterBreathingDuration;
	private int nightVisionDuration;

    private NamespacedKey frogMilkKey;

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        frogMilkKey = new NamespacedKey(this, "frog_milk");
        getLogger().info("FrogMilk plugin enabled!");
		saveDefaultConfig();
		glowingDuration = getConfig().getInt("frogmilk.glowing-duration", 30);
		waterBreathingDuration = getConfig().getInt("frogmilk.water-breathing-duration", 120);
		nightVisionDuration = getConfig().getInt("frogmilk.night-vision-duration", 120);
		Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        getLogger().info("FrogMilk plugin disabled!");
    }

    @EventHandler
    public void onFrogRightClick(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return; // Only main hand

        if (event.getRightClicked().getType() == EntityType.FROG) {
            Player player = event.getPlayer();
            ItemStack item = player.getInventory().getItemInMainHand();

            if (item.getType() == Material.BUCKET) {
                // remove one bucket
                item.setAmount(item.getAmount() - 1);
                //give Frog Milk, the milkiest of milks
                ItemStack frogMilk = createFrogMilk();
                player.getInventory().addItem(frogMilk);
                player.playSound(player.getLocation(), Sound.ENTITY_COW_MILK, 1.0f, 1.0f);
				// cancel default interaction
                event.setCancelled(true); 
            }
        }
    }

    private ItemStack createFrogMilk() {
        ItemStack milk = new ItemStack(Material.MILK_BUCKET);
        ItemMeta meta = milk.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§bFrog Milk");
            meta.setLore(List.of(
                "§7Freshly squeezed from a frog!",
                "§7Drink to hop into new powers."
            ));
            meta.getPersistentDataContainer().set(frogMilkKey, PersistentDataType.BYTE, (byte) 1);
            milk.setItemMeta(meta);
        }
        return milk;
    }

	@EventHandler
	public void onDrinkFrogMilk(PlayerItemConsumeEvent event) {
		Player player = event.getPlayer();
		ItemStack consumed = event.getItem();
	
		if (consumed.getType() == Material.MILK_BUCKET) {
			ItemMeta meta = consumed.getItemMeta();
			if (meta != null && meta.getPersistentDataContainer().has(frogMilkKey, PersistentDataType.BYTE)) {
				// delay by 1 tick since milk buckets clear effects
				Bukkit.getScheduler().runTask(this, () -> {
					// seconds * 20 since theres 20 ticks in a second, you can adjust the potion effects here
					player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, glowingDuration * 20, 0));
					player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, waterBreathingDuration * 20, 0));
					player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, nightVisionDuration * 20, 0));
				});
			}
		}
	}
}
