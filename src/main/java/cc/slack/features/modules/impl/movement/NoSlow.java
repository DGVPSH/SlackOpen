// Slack Client (discord.gg/paGUcq2UTb)

package cc.slack.features.modules.impl.movement;

import cc.slack.start.Slack;
import cc.slack.events.impl.network.PacketEvent;
import cc.slack.events.impl.player.UpdateEvent;
import cc.slack.features.modules.api.Category;
import cc.slack.features.modules.api.Module;
import cc.slack.features.modules.api.ModuleInfo;
import cc.slack.features.modules.api.settings.impl.BooleanValue;
import cc.slack.features.modules.api.settings.impl.ModeValue;
import cc.slack.features.modules.api.settings.impl.NumberValue;
import cc.slack.features.modules.impl.combat.KillAura;
import cc.slack.utils.network.PacketUtil;
import cc.slack.utils.network.BlinkUtil;
import cc.slack.utils.player.MovementUtil;
import io.github.nevalackin.radbus.Listen;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.item.*;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.util.BlockPos;

@ModuleInfo(
        name = "NoSlow",
        category = Category.MOVEMENT
)

public class NoSlow extends Module {

    // Slow Modes
    public final ModeValue<String> blockmode = new ModeValue<>("Block", new String[]{"None", "Vanilla", "NCP Latest", "Hypixel", "Hypixel 1.9", "Switch", "Place", "C08 Tick"});
    public final ModeValue<String> eatmode = new ModeValue<>("Eat", new String[]{"None","Vanilla", "NCP Latest", "Hypixel", "Switch", "Place", "C08 Tick", "Blink", "Slowed", "Float"});
    public final ModeValue<String> potionmode = new ModeValue<>("Potion", new String[]{"None","Vanilla", "NCP Latest", "Hypixel", "Switch", "Place", "C08 Tick", "Float"});
    public final ModeValue<String> bowmode = new ModeValue<>("Bow", new String[]{"None","Vanilla", "NCP Latest", "Hypixel", "Switch", "Place", "C08 Tick", "Float"});


    public final NumberValue<Float> forwardMultiplier = new NumberValue<>("Forward Multiplier", 1f, 0.2f,1f, 0.05f);
    public final NumberValue<Float> strafeMultiplier = new NumberValue<>("Strafe Multiplier", 1f, 0.2f,1f, 0.05f);
    private final BooleanValue sprint = new BooleanValue("Sprint", true);

    // Display
    private final ModeValue<String> displayMode = new ModeValue<>("Display", new String[]{"Advanced","Simple", "Off"});


    public float fMultiplier = 0F;
    public float sMultiplier = 0F;
    public boolean sprinting = true;

    private boolean blink = false;

    private boolean doFloat = false;

    public NoSlow() {
        addSettings(blockmode, eatmode, potionmode, bowmode
                , forwardMultiplier, strafeMultiplier, sprint);
    }

    private boolean badC07 = false;

    @Override
    public void onDisable() {
        if (blink) {
            blink = false;
            BlinkUtil.disable();
        }
    }

    @SuppressWarnings("unused")
    @Listen
    public void onUpdate(UpdateEvent event) {
        doFloat = false;
        if (mc.thePlayer == null || mc.thePlayer.getHeldItem() == null || mc.thePlayer.getHeldItem().item == null) return;

        fMultiplier = forwardMultiplier.getValue();
        sMultiplier = strafeMultiplier.getValue();

        boolean usingItem = mc.thePlayer.isUsingItem() || (Slack.getInstance().getModuleManager().getInstance(KillAura.class).isToggle() && Slack.getInstance().getModuleManager().getInstance(KillAura.class).isBlocking);

        if (usingItem && mc.thePlayer.getHeldItem().item instanceof ItemSword) {
            String mode = blockmode.getValue().toLowerCase();
            processModeSword(mode);
        }

        if (mc.thePlayer.isUsingItem() && mc.thePlayer.getHeldItem().item instanceof ItemFood) {
            String mode = eatmode.getValue().toLowerCase();
            processModeEat(mode);

        } else if (!mc.thePlayer.isUsingItem() && blink) {
            blink = false;
            BlinkUtil.disable();
        }

        if (mc.thePlayer.getHeldItem().item instanceof ItemFood) {
            if (mc.thePlayer.offGroundTicks == 2) {
                if (GameSettings.isKeyDown(mc.gameSettings.keyBindUseItem)) {
                    mc.gameSettings.keyBindUseItem.pressed = true;
                }
            }
        }

        if (mc.thePlayer.isUsingItem() && mc.thePlayer.getHeldItem().item instanceof ItemPotion) {
            String mode = potionmode.getValue().toLowerCase();
            processModePotion(mode);
        }

        if (mc.thePlayer.isUsingItem() && mc.thePlayer.getHeldItem().item instanceof ItemBow) {
            String mode = bowmode.getValue().toLowerCase();
            processModeBow(mode);
        }

        badC07 = false;
    }

