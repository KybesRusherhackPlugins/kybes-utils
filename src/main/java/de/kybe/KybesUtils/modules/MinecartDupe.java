package de.kybe.KybesUtils.modules;

import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.rusherhack.client.api.RusherHackAPI;
import org.rusherhack.client.api.events.client.EventUpdate;
import org.rusherhack.client.api.feature.module.ModuleCategory;
import org.rusherhack.client.api.feature.module.ToggleableModule;
import org.rusherhack.client.api.utils.ChatUtils;
import org.rusherhack.client.api.utils.RotationUtils;
import org.rusherhack.core.event.subscribe.Subscribe;
import org.rusherhack.core.setting.BooleanSetting;
import org.rusherhack.core.setting.NumberSetting;

import java.util.ArrayList;
import java.util.List;

public class MinecartDupe extends ToggleableModule {
    // Static fields representing positions and directions
    public static Vec3 railLocation = null;
    public static BlockPos railBlock = null;
    public static Direction minecartDirection = null;
    public static Vec3 minecartChestLocation = null;
    public static BlockPos minecartChestBlock = null;

    // Settings
    public static NumberSetting<Integer> fillItemsPerTick = new NumberSetting<>("Items Per Tick", 1, 1, 29);
    public static NumberSetting<Integer> stealItemsPerTick = new NumberSetting<>("Steal Items Per Tick", 1, 1, 29);
    public static NumberSetting<Integer> stealToFillDelay = new NumberSetting<>("Steal To Fill Delay", 10, 1, 100);
    public static NumberSetting<Integer> lagbackDelay = new NumberSetting<>("Lagback Delay", 10, 1, 100);
    public static NumberSetting<Integer> placeToCheckDelay = new NumberSetting<>("Place To Check Delay", 10, 1, 100);
    public static BooleanSetting refillMinecart = new BooleanSetting("Refill Minecart", true);

    private int shulkerDuped = 0;
    private int stealToFillTicks = 0;
    private int ticks = 0;

    private int oldSlots = 999;
    private int oldShulkerSlots = -1;

    private enum State {
        PLACE_MINECART,
        FILL_MINECART,
        STEAL_TO_FILL_DELAY,
        MOVE_MINECART_AND_STEAL,
        REVERT,
        GET_MINECART
    }

    private State state = State.PLACE_MINECART;

    public MinecartDupe() {
        super("Minecart Dupe", ModuleCategory.MISC);
        this.registerSettings(
                fillItemsPerTick,
                stealItemsPerTick,
                stealToFillDelay,
                lagbackDelay,
                placeToCheckDelay,
                refillMinecart
        );
    }

    @Subscribe
    public void onTick(EventUpdate event) {
        if (!validateState()) {
            this.setToggled(false);
            return;
        }

        if (ticks < 0) {
            ticks++;
            return;
        } else {
            ticks = 0;
        }

        switch (state) {
            case PLACE_MINECART -> handlePlaceMinecart();
            case FILL_MINECART -> handleFillMinecart();
            case STEAL_TO_FILL_DELAY -> handleStealToFillDelay();
            case MOVE_MINECART_AND_STEAL -> handleMoveMinecartAndSteal();
            case REVERT -> handleRevert();
            case GET_MINECART -> handleGetMinecart();
        }
    }

    private boolean validateState() {
        if (mc.level == null) {
            RusherHackAPI.getNotificationManager().error("Level is null");
            return false;
        }
        if (mc.player == null) {
            RusherHackAPI.getNotificationManager().error("Player is null");
            return false;
        }
        if (mc.gameMode == null) {
            RusherHackAPI.getNotificationManager().error("GameMode is null");
            return false;
        }
        if (railLocation == null || railBlock == null) {
            RusherHackAPI.getNotificationManager().error("railLocation set it with *minecartDupe rails");
            return false;
        }
        if (minecartDirection == null) {
            RusherHackAPI.getNotificationManager().error("Direction is null set it with *minecartDupe direction. while looking in minecart direction");
            return false;
        }
        if (refillMinecart.getValue() && (minecartChestLocation == null || minecartChestBlock == null)) {
            RusherHackAPI.getNotificationManager().error("minecartChestLocation set it with *minecartDupe minecart while looking at the chest");
            return false;
        }
        return true;
    }

