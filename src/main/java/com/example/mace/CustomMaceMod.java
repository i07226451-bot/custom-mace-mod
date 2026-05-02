
package com.example.mace;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CustomMaceMod implements ModInitializer {

    public static final String MOD_ID = "custommace";

    public static final Item CUSTOM_MACE = new CustomMace(new FabricItemSettings().maxCount(1));

    public static final Enchantment SKY_FURY = new SkyFuryEnchantment();

    @Override
    public void onInitialize() {
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "mace"), CUSTOM_MACE);
        Registry.register(Registries.ENCHANTMENT, new Identifier(MOD_ID, "sky_fury"), SKY_FURY);

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!(entity instanceof LivingEntity target)) return ActionResult.PASS;

            ItemStack stack = player.getStackInHand(hand);
            if (stack.getItem() != CUSTOM_MACE) return ActionResult.PASS;

            if (!world.isClient) {
                applyMaceEffect(player, target, (ServerWorld) world, stack);
            }

            return ActionResult.SUCCESS;
        });
    }

    private static void applyMaceEffect(PlayerEntity player, LivingEntity target, ServerWorld world, ItemStack stack) {

        if (player.getItemCooldownManager().isCoolingDown(CUSTOM_MACE)) return;
        player.getItemCooldownManager().set(CUSTOM_MACE, 20);

        double fallDistance = player.fallDistance;
        float damage = (float) (fallDistance * 3.0);

        target.damage(DamageSource.player(player), damage);

        // Break armor
        target.getArmorItems().forEach(armor -> {
            armor.setDamage(armor.getMaxDamage());
        });

        // Lightning
        if (EnchantmentHelper.getLevel(SKY_FURY, stack) > 0) {
            LightningEntity lightning = new LightningEntity(world, target.getX(), target.getY(), target.getZ(), false);
            world.spawnEntity(lightning);
        }

        // Break blocks 3x3
        BlockPos center = target.getBlockPos();
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos pos = center.add(x, -1, z);
                BlockState state = world.getBlockState(pos);
                if (!state.isAir()) {
                    world.breakBlock(pos, true);
                }
            }
        }

        world.playSound(null, target.getBlockPos(), SoundEvents.ENTITY_GENERIC_EXPLODE, player.getSoundCategory(), 1.0f, 1.0f);

        player.fallDistance = 0;
    }

    public static class SkyFuryEnchantment extends Enchantment {
        protected SkyFuryEnchantment() {
            super(Rarity.VERY_RARE, EnchantmentTarget.WEAPON, new net.minecraft.entity.EquipmentSlot[]{net.minecraft.entity.EquipmentSlot.MAINHAND});
        }

        @Override
        public int getMaxLevel() {
            return 3;
        }
    }

    public static class CustomMace extends Item {

        public CustomMace(Settings settings) {
            super(settings);
        }

        @Override
        public boolean isDamageable() {
            return false;
        }

        @Override
        public void appendTooltip(ItemStack stack, World world, java.util.List<Text> tooltip, net.minecraft.client.item.TooltipContext context) {
            tooltip.add(Text.literal("§6God Mace"));
            tooltip.add(Text.literal("3x fall damage").formatted(Formatting.RED));
            tooltip.add(Text.literal("Breaks armor").formatted(Formatting.DARK_PURPLE));
            tooltip.add(Text.literal("⚡ Sky Fury enchant").formatted(Formatting.AQUA));
        }
    }
}
