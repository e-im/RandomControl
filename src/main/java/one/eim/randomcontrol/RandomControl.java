package one.eim.randomcontrol;

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Squid;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.jpenilla.reflectionremapper.ReflectionRemapper;
import xyz.jpenilla.reflectionremapper.proxy.ReflectionProxyFactory;

public final class RandomControl extends JavaPlugin implements Listener {
    EntityProxy entityProxy;

    @Override
    public void onEnable() {
        final ReflectionRemapper remapper = ReflectionRemapper.forReobfMappingsInPaperJar();
        final ReflectionProxyFactory proxyFactory = ReflectionProxyFactory.create(remapper, this.getClassLoader());
        this.entityProxy = proxyFactory.reflectionProxy(EntityProxy.class);

        this.getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void on(final EntityAddToWorldEvent event) {
        final Entity entity = ((CraftEntity) event.getEntity()).getHandle();

        if (this.entityProxy.getRandom(entity) != Entity.SHARED_RANDOM) {
            return;
        }

        final RandomSource random = RandomSource.create();
        this.entityProxy.setRandom(entity, random);
        if (entity instanceof Squid) {
            random.setSeed(entity.getId());
        }
    }
}
