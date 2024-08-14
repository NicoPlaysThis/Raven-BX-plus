package keystrokesmod.module.impl.client;

import keystrokesmod.event.SendPacketEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.ModeSetting;
import keystrokesmod.utility.Commands;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class CommandChat extends Module {
    private static final String[] IDENTIFIERS = new String[]{".", "#", "@"};
    private final ModeSetting identifier;
    public CommandChat() {
        super("Command chat", category.client);
        this.registerSetting(identifier = new ModeSetting("Identifier", IDENTIFIERS, 0));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onSendPacket(@NotNull SendPacketEvent event) {
        if (event.getPacket() instanceof C01PacketChatMessage) {
            final String message = ((C01PacketChatMessage) event.getPacket()).getMessage();

            if (message.startsWith(IDENTIFIERS[(int) identifier.getInput()])) {
                event.setCanceled(true);

                Commands.rCMD(message.substring(1));
            }
        }
    }
}
