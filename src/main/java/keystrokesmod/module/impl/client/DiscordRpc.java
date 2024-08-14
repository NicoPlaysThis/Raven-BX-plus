package keystrokesmod.module.impl.client;

import keystrokesmod.module.Module;
import keystrokesmod.module.impl.client.discordrpc.AugustusRPC;
import keystrokesmod.module.impl.client.discordrpc.BadlionRPC;
import keystrokesmod.module.impl.client.discordrpc.LunarClientRPC;
import keystrokesmod.module.impl.client.discordrpc.RavenBXplusRPC;
import keystrokesmod.module.setting.impl.ModeValue;

public class DiscordRpc extends Module {
    private final ModeValue mode;

    public DiscordRpc() {
        super("DiscordRPC", category.client);
        this.registerSetting(mode = new ModeValue("Mode", this)
                .add(new RavenBXplusRPC("Raven BX+", this))
                .add(new LunarClientRPC("Lunar Client", this))
                .add(new AugustusRPC("Augustus", this))
                .add(new BadlionRPC("Badlion Client", this))
                .setDefaultValue("Raven BX+"));
    }

    public void onEnable() {
        mode.enable();
    }

    public void onDisable() {
        mode.disable();
    }
}
