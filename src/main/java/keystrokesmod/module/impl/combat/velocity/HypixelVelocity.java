package keystrokesmod.module.impl.combat.velocity;

import keystrokesmod.event.PreVelocityEvent;
import keystrokesmod.module.impl.combat.Velocity;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.MoveUtil;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class HypixelVelocity extends SubMode<Velocity> {
    private final SliderSetting horizontal;
    private final SliderSetting vertical;
    private final SliderSetting chance;
    private final ButtonSetting cancelAir;
    private final ButtonSetting damageBoost;
    private final ButtonSetting onlyFirstHit;
    private final SliderSetting resetTime;

    private long lastVelocityTime = 0;

    public HypixelVelocity(String name, @NotNull Velocity parent) {
        super(name, parent);
        this.registerSetting(horizontal = new SliderSetting("Horizontal", 90.0, -100.0, 100.0, 1.0));
        this.registerSetting(vertical = new SliderSetting("Vertical", 100.0, 0.0, 100.0, 1.0));
        this.registerSetting(chance = new SliderSetting("Chance", 100, 0, 100, 1, "%"));
        this.registerSetting(cancelAir = new ButtonSetting("Cancel air", false));
        this.registerSetting(damageBoost = new ButtonSetting("Damage boost", false));
        this.registerSetting(onlyFirstHit = new ButtonSetting("Only first hit", false));
        this.registerSetting(resetTime = new SliderSetting("Reset time", 5000, 500, 10000, 500, "ms", onlyFirstHit::isToggled));
    }

    @SubscribeEvent
    public void onPreVelocity(@NotNull PreVelocityEvent event) {
        final long time = System.currentTimeMillis();

        if (onlyFirstHit.isToggled() && time - lastVelocityTime < resetTime.getInput())
            return;
        lastVelocityTime = time;

        event.setCanceled(true);

        if (!mc.thePlayer.onGround && cancelAir.isToggled())
            return;

        double motionX = event.getMotionX() / 8000.0;
        double motionY = event.getMotionY() / 8000.0;
        double motionZ = event.getMotionZ() / 8000.0;

        if (chance.getInput() == 100 || Math.random() * 100 <= chance.getInput()) {
            motionX *= horizontal.getInput() / 100;
            motionY *= vertical.getInput() / 100;
            motionZ *= horizontal.getInput() / 100;
        }

        if (motionY != 0)
            mc.thePlayer.motionY = motionY;

        if (damageBoost.isToggled()) {
            MoveUtil.strafe(Math.hypot(motionX, motionZ));
        } else {
            if (motionX != 0)
                mc.thePlayer.motionX = motionX;
            if (motionZ != 0)
                mc.thePlayer.motionZ = motionZ;
        }
    }
}
