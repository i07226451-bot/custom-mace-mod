package com.example.mace;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

public class CustomMaceMod implements ModInitializer {

    public static final String MOD_ID = "custommace";

    public static final Item CUSTOM_MACE = new Item(new Item.Settings().maxCount(1));

    @Override
    public void onInitialize() {
        Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "mace"), CUSTOM_MACE);

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!(entity instanceof LivingEntity target)) return ActionResult.PASS;

            ItemStack stack = player.getStackInHand(hand);
            if (stack.getItem() != CUSTOM_MACE) return ActionResult.PASS;

            if (!world.isClient) {
                applyEffect(player, target, (ServerWorld) world);
            }

            return ActionResult.SUCCESS;
        });
    }

    private void applyEffect(PlayerEntity player, LivingEntity target, ServerWorld world) {

        float damage = player.fallDistance * 3f;

        target.damage(world.getDamageSources().playerAttack(player), damage);

        // ⚡ молния
        LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(world);
        if (lightning != null) {
            lightning.refreshPositionAfterTeleport(target.getX(), target.getY(), target.getZ());
            world.spawnEntity(lightning);
        }

        // 💥 ломаем 3x3
        BlockPos center = target.getBlockPos();
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos pos = center.add(x, -1, z);
                world.breakBlock(pos, true);
            }
        }

        player.fallDistance = 0;
    }
}
