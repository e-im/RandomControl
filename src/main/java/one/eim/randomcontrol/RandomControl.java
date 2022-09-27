package one.eim.randomcontrol;

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class RandomControl extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        try {
            Class.forName("one.eim.randomcontrol.Compatibility");
        } catch (final ClassNotFoundException | ExceptionInInitializerError e) {
            getLogger().warning("Failed to initialize compatibility for your server");
            getLogger().warning("If you expect this plugin to work on your server please open an issue");
            getLogger().log(Level.WARNING, "Current server implementation: " + getServer().getVersion(), e);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void on(final EntityAddToWorldEvent event) throws Throwable {
        Compatibility.updateRandom(event.getEntity(), event.getEntity().getType() == EntityType.SQUID ? (long) event.getEntity().getEntityId() : null);
    }
}
