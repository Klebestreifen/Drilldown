package io.klebe.drilldown

import net.minecraftforge.common.config.Configuration
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import java.io.File

object ConfigHandler {
    private var config: Configuration? = null

    var energyCapacity = 10000
        get() = field
        private set(value) {
            field = value
        }

    var energyUsage = 400
        get() = field
        private set(value) {
            field = value
        }

    var delay = 20
        get() = field
        private set(value) {
            field = value
        }

    fun load(event: FMLPreInitializationEvent){
        if(config == null){
            event.modConfigurationDirectory.mkdirs()
            val configFile = File(event.modConfigurationDirectory.path + "/" + Drilldown.MODID + ".cfg")
            config = Configuration(configFile)
        }

        config?.let {
            Drilldown.log.info("Load Config...")
            var category = "energy"
            it.addCustomCategoryComment(category, "In this area settings are made about the behaviour with energy.")
            energyCapacity = it.getInt("energyCapacity", category, energyCapacity, 0, Int.MAX_VALUE, "The size of the energy storage of the drill.")
            energyUsage = it.getInt("energyUsage", category, energyUsage, 0, Int.MAX_VALUE, "The energy consumed per block mined.")

            category = "behaviour"
            it.addCustomCategoryComment(category, "Here you can make various settings about the behaviour of the drill.")
            delay = it.getInt("delay", category, delay, 1, Int.MAX_VALUE, "The number of ticks to wait for each block mined.")

            it.save()
        }
    }
}