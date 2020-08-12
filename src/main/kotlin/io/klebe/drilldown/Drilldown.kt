package io.klebe.drilldown

import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.SidedProxy
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import org.apache.logging.log4j.Logger

@Mod(
        modid = Drilldown.MODID,
        name = Drilldown.NAME,
        version = Drilldown.VERSION,
        dependencies = Drilldown.DEPENDENCIES,
        modLanguageAdapter = "net.shadowfacts.forgelin.KotlinAdapter")
object Drilldown {
    const val MODID = "drilldown"
    const val NAME = "Drilldown"
    const val VERSION = "0.2.0"
    const val DEPENDENCIES = "required-after:forgelin@[1.8.4,);"

    private lateinit var logger: Logger

    @SidedProxy(serverSide = "io.klebe.drilldown.CommonProxy", clientSide = "io.klebe.drilldown.ClientProxy")
    private lateinit var private_proxy: CommonProxy

    val log
        get() = logger

    val proxy
        get() = private_proxy

    @Mod.EventHandler fun preInit(e: FMLPreInitializationEvent) {
        this.logger = e.modLog
        MinecraftForge.EVENT_BUS.register(this.proxy)
        this.proxy.preInit(e)
    }

    @Mod.EventHandler fun init(e: FMLInitializationEvent) = this.proxy.init(e)
    @Mod.EventHandler fun postInit(e: FMLPostInitializationEvent) = this.proxy.postInit(e)
}