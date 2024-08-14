package keystrokesmod.module.impl.minigames;

import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.DescriptionSetting;
import keystrokesmod.module.setting.impl.ModeSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.Utils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Mouse;

import java.util.Arrays;
import java.util.List;
import java.util.TimerTask;

public class SumoFences extends Module {
    public static SliderSetting fenceHeight;
    public ModeSetting blockType;
    private java.util.Timer t;
    private final List<String> maps = Arrays.asList("Sumo", "Space Mine", "White Crystal", "Fort");
    private IBlockState f;
    private static final List<BlockPos> f_p = Arrays.asList(new BlockPos(9, 65, -2), new BlockPos(9, 65, -1),
            new BlockPos(9, 65, 0), new BlockPos(9, 65, 1), new BlockPos(9, 65, 2), new BlockPos(9, 65, 3),
            new BlockPos(8, 65, 3), new BlockPos(8, 65, 4), new BlockPos(8, 65, 5), new BlockPos(7, 65, 5),
            new BlockPos(7, 65, 6), new BlockPos(7, 65, 7), new BlockPos(6, 65, 7), new BlockPos(5, 65, 7),
            new BlockPos(5, 65, 8), new BlockPos(4, 65, 8), new BlockPos(3, 65, 8), new BlockPos(3, 65, 9),
            new BlockPos(2, 65, 9), new BlockPos(1, 65, 9), new BlockPos(0, 65, 9), new BlockPos(-1, 65, 9),
            new BlockPos(-2, 65, 9), new BlockPos(-3, 65, 9), new BlockPos(-3, 65, 8), new BlockPos(-4, 65, 8),
            new BlockPos(-5, 65, 8), new BlockPos(-5, 65, 7), new BlockPos(-6, 65, 7), new BlockPos(-7, 65, 7),
            new BlockPos(-7, 65, 6), new BlockPos(-7, 65, 5), new BlockPos(-8, 65, 5), new BlockPos(-8, 65, 4),
            new BlockPos(-8, 65, 3), new BlockPos(-9, 65, 3), new BlockPos(-9, 65, 2), new BlockPos(-9, 65, 1),
            new BlockPos(-9, 65, 0), new BlockPos(-9, 65, -1), new BlockPos(-9, 65, -2), new BlockPos(-9, 65, -3),
            new BlockPos(-8, 65, -3), new BlockPos(-8, 65, -4), new BlockPos(-8, 65, -5), new BlockPos(-7, 65, -5),
            new BlockPos(-7, 65, -6), new BlockPos(-7, 65, -7), new BlockPos(-6, 65, -7), new BlockPos(-5, 65, -7),
            new BlockPos(-5, 65, -8), new BlockPos(-4, 65, -8), new BlockPos(-3, 65, -8), new BlockPos(-3, 65, -9),
            new BlockPos(-2, 65, -9), new BlockPos(-1, 65, -9), new BlockPos(0, 65, -9), new BlockPos(1, 65, -9),
            new BlockPos(2, 65, -9), new BlockPos(3, 65, -9), new BlockPos(3, 65, -8), new BlockPos(4, 65, -8),
            new BlockPos(5, 65, -8), new BlockPos(5, 65, -7), new BlockPos(6, 65, -7), new BlockPos(7, 65, -7),
            new BlockPos(7, 65, -6), new BlockPos(7, 65, -5), new BlockPos(8, 65, -5), new BlockPos(8, 65, -4),
            new BlockPos(8, 65, -3), new BlockPos(9, 65, -3));
    private final String[] mode = new String[]{"Oak fence", "Leaves", "Glass", "Barrier"};

    public SumoFences() {
        super("Sumo Fences", category.minigames, 0);
        this.f = Blocks.oak_fence.getDefaultState();
        this.registerSetting(new DescriptionSetting("Fences for Hypixel sumo."));
        this.registerSetting(fenceHeight = new SliderSetting("Fence height", 4.0D, 1.0D, 16.0D, 1.0D));
        this.registerSetting(blockType = new ModeSetting("Block type", mode, 0));
    }

    public void onEnable() {
        (this.t = new java.util.Timer()).scheduleAtFixedRate(this.t(), 0L, 500L);
    }

    public void onDisable() {
        if (this.t != null) {
            this.t.cancel();
            this.t.purge();
            this.t = null;
        }

        for (BlockPos p : f_p) {
            for (int i = 0; (double) i < fenceHeight.getInput(); ++i) {
                BlockPos p2 = new BlockPos(p.getX(), p.getY() + i, p.getZ());
                if (mc.theWorld.getBlockState(p2).getBlock() == this.f) {
                    mc.theWorld.setBlockState(p2, Blocks.air.getDefaultState());
                }
            }
        }
    }

    @SubscribeEvent
    public void onMouse(MouseEvent e) {
        if (e.buttonstate && (e.button == 0 || e.button == 1) && Utils.nullCheck() && this.isSumo()) {
            MovingObjectPosition mop = mc.objectMouseOver;
            if (mop != null && mop.typeOfHit == MovingObjectType.BLOCK) {
                int x = mop.getBlockPos().getX();
                int z = mop.getBlockPos().getZ();

                for (BlockPos pos : f_p) {
                    if (pos.getX() == x && pos.getZ() == z) {
                        e.setCanceled(true);
                        if (e.button == 0) {
                            Utils.playerSwing();
                        }
                        Mouse.poll();
                        break;
                    }
                }
            }
        }
    }

    public TimerTask t() {
        return new TimerTask() {
            public void run() {
                if (SumoFences.this.isSumo()) {
                    for (BlockPos p : SumoFences.f_p) {
                        for (int i = 0; (double) i < SumoFences.fenceHeight.getInput(); ++i) {
                            BlockPos p2 = new BlockPos(p.getX(), p.getY() + i, p.getZ());
                            if (mc.theWorld.getBlockState(p2).getBlock() == Blocks.air) {
                                mc.theWorld.setBlockState(p2, SumoFences.this.f);
                            }
                        }
                    }

                }
            }
        };
    }

    private boolean isSumo() {
        if (Utils.isHypixel()) {
            for (String l : Utils.gsl()) {
                String s = Utils.stripColor(l);
                if (s.startsWith("Map:")) {
                    if (this.maps.contains(s.substring(5))) {
                        return true;
                    }
                } else if (s.equals("Mode: Sumo Duel")) {
                    return true;
                }
            }
        }

        return false;
    }

    public void guiUpdate() {
        switch ((int) blockType.getInput()) {
            case 0:
                this.f = Blocks.oak_fence.getDefaultState();
                break;
            case 1:
                this.f = Blocks.leaves.getDefaultState();
                break;
            case 2:
                this.f = Blocks.glass.getDefaultState();
                break;
            case 3:
                this.f = Blocks.barrier.getDefaultState();
        }
    }
}
