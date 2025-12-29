package net.momirealms.craftengine.bukkit.plugin.network.id;

import net.momirealms.craftengine.bukkit.plugin.network.PacketIds;
import net.momirealms.craftengine.core.plugin.network.ConnectionState;
import net.momirealms.craftengine.core.plugin.network.PacketFlow;
import net.momirealms.craftengine.core.util.VersionHelper;

public class PacketIds1_20_5 implements PacketIds {

    @Override
    public int clientboundBlockUpdatePacket() {
        return PacketIdHelper.byName("minecraft:block_update", PacketFlow.CLIENTBOUND, ConnectionState.PLAY);
    }

    @Override
    public int clientboundSectionBlocksUpdatePacket() {
        return PacketIdHelper.byName("minecraft:section_blocks_update", PacketFlow.CLIENTBOUND, ConnectionState.PLAY);
    }

    @Override
    public int clientboundLevelParticlesPacket() {
        return PacketIdHelper.byName("minecraft:level_particles", PacketFlow.CLIENTBOUND, ConnectionState.PLAY);
    }

    @Override
    public int clientboundLevelEventPacket() {
        return PacketIdHelper.byName("minecraft:level_event", PacketFlow.CLIENTBOUND, ConnectionState.PLAY);
    }

    @Override
    public int clientboundAddEntityPacket() {
        return PacketIdHelper.byName("minecraft:add_entity", PacketFlow.CLIENTBOUND, ConnectionState.PLAY);
    }

    @Override
    public int clientboundOpenScreenPacket() {
        return PacketIdHelper.byName("minecraft:open_screen", PacketFlow.CLIENTBOUND, ConnectionState.PLAY);
    }

    @Override
    public int clientboundSoundPacket() {
        return PacketIdHelper.byName("minecraft:sound", PacketFlow.CLIENTBOUND, ConnectionState.PLAY);
    }

    @Override
    public int clientboundRemoveEntitiesPacket() {
        return PacketIdHelper.byName("minecraft:remove_entities", PacketFlow.CLIENTBOUND, ConnectionState.PLAY);
    }

    @Override
    public int clientboundSetEntityDataPacket() {
        return PacketIdHelper.byName("minecraft:set_entity_data", PacketFlow.CLIENTBOUND, ConnectionState.PLAY);
    }

    @Override
    public int clientboundSetTitleTextPacket() {
        return PacketIdHelper.byName("minecraft:set_title_text", PacketFlow.CLIENTBOUND, ConnectionState.PLAY);
    }

    @Override
    public int clientboundSetSubtitleTextPacket() {
        return PacketIdHelper.byName("minecraft:set_subtitle_text", PacketFlow.CLIENTBOUND, ConnectionState.PLAY);
    }

    @Override
    public int clientboundSetActionBarTextPacket() {
        return PacketIdHelper.byName("minecraft:set_action_bar_text", PacketFlow.CLIENTBOUND, ConnectionState.PLAY);
    }

    @Override
    public int clientboundBossEventPacket() {
        return PacketIdHelper.byName("minecraft:boss_event", PacketFlow.CLIENTBOUND, ConnectionState.PLAY);
    }

    @Override
    public int clientboundSystemChatPacket() {
        return PacketIdHelper.byName("minecraft:system_chat", PacketFlow.CLIENTBOUND, ConnectionState.PLAY);
    }

    @Override
    public int clientboundTabListPacket() {
        return PacketIdHelper.byName("minecraft:tab_list", PacketFlow.CLIENTBOUND, ConnectionState.PLAY);
    }

    @Override
    public int clientboundSetPlayerTeamPacket() {
        return PacketIdHelper.byName("minecraft:set_player_team", PacketFlow.CLIENTBOUND, ConnectionState.PLAY);
    }

    @Override
    public int clientboundSetObjectivePacket() {
        return PacketIdHelper.byName("minecraft:set_objective", PacketFlow.CLIENTBOUND, ConnectionState.PLAY);
    }

    @Override
    public int clientboundLevelChunkWithLightPacket() {
        return PacketIdHelper.byName("minecraft:level_chunk_with_light", PacketFlow.CLIENTBOUND, ConnectionState.PLAY);
    }

    @Override
    public int clientboundPlayerInfoUpdatePacket() {
        return PacketIdHelper.byName("minecraft:player_info_update", PacketFlow.CLIENTBOUND, ConnectionState.PLAY);
    }

    @Override
    public int clientboundSetScorePacket() {
        return PacketIdHelper.byName("minecraft:set_score", PacketFlow.CLIENTBOUND, ConnectionState.PLAY);
    }

    @Override
    public int clientboundContainerSetContentPacket() {
        return PacketIdHelper.byName("minecraft:container_set_content", PacketFlow.CLIENTBOUND, ConnectionState.PLAY);
    }

    @Override
    public int clientboundContainerSetSlotPacket() {
        return PacketIdHelper.byName("minecraft:container_set_slot", PacketFlow.CLIENTBOUND, ConnectionState.PLAY);
    }

