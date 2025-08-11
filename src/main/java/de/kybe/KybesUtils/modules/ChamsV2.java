package de.kybe.KybesUtils.modules;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.sun.jna.platform.win32.OpenGL32;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.rusherhack.client.api.events.render.EventRenderEntity;
import org.rusherhack.client.api.feature.module.ModuleCategory;
import org.rusherhack.client.api.feature.module.ToggleableModule;
import org.rusherhack.client.api.setting.ColorSetting;
import org.rusherhack.core.event.subscribe.Subscribe;
import org.rusherhack.core.setting.BooleanSetting;

import java.awt.*;

public class ChamsV2 extends ToggleableModule {
    public static ChamsV2 INSTANCE;

    public final BooleanSetting players = new BooleanSetting("Players", false);
    public final ColorSetting selfPlayerColor = new ColorSetting("Self Player Color", new Color(0, 0, 0, 255));

    public final BooleanSetting otherPlayers = new BooleanSetting("Other Players", false);
    public final ColorSetting otherPlayerColor = new ColorSetting("Other Player Color", new Color(0, 0, 0, 255));

    public final BooleanSetting mobs = new BooleanSetting("Mobs", false);
    public final ColorSetting mobsColor = new ColorSetting("Mobs Color", new Color(0, 0, 0, 255));

    public final BooleanSetting cape = new BooleanSetting("Cape", false);
    public final ColorSetting selfCapeColor = new ColorSetting("Self Cape Color", new Color(0, 0, 0, 255));

    public final BooleanSetting otherCapes = new BooleanSetting("Other Capes", false);
    public final ColorSetting otherCapeColor = new ColorSetting("Other Cape Color", new Color(0, 0, 0, 255));

    public final BooleanSetting armor = new BooleanSetting("Armor", false);
    public final ColorSetting selfArmorColor = new ColorSetting("Self Armor Color", new Color(0, 0, 0, 255));

    public final BooleanSetting otherArmor = new BooleanSetting("Other Armor", false);
    public final ColorSetting otherArmorColor = new ColorSetting("Other Armor Color", new Color(0, 0, 0, 255));

    public final BooleanSetting mobsArmor = new BooleanSetting("Mobs Armor", false);
    public final ColorSetting mobsArmorColor = new ColorSetting("Mobs Armor Color", new Color(0, 0, 0, 255));

    public final BooleanSetting hands = new BooleanSetting("Hands", false);
    public final ColorSetting handsColor = new ColorSetting("Hands Color", new Color(0, 0, 0, 255));


    public ChamsV2() {
        super("ChamsV2", ModuleCategory.MISC);

        this.registerSettings(
                players, selfPlayerColor,
                otherPlayers, otherPlayerColor,
                mobs, mobsColor,
                cape, selfCapeColor,
                otherCapes, otherCapeColor,
                armor, selfArmorColor,
                otherArmor, otherArmorColor,
                mobsArmor, mobsArmorColor,
                hands, handsColor
        );

        INSTANCE = this;
    }
}