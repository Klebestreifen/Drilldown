package io.klebe.drilldown.blocks

import io.klebe.drilldown.Drilldown
import io.klebe.drilldown.forEach
import net.minecraft.block.Block
import net.minecraft.block.ITileEntityProvider
import net.minecraft.block.SoundType
import net.minecraft.block.material.Material
import net.minecraft.block.properties.PropertyBool
import net.minecraft.block.state.BlockStateContainer
import net.minecraft.block.state.IBlockState
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.item.EntityItem
import net.minecraft.inventory.InventoryHelper
import net.minecraft.item.Item
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.*
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.energy.CapabilityEnergy
import net.minecraftforge.energy.EnergyStorage
import net.minecraftforge.fml.common.registry.GameRegistry
import net.minecraftforge.items.CapabilityItemHandler
import net.minecraftforge.items.ItemStackHandler
import java.util.*

object BlockDrill: Block(Material.ANVIL), ITileEntityProvider {

    var onProp: PropertyBool? = null
        get(){
            if(field == null)
                field = PropertyBool.create("on")
            return field
        }
        private set(value){
            field = value
        }

    val BLOCK_NAME = "drill"

    init{
        this.setUnlocalizedName(BlockDrill.BLOCK_NAME)
        this.setRegistryName(ResourceLocation(Drilldown.MODID, BlockDrill.BLOCK_NAME))
        this.setCreativeTab(CreativeTabs.REDSTONE)
        this.translucent = false
        this.setSoundType(SoundType.METAL)
        this.setHardness(2f)
        this.setDefaultState(this.blockState.getBaseState().withProperty(onProp, false))
        GameRegistry.registerTileEntity(BlockDrill.TileEntityDrill::class.java, ResourceLocation(Drilldown.MODID, BlockDrill.BLOCK_NAME))
    }

    fun registerModels(){
        Drilldown.proxy.registerItemRenderer(Item.getItemFromBlock(this), 0);
    }

    override fun breakBlock(worldIn: World, pos: BlockPos, state: IBlockState) {
        val tileentity = worldIn.getTileEntity(pos)

        if(tileentity is TileEntityDrill) tileentity.dropInventory()

        super.breakBlock(worldIn, pos, state)
    }

    override fun quantityDropped(random: Random): Int = 1
    override fun quantityDropped(state: IBlockState, fortune: Int, random: Random): Int = 1
    override fun getItemDropped(state: IBlockState, rand: Random, fortune: Int): Item = BlockDrill.ItemBlockDrill
    override fun createNewTileEntity(worldIn: World, meta: Int) = BlockDrill.TileEntityDrill()
    override fun getStateFromMeta(meta: Int) = this.defaultState.withProperty(onProp, meta != 0)
    override fun getMetaFromState(state: IBlockState) = if(state.getValue<Boolean>(onProp)) 1 else 0
    override fun createBlockState(): BlockStateContainer = BlockStateContainer(this, onProp)
    override fun isSideSolid(base_state: IBlockState, world: IBlockAccess, pos: BlockPos, side: EnumFacing) = true
    override fun getRenderType(state: IBlockState) = EnumBlockRenderType.MODEL

    object ItemBlockDrill : ItemBlock(BlockDrill){
        init {
            this.setRegistryName(BlockDrill.registryName)
        }
    }

    class TileEntityDrill : TileEntity(), ITickable {
        val inventorySize = 9

        // NBT Vars
        val itemStackHandler = ItemStackHandler(9)
        var energy: DrillEnergyStorage = DrillEnergyStorage()

        companion object{
            const val INVENTORY_TAG_NAME = "Inventory"
            const val ENERGY_TAG_NAME = "Energy"
            const val ENERGY_PER_OPERATION = 400
        }


        fun isInventoryEmpty(): Boolean{
            for (i in 0 until (itemStackHandler.slots-1)){
                if (!itemStackHandler.getStackInSlot(i).isEmpty) return false
            }
            return true
        }

        fun dropInventory(){
            itemStackHandler.forEach {
                InventoryHelper.spawnItemStack(world, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), it.itemStack)
                it.itemStack.shrink(it.itemStack.count)
            }
        }

        private fun mineBlock() = mineBlock(pos.down())

