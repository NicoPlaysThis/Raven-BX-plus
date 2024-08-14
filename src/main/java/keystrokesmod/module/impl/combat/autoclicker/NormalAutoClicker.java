package keystrokesmod.module.impl.combat.autoclicker;

import keystrokesmod.module.impl.combat.HitSelect;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.CoolDown;
import keystrokesmod.utility.Utils;
import org.jetbrains.annotations.NotNull;

public class NormalAutoClicker extends SubMode<IAutoClicker> {
    private final SliderSetting minCPS = new SliderSetting("Min CPS", 8, 1, 40, 0.1);
    private final SliderSetting maxCPS = new SliderSetting("Max CPS", 14, 1, 40, 0.1);
    private final ButtonSetting butterFly = new ButtonSetting("Butterfly", true);
    private final boolean leftClick;
    private final boolean rightClick;
    private final boolean always;

    private final CoolDown clickStopWatch = new CoolDown(0);
    private int ticksDown;
    private long nextSwing;

    public NormalAutoClicker(String name, @NotNull IAutoClicker parent, boolean left, boolean always) {
        super(name, parent);
        this.leftClick = left;
        this.rightClick = !left;
        this.always = always;

        this.registerSetting(minCPS, maxCPS, butterFly);
    }

    @Override
    public void guiUpdate() {
        Utils.correctValue(minCPS, maxCPS);
    }

    @Override
    public void onUpdate() {
        clickStopWatch.setCooldown(nextSwing);
        if (clickStopWatch.hasFinished() && HitSelect.canAttack(mc.objectMouseOver.entityHit) && mc.currentScreen == null) {
            final long clicks = (long) (Utils.randomizeDouble(minCPS.getInput(), maxCPS.getInput()));

            if (mc.gameSettings.keyBindAttack.isKeyDown() || always) {
                ticksDown++;
            } else {
                ticksDown = 0;
            }

            if (this.nextSwing >= 50 * 2 && butterFly.isToggled()) {
                this.nextSwing = (long) (Math.random() * 100);
            } else {
                this.nextSwing = 1000 / clicks;
            }

            if (rightClick && ((mc.gameSettings.keyBindUseItem.isKeyDown() && !mc.gameSettings.keyBindAttack.isKeyDown()) || always)) {
                parent.click();

                if (Math.random() > 0.9) {
                    parent.click();
                }
            }

            if (leftClick && ticksDown > 1 && (!mc.gameSettings.keyBindUseItem.isKeyDown() || always)) {
                parent.click();
            }

            this.clickStopWatch.start();
        }
    }
}
