package de.kybe.KybesUtils.commands;

import de.kybe.KybesUtils.modules.MinecartDupe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.phys.HitResult;
import org.rusherhack.client.api.feature.command.Command;
import org.rusherhack.core.command.annotations.CommandExecutor;

public class MinecartCommands extends Command {
    public MinecartCommands() {
        super("minecartDupe", "minecart dupe setup");
    }

    @CommandExecutor
    private String example() {
        return "use an subcommand";
    }

    @CommandExecutor(subCommand = "rails")
    public String setupRails() {
        if (mc.level == null) return "not in a level";
        if (mc.hitResult == null) return "look at the rails for the minecart";
        if (mc.hitResult.getType() != HitResult.Type.BLOCK) return "look at the rails for the minecart\"";

        MinecartDupe.railLocation = mc.hitResult.getLocation();
        MinecartDupe.railBlock = BlockPos.containing(mc.hitResult.getLocation());
        return "Rails Set";
    }

    @CommandExecutor(subCommand = "minecart")
    public String setupMinecart() {
        if (mc.level == null) return "not in a level";
        if (mc.hitResult == null) return "look at the chest";
        if (mc.hitResult.getType() != HitResult.Type.BLOCK) return "look at the chest";

        BlockPos blockPos = BlockPos.containing(mc.hitResult.getLocation());
        Block block = mc.level.getBlockState(blockPos).getBlock();
        if (!(block instanceof ShulkerBoxBlock || block instanceof ChestBlock)) return "look at the chest or shulker";

        MinecartDupe.minecartChestLocation = mc.hitResult.getLocation();
        MinecartDupe.minecartChestBlock = blockPos;

        return "Minecart Chest Set";
    }

    @CommandExecutor(subCommand = "direction")
    public String setupMinecartDirection() {
        if (mc.level == null) return "not in level";
        if (mc.player == null) return "player is null";

        Direction dir = mc.player.getDirection();
        switch (dir) {
            case UP, DOWN -> {
                return "Dont look " + dir.getName();
            }
            default -> {
                MinecartDupe.minecartDirection = dir;
                return "Minecart Direction set to " + dir.getName();
            }
        }
    }
}