        private fun mineBlock(pos: BlockPos){
            val state = world.getBlockState(pos)
            if(pos.y <= 1) return
            else if(state.block.isAir(state, world, pos)) mineBlock(pos.down())
            else if(state.material.isLiquid) mineBlock(pos.down())
            else if(state.getBlockHardness(world, pos) < 0f) return
            world.destroyBlock(pos, true)
        }

        private fun getAirUnder(): Int = getAirUnder(pos.down(), 0)

        private fun getAirUnder(pos: BlockPos, found: Int): Int{
            var found = found
            val state = world.getBlockState(pos)
            if(pos.y <= 1) return found
            else if(state.block.isAir(state, world, pos) || state.material.isLiquid){
                found = getAirUnder(pos.down(), found + 1)
            }
            return found
        }

        private fun getSpaceUnder(): AxisAlignedBB{
            val airUnder = getAirUnder()
            return AxisAlignedBB(pos.x.toDouble()-0.5, pos.y.toDouble()-airUnder.toDouble(), pos.z.toDouble()-0.5, pos.x.toDouble()+1.5, pos.y.toDouble(), pos.z.toDouble()+1.5)
        }

        fun putStackInInventoryAllSlots(stack: ItemStack): ItemStack {
            var stack = stack

            itemStackHandler.forEach {
                stack = itemStackHandler.insertItem(it.slot, stack, false)
            }

            return stack
        }

        private fun vacuum(){
            val itemEntities = world.getEntitiesWithinAABB<EntityItem>(EntityItem::class.java, getSpaceUnder(), EntitySelectors.IS_ALIVE)
            for(entity in itemEntities){
                val endStack = putStackInInventoryAllSlots(entity.item)
                entity.item = endStack
            }
        }

        fun hasEnergyToWork() = energy.energyStored > ENERGY_PER_OPERATION

        override fun update() {
            if (!world.isRemote) {
                if (hasEnergyToWork() && world.totalWorldTime % 20 == 0L) {
                    mineBlock()
                    energy.energyStored -= ENERGY_PER_OPERATION
                }
                if (world.totalWorldTime % 20 == 2L) {
                    vacuum()
                }
                if (world.totalWorldTime % 20 == 0L) {
                    if (hasEnergyToWork() && !world.getBlockState(pos).getValue(onProp!!)) {
                        this.markDirty()
                        world.setBlockState(pos, defaultState.withProperty(onProp, true))
                    } else if (!hasEnergyToWork() && world.getBlockState(pos).getValue(onProp!!)) {
                        this.markDirty()
                        world.setBlockState(pos, defaultState.withProperty(onProp, false))
                    }
                }
            }
        }

        override fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean {
            val facingFlag = (facing != null) && (facing != EnumFacing.DOWN)

            return when (capability) {
                CapabilityItemHandler.ITEM_HANDLER_CAPABILITY -> facingFlag
                CapabilityEnergy.ENERGY -> facingFlag
                else -> super.hasCapability(capability, facing)
            }
        }

        override fun <T> getCapability(capability: Capability<T>, facing: EnumFacing?): T? {
            return when (capability){
                CapabilityItemHandler.ITEM_HANDLER_CAPABILITY -> itemStackHandler as T
                CapabilityEnergy.ENERGY -> energy as T
                else -> super.getCapability(capability, facing)
            }
        }

        override fun writeToNBT(compound: NBTTagCompound): NBTTagCompound {
            super.writeToNBT(compound)

            compound.setTag(INVENTORY_TAG_NAME, itemStackHandler.serializeNBT());
            compound.setInteger(ENERGY_TAG_NAME, energy.energyStored)

            return compound
        }

        override fun readFromNBT(compound: NBTTagCompound) {
            super.readFromNBT(compound)

            itemStackHandler.deserializeNBT(compound.getCompoundTag(INVENTORY_TAG_NAME))
            energy.energyStored = compound.getInteger(ENERGY_TAG_NAME)
        }

        override fun shouldRefresh(world: World?, pos: BlockPos?, oldState: IBlockState, newState: IBlockState) = oldState.block !== newState.block

        class DrillEnergyStorage : EnergyStorage(10000){
            fun setEnergyStored(energy: Int){
                this.energy = energy
            }
        }
    }
}