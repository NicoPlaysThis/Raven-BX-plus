package keystrokesmod.module.impl.combat.autoclicker;

import keystrokesmod.module.impl.combat.HitSelect;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.CoolDown;
import keystrokesmod.utility.Utils;
import org.jetbrains.annotations.NotNull;

public class LowCPSAutoClicker extends SubMode<IAutoClicker> {
    private final SliderSetting minDelay = new SliderSetting("Min Delay", 500, 100, 3000, 100, "ms");
    private final SliderSetting maxDelay = new SliderSetting("Max Delay", 1000, 100, 3000, 100, "ms");
    private final boolean leftClick;
    private final boolean rightClick;
    private final boolean always;

    private final CoolDown clickStopWatch = new CoolDown(0);
    private int ticksDown;
    private long nextSwing;

    public LowCPSAutoClicker(String name, @NotNull IAutoClicker parent, boolean left, boolean always) {
        super(name, parent);
        this.leftClick = left;
        this.rightClick = !left;
        this.always = always;

        this.registerSetting(minDelay, maxDelay);
    }

    @Override
    public void guiUpdate() {
        Utils.correctValue(minDelay, maxDelay);
    }

    @Override
    public void onUpdate() {
        clickStopWatch.setCooldown(nextSwing);
        if (clickStopWatch.hasFinished() && HitSelect.canAttack(mc.objectMouseOver.entityHit) && mc.currentScreen == null) {
            final long delay = (long) (Utils.randomizeDouble(minDelay.getInput(), maxDelay.getInput()));

            if (mc.gameSettings.keyBindAttack.isKeyDown() || always) {
                ticksDown++;
            } else {
                ticksDown = 0;
            }

            this.nextSwing = delay;

            if (rightClick && ((mc.gameSettings.keyBindUseItem.isKeyDown() && !mc.gameSettings.keyBindAttack.isKeyDown()) || always)) {
                parent.click();
            }

            if (leftClick && ticksDown > 1 && (!mc.gameSettings.keyBindUseItem.isKeyDown() || always)) {
                parent.click();
            }

            this.clickStopWatch.start();
        }
    }
}
