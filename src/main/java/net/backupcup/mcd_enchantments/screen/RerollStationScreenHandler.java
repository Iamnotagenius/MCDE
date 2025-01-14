package net.backupcup.mcd_enchantments.screen;

import net.backupcup.mcd_enchantments.MCDEnchantments;
import net.backupcup.mcd_enchantments.util.EnchantmentSlots;
import net.backupcup.mcd_enchantments.util.EnchantmentUtils;
import net.backupcup.mcd_enchantments.util.Slots;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtElement;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;

public class RerollStationScreenHandler extends ScreenHandler {
    private final Inventory inventory;
    public Inventory getInventory() {
        return inventory;
    }

    public RerollStationScreenHandler(int syncId, PlayerInventory inventory) {
        this(syncId, inventory, new SimpleInventory(2));
    }

    public RerollStationScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory) {
        super(ModScreenHandlers.REROLL_STATION_SCREEN_HANDLER, syncId);

        checkSize(inventory, 1);
        this.inventory = inventory;
        inventory.onOpen(playerInventory.player);

        this.addSlot(new Slot(inventory, 0, 145, 33) {
            @Override
            public boolean canInsert(ItemStack stack) {
                var slots = EnchantmentSlots.fromItemStack(stack);
                return (slots != null &&
                        !EnchantmentTarget.TRIDENT.isAcceptableItem(stack.getItem()) &&
                        !EnchantmentTarget.DIGGER.isAcceptableItem(stack.getItem()) &&
                        !EnchantmentTarget.FISHING_ROD.isAcceptableItem(stack.getItem()) &&
                        EnchantmentUtils.getEnchantmentsForItem(stack).count() != slots.stream().flatMap(s -> s.choices().stream()).count());
            }

            @Override
            public int getMaxItemCount() {
                return 1;
            }
        });

        this.addSlot(new Slot(inventory, 1, 145, 52) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return (stack.getItem() == Items.LAPIS_LAZULI);
            }

            @Override
            public int getMaxItemCount() {
                return 64;
            }
        });

        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
    }

    @Override
    public boolean onButtonClick(PlayerEntity player, int id) {
        ItemStack itemStack = inventory.getStack(0);
        ItemStack lapisLazuliStack = inventory.getStack(1);
        EnchantmentSlots slots = EnchantmentSlots.fromItemStack(itemStack);

        var slotsSize = Slots.values().length;
        var clickedSlot = slots.getSlot(Slots.values()[id / slotsSize]).get();
        Slots toChange;
        short level = 1;
        Identifier enchantmentId;
        var newEnchantment = EnchantmentUtils.generateEnchantment(itemStack, slots);
        if (newEnchantment.isEmpty()) {
            return super.onButtonClick(player, id);
        }

        if (clickedSlot.getChosen().isPresent()) {
            var chosen = clickedSlot.getChosen().get();
            enchantmentId = chosen.getEnchantment();
            level = chosen.getLevel();

            if (!canReroll(player, enchantmentId, level)) {
                return super.onButtonClick(player, id);
            }
            clickedSlot.clearChoice();
            toChange = chosen.getSlot();
            var list = itemStack.getNbt().getList("Enchantments", NbtElement.COMPOUND_TYPE);
            for (int i = 0; i < list.size(); i++) {
                var compound = list.getCompound(i);
                if (enchantmentId.toString().equals(compound.getString("id"))) {
                    list.remove(i);
                }
            }

        } else {
            toChange = Slots.values()[id % slotsSize];
            enchantmentId = clickedSlot.getChoice(toChange).get();

            if (!canReroll(player, enchantmentId, level)) {
                return super.onButtonClick(player, id);
            }
        }

        clickedSlot.changeEnchantment(toChange, newEnchantment.get());
        slots.updateItemStack(itemStack);
        MCDEnchantments.LOGGER.info("Decrementing lapis by {} items", EnchantmentUtils.getCost(enchantmentId, level));
        lapisLazuliStack.decrement(EnchantmentUtils.getCost(enchantmentId, level));

        return super.onButtonClick(player, id);
    }

    public boolean canReroll(PlayerEntity player, Identifier enchantmentId, short level) {
        ItemStack lapisLazuliStack = inventory.getStack(1);
        if (!player.isCreative()) {
            return lapisLazuliStack.getCount() >= EnchantmentUtils.getCost(enchantmentId, level);
        } else {
            return true;
        }
    }

    @Override
    public ItemStack transferSlot(PlayerEntity player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);
        if (slot != null && slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();
            if (invSlot < this.inventory.size()) {
                if (!this.insertItem(originalStack, this.inventory.size(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(originalStack, 0, this.inventory.size(), false)) {
                return ItemStack.EMPTY;
            }

            if (originalStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }

        return newStack;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    private void addPlayerInventory(PlayerInventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 10 + l * 18, 84 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(PlayerInventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 10 + i * 18, 142));
        }
    }
}
