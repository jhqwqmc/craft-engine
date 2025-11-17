package net.momirealms.craftengine.bukkit.plugin.network.id;

import net.momirealms.craftengine.bukkit.plugin.network.PacketIds;
import net.momirealms.craftengine.core.plugin.network.PacketFlow;

public class PacketIds1_20_5 implements PacketIds {

    @Override
    public int clientboundBlockUpdatePacket() {
        return PlayPacketIdHelper.byName("minecraft:block_update", PacketFlow.CLIENTBOUND);
    }

    @Override
    public int clientboundSectionBlocksUpdatePacket() {
        return PlayPacketIdHelper.byName("minecraft:section_blocks_update", PacketFlow.CLIENTBOUND);
    }

    @Override
    public int clientboundLevelParticlesPacket() {
        return PlayPacketIdHelper.byName("minecraft:level_particles", PacketFlow.CLIENTBOUND);
    }

    @Override
    public int clientboundLevelEventPacket() {
        return PlayPacketIdHelper.byName("minecraft:level_event", PacketFlow.CLIENTBOUND);
    }

    @Override
    public int clientboundAddEntityPacket() {
        return PlayPacketIdHelper.byName("minecraft:add_entity", PacketFlow.CLIENTBOUND);
    }

    @Override
    public int clientboundOpenScreenPacket() {
        return PlayPacketIdHelper.byName("minecraft:open_screen", PacketFlow.CLIENTBOUND);
    }

    @Override
    public int clientboundSoundPacket() {
        return PlayPacketIdHelper.byName("minecraft:sound", PacketFlow.CLIENTBOUND);
    }

    @Override
    public int clientboundRemoveEntitiesPacket() {
        return PlayPacketIdHelper.byName("minecraft:remove_entities", PacketFlow.CLIENTBOUND);
    }

    @Override
    public int clientboundSetEntityDataPacket() {
        return PlayPacketIdHelper.byName("minecraft:set_entity_data", PacketFlow.CLIENTBOUND);
    }

    @Override
    public int clientboundSetTitleTextPacket() {
        return PlayPacketIdHelper.byName("minecraft:set_title_text", PacketFlow.CLIENTBOUND);
    }

    @Override
    public int clientboundSetSubtitleTextPacket() {
        return PlayPacketIdHelper.byName("minecraft:set_subtitle_text", PacketFlow.CLIENTBOUND);
    }

    @Override
    public int clientboundSetActionBarTextPacket() {
        return PlayPacketIdHelper.byName("minecraft:set_action_bar_text", PacketFlow.CLIENTBOUND);
    }

    @Override
    public int clientboundBossEventPacket() {
        return PlayPacketIdHelper.byName("minecraft:boss_event", PacketFlow.CLIENTBOUND);
    }

    @Override
    public int clientboundSystemChatPacket() {
        return PlayPacketIdHelper.byName("minecraft:system_chat", PacketFlow.CLIENTBOUND);
    }

    @Override
    public int clientboundTabListPacket() {
        return PlayPacketIdHelper.byName("minecraft:tab_list", PacketFlow.CLIENTBOUND);
    }

    @Override
    public int clientboundSetPlayerTeamPacket() {
        return PlayPacketIdHelper.byName("minecraft:set_player_team", PacketFlow.CLIENTBOUND);
    }

    @Override
    public int clientboundSetObjectivePacket() {
        return PlayPacketIdHelper.byName("minecraft:set_objective", PacketFlow.CLIENTBOUND);
    }

    @Override
    public int clientboundLevelChunkWithLightPacket() {
        return PlayPacketIdHelper.byName("minecraft:level_chunk_with_light", PacketFlow.CLIENTBOUND);
    }

    @Override
    public int clientboundPlayerInfoUpdatePacket() {
        return PlayPacketIdHelper.byName("minecraft:player_info_update", PacketFlow.CLIENTBOUND);
    }

    @Override
    public int clientboundSetScorePacket() {
        return PlayPacketIdHelper.byName("minecraft:set_score", PacketFlow.CLIENTBOUND);
    }

    @Override
    public int clientboundContainerSetContentPacket() {
        return PlayPacketIdHelper.byName("minecraft:container_set_content", PacketFlow.CLIENTBOUND);
    }

    @Override
    public int clientboundContainerSetSlotPacket() {
        return PlayPacketIdHelper.byName("minecraft:container_set_slot", PacketFlow.CLIENTBOUND);
    }

    @Override
    public int clientboundSetCursorItemPacket() {
        return PlayPacketIdHelper.byName("minecraft:set_cursor_item", PacketFlow.CLIENTBOUND);
    }

    @Override
    public int clientboundSetEquipmentPacket() {
        return PlayPacketIdHelper.byName("minecraft:set_equipment", PacketFlow.CLIENTBOUND);
    }

    @Override
    public int clientboundSetPlayerInventoryPacket() {
        return PlayPacketIdHelper.byName("minecraft:set_player_inventory", PacketFlow.CLIENTBOUND);
    }

    @Override
    public int clientboundBlockEventPacket() {
        return PlayPacketIdHelper.byName("minecraft:block_event", PacketFlow.CLIENTBOUND);
    }

    @Override
    public int clientboundRecipeBookAddPacket() {
        return PlayPacketIdHelper.byName("minecraft:recipe_book_add", PacketFlow.CLIENTBOUND);
    }

    @Override
    public int clientboundPlaceGhostRecipePacket() {
        return PlayPacketIdHelper.byName("minecraft:place_ghost_recipe", PacketFlow.CLIENTBOUND);
    }

    @Override
    public int clientboundUpdateRecipesPacket() {
        return PlayPacketIdHelper.byName("minecraft:update_recipes", PacketFlow.CLIENTBOUND);
    }

    @Override
    public int clientboundUpdateAdvancementsPacket() {
        return PlayPacketIdHelper.byName("minecraft:update_advancements", PacketFlow.CLIENTBOUND);
    }

    @Override
    public int clientboundForgetLevelChunkPacket() {
        return PlayPacketIdHelper.byName("minecraft:forget_level_chunk", PacketFlow.CLIENTBOUND);
    }

    @Override
    public int clientBoundMerchantOffersPacket() {
        return PlayPacketIdHelper.byName("minecraft:merchant_offers", PacketFlow.CLIENTBOUND);
    }

    @Override
    public int clientboundBlockEntityDataPacket() {
        return PlayPacketIdHelper.byName("minecraft:block_entity_data", PacketFlow.CLIENTBOUND);
    }

    @Override
    public int serverboundContainerClickPacket() {
        return PlayPacketIdHelper.byName("minecraft:container_click", PacketFlow.SERVERBOUND);
    }

    @Override
    public int serverboundSetCreativeModeSlotPacket() {
        return PlayPacketIdHelper.byName("minecraft:set_creative_mode_slot", PacketFlow.SERVERBOUND);
    }

    @Override
    public int serverboundInteractPacket() {
        return PlayPacketIdHelper.byName("minecraft:interact", PacketFlow.SERVERBOUND);
    }

    @Override
    public int serverboundCustomPayloadPacket() {
        return PlayPacketIdHelper.byName("minecraft:custom_payload", PacketFlow.SERVERBOUND);
    }

    @Override
    public int clientboundPlayerChatPacket() {
        return PlayPacketIdHelper.byName("minecraft:player_chat", PacketFlow.CLIENTBOUND);
    }
}
