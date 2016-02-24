package com.elmakers.mine.bukkit.magic.listener;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.entity.EntityData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MobController implements Listener {
    private MageController controller;
    private final Map<String, EntityData> mobs = new HashMap<String, EntityData>();
    private final Map<String, EntityData> mobsByName = new HashMap<String, EntityData>();
    
    public MobController(MageController controller) {
        this.controller = controller;
    }
    
    public void load(ConfigurationSection configuration) {
        Set<String> mobKeys = configuration.getKeys(false);
        for (String mobKey : mobKeys) {
            ConfigurationSection mobConfiguration = configuration.getConfigurationSection(mobKey);
            if (!mobConfiguration.getBoolean("enabled", true));
            EntityData mob = new EntityData(controller, mobConfiguration);
            mobs.put(mobKey, mob);
            
            // TODO Remove the name map
            String mobName = mob.getName();
            if (mobName != null && !mobName.isEmpty()) {
                mobsByName.put(mobName, mob);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityTarget(EntityTargetEvent event) {
        // TODO: Don't use metadata!
        if (event.isCancelled() || !event.getEntity().hasMetadata("docile")) {
            return;
        }

        if (event.getReason() == EntityTargetEvent.TargetReason.CLOSEST_PLAYER ) {
            event.setCancelled(true);
        }
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        // TODO: fix all of this, don't reference by names.
        Entity entity = event.getEntity();
        if (!(entity instanceof LivingEntity))
        {
            return;
        }

        LivingEntity died = (LivingEntity)entity;
        String name = died.getCustomName();
        if (name == null || name.isEmpty() || !died.isCustomNameVisible())
        {
            return;
        }

        // TODO Fix this
        EntityData mob = mobsByName.get(name);
        if (mob == null) return;

        mob.modifyDrops(controller, event);

        // Prevent double-deaths .. gg Mojang?
        // Kind of hacky to use this flag for it, but seemed easiest
        died.setCustomNameVisible(false);
    }
    
    public int getCount() {
        return mobs.size();
    }

    public Set<String> getKeys() {
        return mobs.keySet();
    }
    
    public EntityData get(String key) {
        return mobs.get(key);
    }
}