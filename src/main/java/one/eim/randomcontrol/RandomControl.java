package one.eim.randomcontrol;

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Squid;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;
import xyz.jpenilla.reflectionremapper.ReflectionRemapper;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.logging.Level;

public final class RandomControl extends JavaPlugin implements Listener {
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    private static final ReflectionRemapper REMAPPER = ReflectionRemapper.forReobfMappingsInPaperJar();
    private static final Map<String, Class<?>> classCache = new HashMap<>();
    private static final Class<?> RANDOM_SOURCE = findClass("net.minecraft.util.RandomSource");
    private @Nullable MethodHandle randomSourceCreate;
    private @Nullable MethodHandle randomSourceSetSeed;
    private MethodHandle craftEntityGetHandle;
    private MethodHandle entitySetRandom;
    private MethodHandle entityGetRandom;
    private Object SHARED_RANDOM;

    @Override
    public void onEnable() {
        final Class<?> craftEntity = Objects.requireNonNull(findClass(Bukkit.getServer().getClass().getName().replace("CraftServer", "entity.CraftEntity")), "CraftEntity does not exist? Not CraftBukkit?");
        this.craftEntityGetHandle = findMethodHandle(craftEntity.getName(), "getHandle");

        if (this.craftEntityGetHandle == null) {
            this.getLogger().log(Level.WARNING, "Failed to get MethodHandle for CraftEntity#getHandle");
            this.getServer().getPluginManager().disablePlugin(this);
        }

        if (RANDOM_SOURCE != null) {
            this.randomSourceCreate = findMethodHandle("net.minecraft.util.RandomSource", "create");
            this.randomSourceSetSeed = findMethodHandle("net.minecraft.util.RandomSource", "setSeed", long.class);
        } else {
            this.randomSourceCreate = null;
            this.randomSourceSetSeed = null;
        }

        try {
            final Field entityRandomField = Entity.class.getDeclaredField(REMAPPER.remapFieldName(Entity.class, "random"));
            entityRandomField.setAccessible(true);
            this.entitySetRandom = LOOKUP.unreflectSetter(entityRandomField);
            this.entityGetRandom = LOOKUP.unreflectGetter(entityRandomField);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            this.getLogger().log(Level.WARNING, "Failed to unreflect MethodHandle getter/setter random on Entity", e);
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        try {
            SHARED_RANDOM = Entity.class.getDeclaredField("SHARED_RANDOM").get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            this.getLogger().log(Level.WARNING, "Failed to get SHARED_RANDOM on Entity. Not Paper?", e);
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void on(final EntityAddToWorldEvent event) throws Throwable {
        final Entity entity = (Entity) craftEntityGetHandle.invoke(event.getEntity());

        // only set once per entity - hopefully
        if (entityGetRandom.invoke(entity) != SHARED_RANDOM) {
            return;
        }

        if (randomSourceCreate != null) {
            final Object randomSource = randomSourceCreate.invoke();
            if (randomSourceSetSeed != null && entity instanceof Squid) {
                randomSourceSetSeed.invoke(randomSource, (long) entity.getId());
            }
            entitySetRandom.invoke(entity, randomSource);
            return;
        }

        final Random random = new Random();
        if (entity instanceof Squid) {
            random.setSeed(entity.getId());
        }
        entitySetRandom.invoke(entity, random);
    }

    private static @Nullable Class<?> findClass(final String name) {
        String className = name;
        if (name.startsWith("net.minecraft")) {
            className = REMAPPER.remapClassName(name);
        }

        if (classCache.containsKey(className)) {
            return classCache.get(className);
        }

        try {
            final Class<?> clazz = Class.forName(className);
            classCache.put(className, clazz);
            return clazz;
        } catch (final ClassNotFoundException e) {
            return null;
        }
    }

    private static @Nullable MethodHandle findMethodHandle(final String className, final String methodName, final @Nullable Class<?>... params) {
        final Class<?> clazz = Objects.requireNonNull(findClass(className), "Could not find class " + className);
        try {
            return LOOKUP.unreflect(clazz.getDeclaredMethod(clazz.getPackageName().startsWith("net.minecraft") ? REMAPPER.remapMethodName(clazz, methodName, params) : methodName, params));
        } catch (final NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }
}