    // onSword (Blocking)
    private void processModeSword(String mode) {
        switch (mode) {
            case "none":
                setMultipliers(0.2F, 0.2F);
                break;
            case "vanilla":
                setMultipliers(forwardMultiplier.getValue(), strafeMultiplier.getValue());
                break;
            case "ncp latest":
            case "switch":
                setMultipliers(forwardMultiplier.getValue(), strafeMultiplier.getValue());
                mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem % 8 + 1));
                mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                break;
            case "place":
                setMultipliers(forwardMultiplier.getValue(), strafeMultiplier.getValue());
                mc.getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()));
                break;
            case "c08 tick":
                setMultipliers(forwardMultiplier.getValue(), strafeMultiplier.getValue());
                if (mc.thePlayer.ticksExisted % 3 == 0) {
                    mc.getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()));
                }
                break;
            case "hypixel 1.9":
                setMultipliers(1, 1);
                PacketUtil.switchItemToOffhand();
                break;
            case "hypixel":
                setMultipliers(1, 1);
                break;
        }
    }


    // onEat (Eating and drinking)
    private void processModeEat(String mode) {
        switch (mode) {
            case "none":
                setMultipliers(0.2F, 0.2F);
                break;
            case "vanilla":
                setMultipliers(forwardMultiplier.getValue(), strafeMultiplier.getValue());
                break;
            case "ncp latest":
            case "switch":
                setMultipliers(forwardMultiplier.getValue(), strafeMultiplier.getValue());
                mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem % 8 + 1));
                mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                break;
            case "place":
                setMultipliers(forwardMultiplier.getValue(), strafeMultiplier.getValue());
                mc.getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()));
                break;
            case "c08 tick":
                setMultipliers(forwardMultiplier.getValue(), strafeMultiplier.getValue());
                if (mc.thePlayer.ticksExisted % 3 == 0) {
                    mc.getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()));
                }
                break;
            case "hypixel":
                if (mc.thePlayer.getItemInUseDuration() == 0 && mc.thePlayer.onGround) {
                    mc.thePlayer.jump();
                    mc.thePlayer.motionX *= 0.2;
                    mc.thePlayer.motionZ *= 0.2;
                    mc.gameSettings.keyBindUseItem.pressed = false;
                }
                if (mc.thePlayer.offGroundTicks == 2) {
                    if (mc.gameSettings.keyBindUseItem.isKeyDown()) {
                        mc.gameSettings.keyBindUseItem.pressed = true;
                    }
                }
                if (mc.thePlayer.getItemInUseDuration() > 1) {
                    setMultipliers(forwardMultiplier.getValue(), strafeMultiplier.getValue());
                }
                doFloat = true;
                break;
            case "blink":
                if (mc.thePlayer.getItemInUseDuration() == 2) {
                    blink = true;
                    BlinkUtil.enable(false, true);
                } else if (mc.thePlayer.getItemInUseDuration() == 29) {
                    PacketUtil.sendNoEvent(new C08PacketPlayerBlockPlacement(new BlockPos(-1, -1, -1), 1, null, 0.0f, 0.0f, 0.0f));
                    blink = false;
                    BlinkUtil.disable();
                }
                break;
            case "slowed":
                setMultipliers(0.7f, 0.8f);
                break;
            case "float":
                if (mc.thePlayer.getItemInUseDuration() == 0 && mc.thePlayer.onGround) {
                    mc.thePlayer.jump();
                    mc.thePlayer.motionX *= 0.2;
                    mc.thePlayer.motionZ *= 0.2;
                    mc.gameSettings.keyBindUseItem.pressed = false;
                }
                if (mc.thePlayer.offGroundTicks == 2) {
                    if (mc.gameSettings.keyBindUseItem.isKeyDown()) {
                        mc.gameSettings.keyBindUseItem.pressed = true;
                    }
                }
                if (mc.thePlayer.getItemInUseDuration() > 1) {
                    setMultipliers(forwardMultiplier.getValue(), strafeMultiplier.getValue());
                }
                doFloat = true;
                break;

        }
    }

    // onPotion Slow
    private void processModePotion(String mode) {
        switch (mode) {
            case "none":
                setMultipliers(0.2F, 0.2F);
                break;
            case "vanilla":
                setMultipliers(forwardMultiplier.getValue(), strafeMultiplier.getValue());
                break;
            case "ncp latest":
            case "switch":
                setMultipliers(forwardMultiplier.getValue(), strafeMultiplier.getValue());
                mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem % 8 + 1));
                mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                break;
            case "place":
                setMultipliers(forwardMultiplier.getValue(), strafeMultiplier.getValue());
                mc.getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()));
                break;
            case "c08 tick":
                setMultipliers(forwardMultiplier.getValue(), strafeMultiplier.getValue());
                if (mc.thePlayer.ticksExisted % 3 == 0) {
                    mc.getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()));
                }
                break;
            case "hypixel":
            case "float":
                if (mc.thePlayer.getItemInUseDuration() == 0 && mc.thePlayer.onGround) {
                    mc.thePlayer.jump();
                }
                if (mc.thePlayer.getItemInUseDuration() != 1) {
                    setMultipliers(forwardMultiplier.getValue(), strafeMultiplier.getValue());
                }
                doFloat = true;
                break;
        }
    }

    // onBow Slow
    private void processModeBow(String mode) {
        switch (mode) {
            case "none":
                setMultipliers(0.2F, 0.2F);
                break;
            case "vanilla":
                setMultipliers(forwardMultiplier.getValue(), strafeMultiplier.getValue());
                break;
            case "ncp latest":
            case "switch":
                setMultipliers(forwardMultiplier.getValue(), strafeMultiplier.getValue());
                mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem % 8 + 1));
                mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                break;
            case "place":
                setMultipliers(forwardMultiplier.getValue(), strafeMultiplier.getValue());
                mc.getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()));
                break;
            case "c08 tick":
                setMultipliers(forwardMultiplier.getValue(), strafeMultiplier.getValue());
                if (mc.thePlayer.ticksExisted % 3 == 0) {
                    mc.getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()));
                }
                break;
            case "hypixel":
            case "float":
                if (mc.thePlayer.getItemInUseDuration() == 0 && mc.thePlayer.onGround) {
                    mc.thePlayer.jump();
                }
                if (mc.thePlayer.getItemInUseDuration() != 1) {
                    setMultipliers(forwardMultiplier.getValue(), strafeMultiplier.getValue());
                }
                doFloat = true;
                break;
        }
    }

    private void setMultipliers(float forward, float strafe) {
        fMultiplier = forward;
        sMultiplier = strafe;
        sprinting = sprint.getValue();
    }

    @Listen
    public void onPacket(PacketEvent e) {

        if (e.getPacket() instanceof C07PacketPlayerDigging) badC07 = true;
        if (doFloat && e.getPacket() instanceof C03PacketPlayer) {
            ((C03PacketPlayer) e.getPacket()).y += 0.00000000000001;
        }
    }

    @Override
    public String getMode() {
        return blockmode.getValue();
    }

}
