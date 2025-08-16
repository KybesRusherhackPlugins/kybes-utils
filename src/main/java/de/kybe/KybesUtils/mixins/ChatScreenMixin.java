package de.kybe.KybesUtils.mixins;

import de.kybe.KybesUtils.modules.ChatCopyModule;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.screens.ChatScreen;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static org.rusherhack.client.api.Globals.mc;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin {
    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void mouseClicked$Head(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (button != GLFW.GLFW_MOUSE_BUTTON_2 || !ChatCopyModule.INSTANCE.isToggled()) return;

        ChatComponent chatComponent = mc.gui.getChat();
        IMixinChatComponent iMixinChatComponent = (IMixinChatComponent) chatComponent;
        double d = iMixinChatComponent.kybe$screenToChatX(mouseX);
        double e = iMixinChatComponent.kybe$screenToChatY(mouseY);

        int i = iMixinChatComponent.kybe$getMessageLineIndexAt(d, e);
        if (!(i >= 0 && i < iMixinChatComponent.kybe$getTrimmedMessages().size())) return;

        GuiMessage.Line line = iMixinChatComponent.kybe$getTrimmedMessages().get(i);

        GuiMessage wholeMessage = iMixinChatComponent.kybe$getAllMessages()
            .stream()
            .filter(msg -> msg.addedTime() == line.addedTime())
            .findFirst()
            .orElse(null);

        if (wholeMessage != null) {
            String text = wholeMessage.content().getString();

            mc.keyboardHandler.setClipboard(text);
            cir.setReturnValue(true);
            return;
        }

        cir.setReturnValue(false);
    }
}
