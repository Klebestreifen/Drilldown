package io.klebe.drilldown

import net.minecraft.item.ItemStack
import net.minecraftforge.items.ItemStackHandler

data class ItemStackHandlerForEachObject(val itemStack: ItemStack, val slot: Int)

class ItemStackHandlerIterator(private val ish: ItemStackHandler) : Iterator<ItemStack>{
    var currentSlot = 0
        get() = field
        private set(value) {
            field = value
        }

    override fun hasNext(): Boolean = currentSlot < ish.slots
    override fun next(): ItemStack = ish[currentSlot++]
}

operator fun ItemStackHandler.get(slot: Int) = this.getStackInSlot(slot)
operator fun ItemStackHandler.iterator() = ItemStackHandlerIterator(this)

fun ItemStackHandler.forEach(action: (it: ItemStackHandlerForEachObject) -> Unit){
    val iterator = this.iterator()
    for(iStack in iterator) action(ItemStackHandlerForEachObject(iStack, iterator.currentSlot-1))
}