    @Override
    public int clientboundSetCursorItemPacket() {
        return PacketIdHelper.byName("minecraft:set_cursor_item", PacketFlow.CLIENTBOUND, ConnectionState.PLAY);
    }

    @Override
    public int clientboundSetEquipmentPacket() {
        return PacketIdHelper.byName("minecraft:set_equipment", PacketFlow.CLIENTBOUND, ConnectionState.PLAY);
    }

    @Override
    public int clientboundSetPlayerInventoryPacket() {
        return PacketIdHelper.byName("minecraft:set_player_inventory", PacketFlow.CLIENTBOUND, ConnectionState.PLAY);
    }

    @Override
    public int clientboundBlockEventPacket() {
        return PacketIdHelper.byName("minecraft:block_event", PacketFlow.CLIENTBOUND, ConnectionState.PLAY);
    }

    @Override
    public int clientboundRecipeBookAddPacket() {
        return PacketIdHelper.byName("minecraft:recipe_book_add", PacketFlow.CLIENTBOUND, ConnectionState.PLAY);
    }

    @Override
    public int clientboundPlaceGhostRecipePacket() {
        return PacketIdHelper.byName("minecraft:place_ghost_recipe", PacketFlow.CLIENTBOUND, ConnectionState.PLAY);
    }

    @Override
    public int clientboundUpdateRecipesPacket() {
        return PacketIdHelper.byName("minecraft:update_recipes", PacketFlow.CLIENTBOUND, ConnectionState.PLAY);
    }

    @Override
    public int clientboundUpdateAdvancementsPacket() {
        return PacketIdHelper.byName("minecraft:update_advancements", PacketFlow.CLIENTBOUND, ConnectionState.PLAY);
    }

    @Override
    public int clientboundForgetLevelChunkPacket() {
        return PacketIdHelper.byName("minecraft:forget_level_chunk", PacketFlow.CLIENTBOUND, ConnectionState.PLAY);
    }

    @Override
    public int clientBoundMerchantOffersPacket() {
        return PacketIdHelper.byName("minecraft:merchant_offers", PacketFlow.CLIENTBOUND, ConnectionState.PLAY);
    }

    @Override
    public int clientboundBlockEntityDataPacket() {
        return PacketIdHelper.byName("minecraft:block_entity_data", PacketFlow.CLIENTBOUND, ConnectionState.PLAY);
    }

    @Override
    public int serverboundContainerClickPacket() {
        return PacketIdHelper.byName("minecraft:container_click", PacketFlow.SERVERBOUND, ConnectionState.PLAY);
    }

    @Override
    public int serverboundSetCreativeModeSlotPacket() {
        return PacketIdHelper.byName("minecraft:set_creative_mode_slot", PacketFlow.SERVERBOUND, ConnectionState.PLAY);
    }

    @Override
    public int serverboundInteractPacket() {
        return PacketIdHelper.byName("minecraft:interact", PacketFlow.SERVERBOUND, ConnectionState.PLAY);
    }

    @Override
    public int serverboundCustomPayloadPacket() {
        return PacketIdHelper.byName("minecraft:custom_payload", PacketFlow.SERVERBOUND, ConnectionState.PLAY);
    }

    @Override
    public int clientboundPlayerChatPacket() {
        return PacketIdHelper.byName("minecraft:player_chat", PacketFlow.CLIENTBOUND, ConnectionState.PLAY);
    }

    @Override
    public int clientIntentionPacket() {
        return PacketIdHelper.byName("minecraft:intention", PacketFlow.SERVERBOUND, ConnectionState.HANDSHAKING);
    }

    @Override
    public int clientboundStatusResponsePacket() {
        return PacketIdHelper.byName("minecraft:status_response", PacketFlow.CLIENTBOUND, ConnectionState.STATUS);
    }

    @Override
    public int serverboundFinishConfigurationPacket() {
        return PacketIdHelper.byName("minecraft:finish_configuration", PacketFlow.SERVERBOUND, ConnectionState.CONFIGURATION);
    }

    @Override
    public int clientboundLoginPacket() {
        return PacketIdHelper.byName("minecraft:login", PacketFlow.CLIENTBOUND, ConnectionState.PLAY);
    }

    @Override
    public int clientboundLoginFinishedPacket() {
        return PacketIdHelper.byName(VersionHelper.isOrAbove1_21_2() ? "minecraft:login_finished" : "minecraft:game_profile", PacketFlow.CLIENTBOUND, ConnectionState.LOGIN);
    }

    @Override
    public int serverboundLoginAcknowledgedPacket() {
        return PacketIdHelper.byName("minecraft:login_acknowledged", PacketFlow.SERVERBOUND, ConnectionState.LOGIN);
    }

    @Override
    public int clientboundStartConfigurationPacket() {
        return PacketIdHelper.byName("minecraft:start_configuration", PacketFlow.CLIENTBOUND, ConnectionState.PLAY);
    }

    @Override
    public int serverboundConfigurationAcknowledgedPacket() {
        return PacketIdHelper.byName("minecraft:configuration_acknowledged", PacketFlow.SERVERBOUND, ConnectionState.PLAY);
    }
}
