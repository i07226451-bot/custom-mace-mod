package com.example.mace;

import net.fabricmc.api.ModInitializer;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class CustomMaceMod implements ModInitializer {

    public static final String MOD_ID = "custommace";

    public static final Item TEST_ITEM = new Item(new Item.Settings());

    @Override
    public void onInitialize() {
        Registry.register(
                Registries.ITEM,
                Identifier.of(MOD_ID, "test_item"),
                TEST_ITEM
        );

        System.out.println("CUSTOM MACE MOD LOADED!");
    }
}
