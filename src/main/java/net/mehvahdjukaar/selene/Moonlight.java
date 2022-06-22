package net.mehvahdjukaar.selene;

import net.mehvahdjukaar.selene.block_set.BlockSetManager;
import net.mehvahdjukaar.selene.block_set.leaves.LeavesTypeRegistry;
import net.mehvahdjukaar.selene.block_set.wood.WoodTypeRegistry;
import net.mehvahdjukaar.selene.builtincompat.CompatWoodTypes;
import net.mehvahdjukaar.selene.fluids.SoftFluid;
import net.mehvahdjukaar.selene.fluids.SoftFluidRegistryOld;
import net.mehvahdjukaar.selene.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.selene.map.MapDecorationRegistry;
import net.mehvahdjukaar.selene.misc.ModCriteriaTriggers;
import net.mehvahdjukaar.selene.network.ClientBoundSyncFluidsPacket;
import net.mehvahdjukaar.selene.network.ClientBoundSyncMapDecorationTypesPacket;
import net.mehvahdjukaar.selene.network.NetworkHandler;
import net.mehvahdjukaar.selene.villager_ai.VillagerAIManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

@Mod(Moonlight.MOD_ID)
public class Moonlight {

    public static final String MOD_ID = "moonlight";

    public static final Logger LOGGER = LogManager.getLogger();

    public Moonlight() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        VillagerAIManager.SCHEDULES.register(bus);
        SoftFluidRegistry.DEFERRED_REGISTER.register(bus);
        bus.addListener(Moonlight::init);
        MinecraftForge.EVENT_BUS.register(this);
        BlockSetManager.registerBlockSetDefinition(new WoodTypeRegistry());
        BlockSetManager.registerBlockSetDefinition(new LeavesTypeRegistry());
        CompatWoodTypes.init();
    }

    public static ResourceLocation res(String replace) {
        return new ResourceLocation(MOD_ID,replace);
    }

    @SubscribeEvent
    public void addDataPackRegistries(NewRegistryEvent event){
        RegistryBuilder<SoftFluid> builder = new RegistryBuilder<>();
    }

    @SubscribeEvent
    public void addJsonListener(final AddReloadListenerEvent event) {
        event.addListener(SoftFluidRegistryOld.INSTANCE);
        event.addListener(MapDecorationRegistry.DATA_DRIVEN_REGISTRY);
    }

    @SubscribeEvent
    public void onDataLoad(OnDatapackSyncEvent event) {
        // if we're on the server, send syncing packets
        if (ServerLifecycleHooks.getCurrentServer() != null) {
            List<ServerPlayer> playerList = event.getPlayer() != null ? List.of(event.getPlayer()) : event.getPlayerList().getPlayers();
            playerList.forEach(serverPlayer -> {

                NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer),
                        new ClientBoundSyncFluidsPacket(SoftFluidRegistryOld.getValues()));
                NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer),
                        new ClientBoundSyncMapDecorationTypesPacket(MapDecorationRegistry.DATA_DRIVEN_REGISTRY.getTypes()));
            });
        }
    }

    public static void init(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            NetworkHandler.registerMessages();
            ModCriteriaTriggers.init();
            VillagerAIManager.init();
        });
    }

}