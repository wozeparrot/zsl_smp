// quick_insert.sc
// Punch (left-click) a container with an item to insert one item inside.
// Works with any block that has slots: chests, furnaces, modded machines, etc.
// Empty hand = break normally. Holding a tool = break normally. Full inventory = break normally.

__config() -> {
    'scope' -> 'global',
    'stay_loaded' -> true,
    'strict' -> false,
};

__on_player_clicks_block(player, block, face) -> (
    held = query(player, 'holds');
    if (held == null, return(null));

    [item_name, count, nbt] = held;

    if (_is_tool(item_name), return(null));

    size = inventory_size(block);
    if (size == null, return(null));

    block_name = str(block);
    inserted = _insert_item(block, size, item_name, nbt, block_name);

    if (inserted,
        slot = query(player, 'selected_slot');
        inventory_set(player, slot, count - 1);
        return('cancel');
    ,
        return(null);
    );
);

_is_tool(item_name) -> (
    if (item_tags(item_name, 'pickaxes') == true, return(true));
    if (item_tags(item_name, 'axes') == true, return(true));
    if (item_tags(item_name, 'shovels') == true, return(true));
    if (item_tags(item_name, 'hoes') == true, return(true));
    if (item_tags(item_name, 'swords') == true, return(true));
    return(item_name == 'shears' ||
           item_name == 'flint_and_steel' ||
           item_name == 'fishing_rod' ||
           item_name == 'bow' ||
           item_name == 'crossbow' ||
           item_name == 'shield' ||
           item_name == 'trident' ||
           item_name == 'spyglass' ||
           item_name == 'brush' ||
           item_name == 'compass' ||
           item_name == 'recovery_compass' ||
           item_name == 'clock' ||
           item_name == 'goat_horn' ||
           item_name == 'lead' ||
           item_name == 'name_tag' ||
           item_name == 'writable_book' ||
           item_name == 'written_book' ||
           item_name == 'enchanted_book' ||
           item_name == 'bundle' ||
           item_name == 'saddle' ||
           item_name == 'horse_armor' ||
           item_name == 'totem_of_undying');
);

_insert_item(block, size, item_name, nbt, block_name) -> (
    if (block_name == 'furnace' || block_name == 'blast_furnace' || block_name == 'smoker',
        return(_insert_furnace(block, item_name, nbt));
    ,
        return(_insert_generic(block, size, item_name, nbt));
    );
);

_insert_furnace(block, item_name, nbt) -> (
    // Slot 0 = input, Slot 1 = fuel, Slot 2 = output (never insert here)

    // First, try to stack with existing items in slots 0 and 1
    i = 0;
    while (i < 2,
        slot_item = inventory_get(block, i);
        if (slot_item,
            [s_name, s_count, s_nbt] = slot_item;
            if (s_name == item_name && s_count < 64 && s_nbt == nbt,
                inventory_set(block, i, s_count + 1);
                return(true);
            );
        );
        i += 1;
    );

    // No matching stack found. Determine target slot.
    if (_is_fuel(item_name),
        // Fuel item. Insert into fuel slot (1) if empty.
        if (inventory_get(block, 1) == null,
            inventory_set(block, 1, 1, item_name, nbt);
            return(true);
        ,
            return(false);
        );
    ,
        // Input item. Insert into input slot (0) if empty.
        if (inventory_get(block, 0) == null,
            inventory_set(block, 0, 1, item_name, nbt);
            return(true);
        ,
            return(false);
        );
    );
);

_is_fuel(item_name) -> (
    if (item_tags(item_name, 'logs') == true, return(true));
    if (item_tags(item_name, 'planks') == true, return(true));
    if (item_tags(item_name, 'saplings') == true, return(true));
    if (item_tags(item_name, 'boats') == true, return(true));

    return(item_name == 'coal' ||
           item_name == 'charcoal' ||
           item_name == 'coal_block' ||
           item_name == 'stick' ||
           item_name == 'blaze_rod' ||
           item_name == 'lava_bucket' ||
           item_name == 'dried_kelp_block' ||
           item_name == 'bamboo' ||
           item_name == 'dead_bush');
);

_insert_generic(block, size, item_name, nbt) -> (
    // First pass: try to stack with existing items
    i = 0;
    while (i < size,
        slot_item = inventory_get(block, i);
        if (slot_item,
            [s_name, s_count, s_nbt] = slot_item;
            if (s_name == item_name && s_count < 64 && s_nbt == nbt,
                inventory_set(block, i, s_count + 1);
                return(true);
            );
        );
        i += 1;
    );

    // Second pass: find first empty slot
    i = 0;
    while (i < size,
        slot_item = inventory_get(block, i);
        if (slot_item == null,
            inventory_set(block, i, 1, item_name, nbt);
            return(true);
        );
        i += 1;
    );

    return(false);
);
