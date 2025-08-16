package de.kybe.KybesUtils.mixins;

import net.minecraft.client.GuiMessage;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(ChatComponent.class)
public interface IMixinChatComponent {
    @Invoker("screenToChatX")
    double kybe$screenToChatX(double mouseX);

    @Invoker("screenToChatY")
    double kybe$screenToChatY(double mouseY);

    @Invoker("getMessageLineIndexAt")
    int kybe$getMessageLineIndexAt(double mouseX, double mouseY);

    @Accessor("trimmedMessages")
    List<GuiMessage.Line> kybe$getTrimmedMessages();

    @Accessor("allMessages")
    List<GuiMessage> kybe$getAllMessages();
}
