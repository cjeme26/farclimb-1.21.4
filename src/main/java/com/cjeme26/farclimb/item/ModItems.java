package com.cjeme26.farclimb.item;

import com.cjeme26.farclimb.FarClimb;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

public final class ModItems {
    public static final Item CLIMBING_AXE = register("climbing_axe", new Item.Settings()
            .maxCount(1)
            .maxDamage(500));

    private ModItems() {
    }

    private static Item register(String name, Item.Settings settings) {
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, FarClimb.id(name));
        Item item = new Item(settings.registryKey(key));
        return Registry.register(Registries.ITEM, key, item);
    }

    public static void initialize() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS)
                .register(entries -> entries.add(CLIMBING_AXE));

        FarClimb.LOGGER.info("Registered FarClimb items.");
    }
}
