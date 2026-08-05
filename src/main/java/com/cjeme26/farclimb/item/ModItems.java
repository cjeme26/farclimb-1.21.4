package com.cjeme26.farclimb.item;

import com.cjeme26.farclimb.FarClimb;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;

public final class ModItems {
    public static final Item CLIMBING_AXE = register("climbing_axe", new Item.Settings()
            .maxCount(1)
            .maxDamage(500));

    public static final RegistryKey<ItemGroup> FARCLIMB_GROUP_KEY = RegistryKey.of(
            RegistryKeys.ITEM_GROUP,
            FarClimb.id("farclimb")
    );

    private ModItems() {
    }

    private static Item register(String name, Item.Settings settings) {
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, FarClimb.id(name));
        Item item = new Item(settings.registryKey(key));
        return Registry.register(Registries.ITEM, key, item);
    }

    public static void initialize() {
        Registry.register(
                Registries.ITEM_GROUP,
                FARCLIMB_GROUP_KEY,
                FabricItemGroup.builder()
                        .icon(() -> new ItemStack(CLIMBING_AXE))
                        .displayName(Text.translatable("itemGroup.farclimb"))
                        .entries((displayContext, entries) -> entries.add(CLIMBING_AXE))
                        .build()
        );

        FarClimb.LOGGER.info("Registered FarClimb items and creative tab.");
    }
}
