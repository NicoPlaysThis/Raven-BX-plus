package keystrokesmod.module.impl.other;

import keystrokesmod.event.MoveInputEvent;
import keystrokesmod.event.RotationEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.impl.world.Scaffold;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.DescriptionSetting;
import keystrokesmod.module.setting.impl.ModeSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.utils.ModeOnly;
import keystrokesmod.utility.AimSimulator;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.RotationUtils;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public final class RotationHandler extends Module {
    private static @Nullable Float movementYaw = null;
    private static @Nullable Float rotationYaw = null;
    private static @Nullable Float rotationPitch = null;
    private boolean isSet = false;
    private static MoveFix moveFix = MoveFix.NONE;

    private static final ModeSetting defaultMoveFix = new ModeSetting("Default MoveFix", new String[]{"None", "Silent", "Strict"}, 0);
    private final ModeSetting smoothBack = new ModeSetting("Smooth back", new String[]{"None", "Default"}, 0);
    private final SliderSetting aimSpeed = new SliderSetting("Aim speed", 5, 1, 15, 0.1, new ModeOnly(smoothBack, 1));
    public static final ButtonSetting rotateBody = new ButtonSetting("Rotate body", true);
    public static final ButtonSetting fullBody = new ButtonSetting("Full body", false);
    public static final SliderSetting randomYawFactor = new SliderSetting("Random yaw factor", 1.0, 0.0, 10.0, 1.0);

    public RotationHandler() {
        super("RotationHandler", category.other);
        this.registerSetting(defaultMoveFix, smoothBack, aimSpeed);
        this.registerSetting(new DescriptionSetting("Classic"));
        this.registerSetting(rotateBody, fullBody, randomYawFactor);
        this.canBeEnabled = false;
    }

    public static float getMovementYaw(Entity entity) {
        if (entity instanceof EntityPlayerSP && movementYaw != null)
            return movementYaw;
        return entity.rotationYaw;
    }

    public static void setMovementYaw(float movementYaw) {
        RotationHandler.movementYaw = movementYaw;
    }

    public static void setRotationYaw(float rotationYaw) {
        if (AimSimulator.yawEquals(rotationYaw, mc.thePlayer.rotationYaw)) {
            RotationHandler.rotationYaw = null;
            return;
        }
        RotationHandler.rotationYaw = rotationYaw;
    }

    public static void setRotationPitch(float rotationPitch) {
        if (rotationPitch == mc.thePlayer.rotationPitch) {
            RotationHandler.rotationPitch = null;
            return;
        }
        RotationHandler.rotationPitch = rotationPitch;
    }

    public static void setMoveFix(MoveFix moveFix) {
        RotationHandler.moveFix = moveFix;
    }

    public static MoveFix getMoveFix() {
        if (moveFix != null)
            return moveFix;
        return MoveFix.values()[(int) defaultMoveFix.getInput()];
    }

    public static float getRotationYaw() {
        if (rotationYaw != null)
            return RotationUtils.normalize(rotationYaw);
        return RotationUtils.normalize(mc.thePlayer.rotationYaw);
    }

    public static float getRotationPitch() {
        if (rotationPitch != null)
            return rotationPitch;
        return mc.thePlayer.rotationPitch;
    }

    /**
     * Fix movement
     * @param event before update living entity (move)
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPreMotion(MoveInputEvent event) {
        if (isSet && mc.currentScreen == null) {
            float viewYaw = RotationUtils.normalize(mc.thePlayer.rotationYaw);
            float viewPitch = RotationUtils.normalize(mc.thePlayer.rotationPitch);
            switch ((int) smoothBack.getInput()) {
                case 0:
                    rotationYaw = null;
                    rotationPitch = null;
                    break;
                case 1:
                    setRotationYaw(AimSimulator.rotMove(viewYaw, getRotationYaw(), (float) aimSpeed.getInput()));
                    setRotationPitch(AimSimulator.rotMove(viewPitch, getRotationPitch(), (float) aimSpeed.getInput()));
                    break;
            }
        }

        if (AimSimulator.yawEquals(getRotationYaw(), mc.thePlayer.rotationYaw)) rotationYaw = null;
        if (getRotationPitch() == mc.thePlayer.rotationPitch) rotationPitch = null;

        RotationEvent rotationEvent = new RotationEvent(getRotationYaw(), getRotationPitch(), MoveFix.values()[(int) defaultMoveFix.getInput()]);
        MinecraftForge.EVENT_BUS.post(rotationEvent);
        isSet = rotationEvent.isSet() || rotationYaw != null || rotationPitch != null;
        if (isSet) {
            rotationYaw = rotationEvent.getYaw();
            rotationPitch = rotationEvent.getPitch();
            moveFix = rotationEvent.getMoveFix();

            switch (moveFix) {
                case NONE:
                    movementYaw = null;
                    break;
                case SILENT:
                    movementYaw = getRotationYaw();

                    final float forward = event.getForward();
                    final float strafe = event.getStrafe();

                    final double angle = MathHelper.wrapAngleTo180_double(Math.toDegrees(MoveUtil.direction(mc.thePlayer.rotationYaw, forward, strafe)));

                    if (forward == 0 && strafe == 0) {
                        return;
                    }

                    float closestForward = 0, closestStrafe = 0, closestDifference = Float.MAX_VALUE;

                    for (float predictedForward = -1F; predictedForward <= 1F; predictedForward += 1F) {
                        for (float predictedStrafe = -1F; predictedStrafe <= 1F; predictedStrafe += 1F) {
                            if (predictedStrafe == 0 && predictedForward == 0) continue;

                            final double predictedAngle = MathHelper.wrapAngleTo180_double(Math.toDegrees(MoveUtil.direction(movementYaw, predictedForward, predictedStrafe)));
                            final double difference = Math.abs(angle - predictedAngle);

                            if (difference < closestDifference) {
                                closestDifference = (float) difference;
                                closestForward = predictedForward;
                                closestStrafe = predictedStrafe;
                            }
                        }
                    }

                    event.setForward(closestForward);
                    event.setStrafe(closestStrafe);
                    break;
                case STRICT:
                    movementYaw = getRotationYaw();
                    break;
            }
        } else {
            movementYaw = null;
            moveFix = null;
        }
    }

    public enum MoveFix {
        NONE,
        SILENT,
        STRICT;

        public static final String[] MODES = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);
    }
}
