// quick_insert.sc
// Punch (left-click) a container with an item to insert one item inside.
// Works with any block that has slots: chests, furnaces, modded machines, etc.
// Empty hand = break normally. Full inventory = break normally.

__config() -> {
    'scope' -> 'global',
    'stay_loaded' -> true,
    'strict' -> false,
};

__on_player_clicks_block(player, block, face) -> (
    held = query(player, 'holds');
    if (held == null, return(null));

    [item_name, count, nbt] = held;

    size = inventory_size(block);
    if (size == null, return(null));

    inserted = _insert_item(block, size, item_name, nbt);

    if (inserted,
        slot = query(player, 'selected_slot');
        inventory_set(player, slot, count - 1);
        return('cancel');
    ,
        return(null);
    );
);

_insert_item(block, size, item_name, nbt) -> (
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
