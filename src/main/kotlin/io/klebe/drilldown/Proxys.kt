package io.klebe.drilldown

import io.klebe.drilldown.blocks.BlockDrill
import net.minecraft.block.Block
import net.minecraft.client.renderer.block.model.ModelResourceLocation
import net.minecraft.item.Item
import net.minecraftforge.client.event.ModelRegistryEvent
import net.minecraftforge.client.model.ModelLoader
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

open class CommonProxy{
    open fun preInit(e: FMLPreInitializationEvent) {
        Drilldown.log.info("preinit")
    }

    open fun init(e: FMLInitializationEvent) {
        Drilldown.log.info("init")
    }

    open fun postInit(e: FMLPostInitializationEvent) {
        Drilldown.log.info("postinit")
    }

    open fun registerItemRenderer(item: Item, meta: Int) {
    }

    @SubscribeEvent
    open fun registerBlocks(event: RegistryEvent.Register<Block> ) {
        event.registry.registerAll(BlockDrill)
    }

    @SubscribeEvent
    open fun registerItems(event: RegistryEvent.Register<Item>){
        event.registry.registerAll(BlockDrill.ItemBlockDrill)
    }
}

@SideOnly(Side.CLIENT)
open class ClientProxy : CommonProxy() {
    override fun preInit(e: FMLPreInitializationEvent) {
        super.preInit(e)
    }

    override fun init(e: FMLInitializationEvent) {
        super.init(e)
    }

    override fun postInit(e: FMLPostInitializationEvent) {
        super.postInit(e)
    }

    @SubscribeEvent
    open fun onModelRegister(event: ModelRegistryEvent?) {
        BlockDrill.registerModels()
    }

    override fun registerItemRenderer(item: Item, meta: Int) {
        super.registerItemRenderer(item, meta)
        ModelLoader.setCustomModelResourceLocation(
                item,
                meta,
                ModelResourceLocation(item.registryName, "inventory"))
    }
}