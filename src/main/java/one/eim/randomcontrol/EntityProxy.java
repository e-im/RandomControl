package one.eim.randomcontrol;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import xyz.jpenilla.reflectionremapper.proxy.annotation.FieldGetter;
import xyz.jpenilla.reflectionremapper.proxy.annotation.FieldSetter;
import xyz.jpenilla.reflectionremapper.proxy.annotation.Proxies;

@Proxies(Entity.class)
public interface EntityProxy {
    @FieldSetter("random")
    void setRandom(Entity instance, RandomSource random);

    @FieldGetter("random")
    RandomSource getRandom(Entity instance);
}
