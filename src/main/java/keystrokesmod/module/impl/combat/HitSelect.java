package keystrokesmod.module.impl.combat;

import keystrokesmod.event.PreUpdateEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.ModeSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.MoveUtil;
import net.minecraft.entity.Entity;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import static keystrokesmod.module.ModuleManager.hitSelect;

public class HitSelect extends Module {
    private static final String[] MODES = new String[]{"Pause", "Active"};
    private static final String[] PREFERENCES = new String[]{"Move speed", "KB reduction", "Critical hits"};
    private final ModeSetting preference;
    private final ModeSetting mode;
    private final SliderSetting delay;
    private final SliderSetting chance;

    private static long attackTime = -1;
    private static boolean currentShouldAttack = false;

    public HitSelect() {
        super("HitSelect", category.combat, "Chooses the best time to hit.");
        this.registerSetting(mode = new ModeSetting("Mode", MODES, 0,
                "Pause: Legitimate pause clicking\n" +
                "Active: Cancel attack but allow click"));
        this.registerSetting(preference = new ModeSetting("Preference", PREFERENCES, 0,
                "Move speed: Keep sprint but legitimate\n" +
                        "KB reduction: KnockBack reduction\n" +
                        "Critical hits: Critical hit frequency"));
        this.registerSetting(delay = new SliderSetting("Delay", 420, 300, 500, 1));
        this.registerSetting(chance = new SliderSetting("Chance", 80, 0, 100, 1));
    }

    @Override
    public String getInfo() {
        return MODES[(int) mode.getInput()];
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onAttack(@NotNull AttackEntityEvent event) {
        if (mode.getInput() == 1 && !currentShouldAttack) {
            event.setCanceled(true);
            return;
        }

        attackTime = System.currentTimeMillis();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPreUpdate(PreUpdateEvent event) {
        if (Math.random() > hitSelect.chance.getInput()) {
            currentShouldAttack = true;
        } else {
            switch ((int) preference.getInput()) {
                case 1:
                    currentShouldAttack = mc.thePlayer.hurtTime > 0 && !mc.thePlayer.onGround && MoveUtil.isMoving();
                    break;
                case 2:
                    currentShouldAttack = !mc.thePlayer.onGround && mc.thePlayer.motionY < 0;
                    break;
            }

            if (!currentShouldAttack)
                currentShouldAttack = System.currentTimeMillis() - HitSelect.attackTime >= hitSelect.delay.getInput();
        }
    }

    public static boolean canAttack(Entity target) {
        return canSwing();
    }

    public static boolean canSwing() {
        if (!hitSelect.isEnabled() || hitSelect.mode.getInput() == 1) return true;
        return currentShouldAttack;
    }
}
