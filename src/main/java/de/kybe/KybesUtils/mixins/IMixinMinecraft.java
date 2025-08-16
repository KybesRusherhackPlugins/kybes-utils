package de.kybe.KybesUtils.mixins;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Minecraft.class)
public interface IMixinMinecraft {
    @Accessor("demo")
    @Mutable
    void kybe$setDemo(boolean demo);
}
