package com.feywild.quest_giver;

import com.feywild.quest_giver.entity.GuildMasterProfession;
import com.feywild.quest_giver.entity.ModEntityTypes;
import com.feywild.quest_giver.entity.ModPoiTypes;
import com.feywild.quest_giver.entity.QuestVillager;
import com.feywild.quest_giver.network.QuestGiverNetwork;
import com.feywild.quest_giver.quest.QuestManager;
import com.feywild.quest_giver.quest.player.CapabilityQuests;
import com.feywild.quest_giver.quest.reward.CommandReward;
import com.feywild.quest_giver.quest.reward.ItemReward;
import com.feywild.quest_giver.quest.reward.RewardTypes;
import com.feywild.quest_giver.quest.task.*;
import com.feywild.quest_giver.renderer.ExclamationMarkerRenderer;
import com.feywild.quest_giver.util.JigsawHelper;
import io.github.noeppi_noeppi.libx.mod.registration.ModXRegistration;
import io.github.noeppi_noeppi.libx.mod.registration.RegistrationBuilder;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.VillagerRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderNameplateEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;


import javax.annotation.Nonnull;



@Mod("quest_giver")
public final class QuestGiverMod extends ModXRegistration
{

    private static QuestGiverMod instance;
    private static QuestGiverNetwork network;

    public QuestGiverMod() {

        instance = this;
        network = new QuestGiverNetwork(this);

        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        bus.addListener(CapabilityQuests::register);
        bus.addListener(this::entityAttributes);

        MinecraftForge.EVENT_BUS.addListener(this::reloadData);

        MinecraftForge.EVENT_BUS.addGenericListener(Entity.class, CapabilityQuests::attachPlayerCaps);
        MinecraftForge.EVENT_BUS.addListener(CapabilityQuests::playerCopy);

        MinecraftForge.EVENT_BUS.register(new EventListener());
        GuildMasterProfession.PROFESSION.register(bus);
        ModPoiTypes.POI_TYPES.register(bus);


        // Quest task & reward types. Not in setup as they are required for datagen.
        TaskTypes.register(new ResourceLocation(this.modid, "craft"), CraftTask.INSTANCE);
        TaskTypes.register(new ResourceLocation(this.modid, "gift"), GiftTask.INSTANCE);
        TaskTypes.register(new ResourceLocation(this.modid, "item"), ItemTask.INSTANCE);
        TaskTypes.register(new ResourceLocation(this.modid, "kill"), KillTask.INSTANCE);
        TaskTypes.register(new ResourceLocation(this.modid, "biome"), BiomeTask.INSTANCE);
        TaskTypes.register(new ResourceLocation(this.modid, "structure"), StructureTask.INSTANCE);
        TaskTypes.register(new ResourceLocation(this.modid, "special"), SpecialTask.INSTANCE);

        RewardTypes.register(new ResourceLocation(this.modid, "item"), ItemReward.INSTANCE);
        RewardTypes.register(new ResourceLocation(this.modid, "command"), CommandReward.INSTANCE);

        MinecraftForge.EVENT_BUS.register(this);
    }

    @Nonnull
    public static QuestGiverMod getInstance(){
        return instance;
    }

    @Nonnull
    public static QuestGiverNetwork getNetwork() {
        return network;
    }

    @Override
    protected void initRegistration(RegistrationBuilder builder) {
        builder.setVersion(1);
    }

    @Override
    protected void setup(final FMLCommonSetupEvent event)
    {
        event.enqueueWork(() -> {
            SpawnPlacements.register(ModEntityTypes.questVillager, SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, QuestVillager::canSpawn);
            ModPoiTypes.register();
        });
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected void clientSetup(FMLClientSetupEvent event) {
        EntityRenderers.register(ModEntityTypes.questVillager, VillagerRenderer::new);
        MinecraftForge.EVENT_BUS.register(QuestGiverRenderer.class);
    }

    @SubscribeEvent
    public void reloadData(AddReloadListenerEvent event) {
        event.addListener(QuestManager.createReloadListener());
    }

    private void entityAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntityTypes.questVillager, QuestVillager.createAttributes().build());
    }

    @SubscribeEvent
    public void onServerAboutToStartEvent(ServerAboutToStartEvent event){
        JigsawHelper.registerJigsaw(event.getServer(), new ResourceLocation("minecraft:village/desert/houses"),
                new ResourceLocation("quest_giver:village/desert/houses/guild_house"), 10);
        JigsawHelper.registerJigsaw(event.getServer(), new ResourceLocation("minecraft:village/plains/houses"),
                new ResourceLocation("quest_giver:village/plains/houses/guild_house"), 10);
        JigsawHelper.registerJigsaw(event.getServer(), new ResourceLocation("minecraft:village/savanna/houses"),
                new ResourceLocation("quest_giver:village/savanna/houses/guild_house"), 10);
        JigsawHelper.registerJigsaw(event.getServer(), new ResourceLocation("minecraft:village/snowy/houses"),
                new ResourceLocation("quest_giver:village/snowy/houses/guild_house"), 10);
        JigsawHelper.registerJigsaw(event.getServer(), new ResourceLocation("minecraft:village/taiga/houses"),
                new ResourceLocation("quest_giver:village/taiga/houses/guild_house"), 10);

        JigsawHelper.registerJigsaw(event.getServer(), new ResourceLocation("minecraft:village/desert/houses"),
                new ResourceLocation("quest_giver:village/desert/houses/stall"), 5);
        JigsawHelper.registerJigsaw(event.getServer(), new ResourceLocation("minecraft:village/plains/houses"),
                new ResourceLocation("quest_giver:village/plains/houses/stall"), 5);
        JigsawHelper.registerJigsaw(event.getServer(), new ResourceLocation("minecraft:village/savanna/houses"),
                new ResourceLocation("quest_giver:village/savanna/houses/stall"), 5);
        JigsawHelper.registerJigsaw(event.getServer(), new ResourceLocation("minecraft:village/snowy/houses"),
                new ResourceLocation("quest_giver:village/snowy/houses/stall"), 10);
        JigsawHelper.registerJigsaw(event.getServer(), new ResourceLocation("minecraft:village/taiga/houses"),
                new ResourceLocation("quest_giver:village/taiga/houses/stall"), 10);

        JigsawHelper.registerJigsaw(event.getServer(), new ResourceLocation("minecraft:village/desert/villagers"),
                new ResourceLocation("quest_giver:village/desert/villagers/quest_villager_desert"), 10);
        JigsawHelper.registerJigsaw(event.getServer(), new ResourceLocation("minecraft:village/plains/villagers"),
                new ResourceLocation("quest_giver:village/plains/villagers/quest_villager"), 10);
        JigsawHelper.registerJigsaw(event.getServer(), new ResourceLocation("minecraft:village/savanna/villagers"),
                new ResourceLocation("quest_giver:village/savanna/villagers/quest_villager_savanna"), 10);
        JigsawHelper.registerJigsaw(event.getServer(), new ResourceLocation("minecraft:village/snowy/villagers"),
                new ResourceLocation("quest_giver:village/snowy/villagers/quest_villager_snow"), 10);
        JigsawHelper.registerJigsaw(event.getServer(), new ResourceLocation("minecraft:village/taiga/villagers"),
                new ResourceLocation("quest_giver:village/taiga/villagers/quest_villager_taiga"), 10);
    }
}