    private void handlePlaceMinecart() {
        if (mc.player == null || mc.gameMode == null) return;
        lookAtMinecart();

        Entity minecart = getMinecart();
        if (minecart != null) {
            if (mc.screen instanceof ContainerScreen) {
                closeScreen();
            }
            state = State.FILL_MINECART;
            return;
        }

        if (getChestMinecart()) return;

        var bhr = RusherHackAPI.interactions().getBlockHitResult(railBlock, false, false, 5);
        if (bhr != null && mc.player.getInventory().getItem(mc.player.getInventory().selected).getItem() == Items.CHEST_MINECART) {
            mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, bhr);
        }
    }

    private void handleFillMinecart() {
        openMinecart();
        if (!(mc.screen instanceof ContainerScreen screen)) return;

        if (fillMinecart(screen)) {
            oldSlots = -1;
            state = State.STEAL_TO_FILL_DELAY;
        }
    }

    private void handleStealToFillDelay() {
        if (stealToFillTicks < stealToFillDelay.getValue()) {
            stealToFillTicks++;
        } else {
            state = State.MOVE_MINECART_AND_STEAL;
        }
    }

    boolean moveStarted = false;
    private void handleMoveMinecartAndSteal() {
        if (mc.player == null) return;
        openMinecart();

        if (!(mc.screen instanceof ContainerScreen screen) || clearMinecart(screen)) {
            moveStarted = true;
            double speed = 0.2;
            double stepX = minecartDirection.getStepX();
            double stepY = minecartDirection.getStepY();
            double stepZ = minecartDirection.getStepZ();

            mc.player.setDeltaMovement(stepX * speed, stepY, stepZ * speed);

            if (isInDirectionOfBlock(railBlock, minecartDirection)) {
                mc.player.setDeltaMovement(0, 0, 0);
                moveStarted = false;
                state = State.REVERT;
            }
        }
    }

    private void handleRevert() {
        if (mc.player == null) return;
        double speed = 0.2;
        double stepX = -minecartDirection.getStepX();
        double stepY = -minecartDirection.getStepY();
        double stepZ = -minecartDirection.getStepZ();

        mc.player.setDeltaMovement(stepX * speed, stepY, stepZ * speed);

        if (isInDirectionOfBlock(railBlock.relative(minecartDirection.getOpposite()), minecartDirection.getOpposite())) {
            mc.player.setDeltaMovement(0, 0, 0);
            getShulkerCount();
            ChatUtils.print("Dupe Count: " + shulkerDuped);

            if (refillMinecart.getValue()) {
                state = getMinecartCount() != 0 ? State.PLACE_MINECART : State.GET_MINECART;
            } else {
                state = State.PLACE_MINECART;
            }

            if (state == State.GET_MINECART) {
                ticks -= 10;
            }
        }
    }

    private void handleGetMinecart() {
        if (mc.player == null || mc.gameMode == null) return;
        if (mc.screen == null && minecartChestLocation != null && minecartChestBlock != null) {
            mc.player.lookAt(EntityAnchorArgument.Anchor.EYES, minecartChestLocation);
            float[] rotations = RotationUtils.getRotations(minecartChestLocation);
            mc.player.connection.send(new ServerboundMovePlayerPacket.Rot(rotations[0], rotations[1], mc.player.onGround(), mc.player.horizontalCollision));
            RusherHackAPI.interactions().useBlock(minecartChestBlock, InteractionHand.MAIN_HAND, true, false);
            ticks -= 10;
            return;
        }

        if (!(mc.screen instanceof ContainerScreen screen)) {
            ChatUtils.print("Screen is not an instance of ContainerScreen");
            return;
        }

        try {
            int freeSlots = countFreeSlots(screen.getMenu(), 27, 63);

            if (freeSlots == 0) {
                state = State.PLACE_MINECART;
                return;
            }

            for (int i = 0; i < 27; i++) {
                ItemStack itemStack = screen.getMenu().getSlot(i).getItem();
                if (!itemStack.isEmpty() && itemStack.getItem() == Items.CHEST_MINECART) {
                    ChatUtils.print("Found CHEST_MINECART in slot: " + i);
                    mc.gameMode.handleInventoryMouseClick(screen.getMenu().containerId, i, 0, ClickType.QUICK_MOVE, mc.player);

                    if (freeSlots <= 2 || getMinecartCount() == 0) {
                        ticks -= 10;
                        state = State.PLACE_MINECART;
                        if (isShulkerBox(screen.getMenu().getCarried().getItem())) {
                            moveShulkerBoxesToInventory(screen);
                        }
                        mc.player.closeContainer();
                        return;
                    }
                }
            }
            ChatUtils.print("CHEST_MINECART not found in the chest.");
        } catch (Exception e) {
            ChatUtils.print("Error retrieving CHEST_MINECART: " + e.getMessage());
        }
    }

    private int countFreeSlots(AbstractContainerMenu container, int start, int end) {
        int freeSlots = 0;
        for (int i = start; i < end; i++) {
            ItemStack itemStack = container.getSlot(i).getItem();
            if (itemStack.isEmpty() || itemStack.getItem() == Items.AIR) {
                freeSlots++;
            }
        }
        return freeSlots;
    }

    private void moveShulkerBoxesToInventory(ContainerScreen screen) {
        if (mc.player == null || mc.gameMode == null) return;
        AbstractContainerMenu container = screen.getMenu();
        for (int b = 27; b < 63; b++) {
            ItemStack stack = container.getSlot(b).getItem();
            if (stack.isEmpty() || stack.getItem() == Items.CHEST_MINECART) {
                mc.gameMode.handleInventoryMouseClick(container.containerId, b, 0, ClickType.PICKUP, mc.player);
            }
        }
    }

    public int getMinecartCount() {
        int count = 0;
        for (int i = 0; i < 36; i++) {
            ItemStack itemStack = mc.player.getInventory().getItem(i);
            if (!itemStack.isEmpty() && itemStack.getItem() == Items.CHEST_MINECART) {
                count++;
            }
        }
        return count;
    }

    public boolean isInDirectionOfBlock(BlockPos pos, Direction direction) {
        double blockCenterX = pos.getCenter().x;
        double blockCenterZ = pos.getCenter().z;

        double playerX = mc.player.position().x;
        double playerZ = mc.player.position().z;

        int stepX = direction.getStepX();
        int stepZ = direction.getStepZ();

        if (stepX != 0) {
            return stepX > 0 ? playerX >= blockCenterX : playerX < blockCenterX;
        } else if (stepZ != 0) {
            return stepZ > 0 ? playerZ >= blockCenterZ : playerZ < blockCenterZ;
        }

        return false;
    }

    public boolean clearMinecart(ContainerScreen screen) {
        AbstractContainerMenu container = screen.getMenu();
        ArrayList<Integer> shulkerSlots = getShulkerSlots(container, 0, 27);

        if (oldShulkerSlots < shulkerSlots.size()) {
            ChatUtils.print("lagback detected delaying by " + lagbackDelay.getValue());
            this.ticks -= lagbackDelay.getValue();
            oldShulkerSlots = shulkerSlots.size();
            return false;
        }

        if (isShulkerBox(container.getCarried().getItem())) {
            for (int i = 0; i < 27; i++) {
                ItemStack itemStack = container.getSlot(i).getItem();
                if (itemStack.isEmpty() || itemStack.getItem() == Items.AIR) {
                    mc.gameMode.handleInventoryMouseClick(container.containerId, i, 0, ClickType.PICKUP, mc.player);
                    return false;
                }
            }
        }

        oldShulkerSlots = shulkerSlots.size();

        if (shulkerSlots.isEmpty() && !isShulkerBox(container.getCarried().getItem())) return true;

        int currentMoves = 0;

        for (int i = 27; i < 63; i++) {
            ItemStack itemStack = container.getSlot(i).getItem();
            if (itemStack.isEmpty() || itemStack.getItem() == Items.AIR) {
                if (shulkerSlots.isEmpty() && !isShulkerBox(container.getCarried().getItem())) return true;

                int slot = shulkerSlots.remove(0);
                mc.gameMode.handleInventoryMouseClick(container.containerId, slot, 0, ClickType.QUICK_MOVE, mc.player);

                currentMoves++;
                if (currentMoves >= stealItemsPerTick.getValue()) {
                    return false;
                }
            }
        }
        return false;
    }

    public void getShulkerCount() {
        for (int i = 0; i < 27; i++) {
            ItemStack itemStack = mc.player.getInventory().getItem(i);
            if (!itemStack.isEmpty() && isShulkerBox(itemStack.getItem())) {
                shulkerDuped++;
            }
        }
    }

    public boolean fillMinecart(ContainerScreen screen) {
        AbstractContainerMenu container = screen.getMenu();
        ArrayList<Integer> shulkerSlots = getShulkerSlots(container, 27, 63);

        int expectedSlots = isShulkerBox(container.getCarried().getItem()) ? shulkerSlots.size() + 1 : shulkerSlots.size();

        if (oldSlots < expectedSlots) {
            ChatUtils.print("lagback detected delaying by " + lagbackDelay.getValue());
            this.ticks -= lagbackDelay.getValue();
            oldSlots = 999;
            return false;
        }

        if (isShulkerBox(container.getCarried().getItem())) {
            for (int i = 27; i < 63; i++) {
                ItemStack itemStack = container.getSlot(i).getItem();
                if (itemStack.isEmpty() || itemStack.getItem() == Items.AIR) {
                    mc.gameMode.handleInventoryMouseClick(container.containerId, i, 0, ClickType.PICKUP, mc.player);
                    return false;
                }
            }
        }

        oldSlots = shulkerSlots.size();

        if (shulkerSlots.isEmpty() && !isShulkerBox(container.getCarried().getItem())) return true;

        int currentMoves = 0;
        for (int i = 0; i < 63; i++) {
            ItemStack itemStack = container.getSlot(i).getItem();
            if (itemStack.isEmpty() || itemStack.getItem() == Items.AIR) {
                if (shulkerSlots.isEmpty() && !isShulkerBox(container.getCarried().getItem())) return true;

                int slot = shulkerSlots.remove(0);
                mc.gameMode.handleInventoryMouseClick(container.containerId, slot, 0, ClickType.QUICK_MOVE, mc.player);

                currentMoves++;
                if (currentMoves >= fillItemsPerTick.getValue()) {
                    return false;
                }
            }
        }
        return false;
    }

    private ArrayList<Integer> getShulkerSlots(AbstractContainerMenu container, int start, int end) {
        ArrayList<Integer> shulkerSlots = new ArrayList<>();
        for (int i = start; i < end; i++) {
            ItemStack itemStack = container.getSlot(i).getItem();
            if (!itemStack.isEmpty() && isShulkerBox(itemStack.getItem())) {
                shulkerSlots.add(i);
            }
        }
        return shulkerSlots;
    }

    private final List<Item> shulkers = List.of(
            Items.SHULKER_BOX,
            Items.WHITE_SHULKER_BOX,
            Items.ORANGE_SHULKER_BOX,
            Items.MAGENTA_SHULKER_BOX,
            Items.LIGHT_BLUE_SHULKER_BOX,
            Items.YELLOW_SHULKER_BOX,
            Items.LIME_SHULKER_BOX,
            Items.PINK_SHULKER_BOX,
            Items.GRAY_SHULKER_BOX,
            Items.LIGHT_GRAY_SHULKER_BOX,
            Items.CYAN_SHULKER_BOX,
            Items.PURPLE_SHULKER_BOX,
            Items.BLUE_SHULKER_BOX,
            Items.BROWN_SHULKER_BOX,
            Items.GREEN_SHULKER_BOX,
            Items.RED_SHULKER_BOX,
            Items.BLACK_SHULKER_BOX
    );

    private boolean isShulkerBox(Item item) {
        return shulkers.contains(item);
    }

    public void openMinecart() {
        if (mc.screen instanceof ContainerScreen) return;

        lookAtMinecart();
        Entity minecart = getMinecart();
        if (minecart == null) return;

        mc.gameMode.interact(mc.player, minecart, InteractionHand.MAIN_HAND);
    }

    public Entity getMinecart() {
        List<Entity> entities = mc.level.getEntities(null, new AABB(railLocation.subtract(1, 1, 1), railLocation.add(1, 1, 1)));
        for (Entity entity : entities) {
            if (entity.getType() == EntityType.CHEST_MINECART) {
                return entity;
            }
        }
        return null;
    }

    public void lookAtMinecart() {
        mc.player.lookAt(EntityAnchorArgument.Anchor.EYES, railLocation);
    }

    public void closeScreen() {
        mc.player.closeContainer();
    }

    public boolean getChestMinecart() {
        // Check if currently holding chest minecart
        if (mc.player.getInventory().getItem(mc.player.getInventory().selected).getItem() == Items.CHEST_MINECART) {
            return false;
        }

        // Check hotbar first
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getItem(i).getItem() == Items.CHEST_MINECART) {
                mc.player.getInventory().selected = i;
                closeScreen();
                return false;
            }
        }

        // Check rest of inventory
        for (int i = 9; i < 38; i++) {
            if (mc.player.getInventory().getItem(i).getItem() == Items.CHEST_MINECART) {
                mc.setScreen(new InventoryScreen(mc.player));
                if (!(mc.screen instanceof InventoryScreen screen)) return false;

                InventoryMenu menu = screen.getMenu();
                for (int b = 0; b < menu.slots.size(); b++) {
                    if (mc.player.getInventory().getItem(b).getItem() == Items.CHEST_MINECART) {
                        mc.gameMode.handleInventoryMouseClick(menu.containerId, b, 0, ClickType.PICKUP, mc.player);
                        mc.gameMode.handleInventoryMouseClick(menu.containerId, 36, 0, ClickType.PICKUP, mc.player);
                        mc.gameMode.handleInventoryMouseClick(menu.containerId, b, 0, ClickType.PICKUP, mc.player);
                        mc.player.getInventory().selected = 0;
                        closeScreen();
                        return false;
                    }
                }
                closeScreen();
                return false;
            }
        }

        closeScreen();

        // If still no chest minecart, change state and notify
        if (mc.player.getInventory().getItem(mc.player.getInventory().selected).getItem() != Items.CHEST_MINECART) {
            this.state = State.GET_MINECART;
            RusherHackAPI.getNotificationManager().error("No Minecarts");
            return true;
        }

        return false;
    }
}