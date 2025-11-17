package net.momirealms.craftengine.bukkit.plugin.network.id;

import net.momirealms.craftengine.bukkit.plugin.network.PacketIds;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.NetworkReflections;
import net.momirealms.craftengine.core.plugin.network.PacketFlow;

public class PacketIds1_20 implements PacketIds {

    @Override
    public int clientboundBlockUpdatePacket() {
        return PlayPacketIdHelper.byClazz(NetworkReflections.clazz$ClientboundBlockUpdatePacket, PacketFlow.CLIENTBOUND);
    }

    @Override
    public int clientboundSectionBlocksUpdatePacket() {
        return PlayPacketIdHelper.byClazz(NetworkReflections.clazz$ClientboundSectionBlocksUpdatePacket, PacketFlow.CLIENTBOUND);
    }

    @Override
    public int clientboundLevelParticlesPacket() {
        return PlayPacketIdHelper.byClazz(NetworkReflections.clazz$ClientboundLevelParticlesPacket, PacketFlow.CLIENTBOUND);
    }

    @Override
    public int clientboundLevelEventPacket() {
        return PlayPacketIdHelper.byClazz(NetworkReflections.clazz$ClientboundLevelEventPacket, PacketFlow.CLIENTBOUND);
    }

    @Override
    public int clientboundAddEntityPacket() {
        return PlayPacketIdHelper.byClazz(NetworkReflections.clazz$ClientboundAddEntityPacket, PacketFlow.CLIENTBOUND);
    }

    @Override
    public int clientboundOpenScreenPacket() {
        return PlayPacketIdHelper.byClazz(NetworkReflections.clazz$ClientboundOpenScreenPacket, PacketFlow.CLIENTBOUND);
    }

    @Override
    public int clientboundSoundPacket() {
        return PlayPacketIdHelper.byClazz(NetworkReflections.clazz$ClientboundSoundPacket, PacketFlow.CLIENTBOUND);
    }

    @Override
    public int clientboundRemoveEntitiesPacket() {
        return PlayPacketIdHelper.byClazz(NetworkReflections.clazz$ClientboundRemoveEntitiesPacket, PacketFlow.CLIENTBOUND);
    }

    @Override
    public int clientboundSetEntityDataPacket() {
        return PlayPacketIdHelper.byClazz(NetworkReflections.clazz$ClientboundSetEntityDataPacket, PacketFlow.CLIENTBOUND);
    }

    @Override
    public int clientboundSetTitleTextPacket() {
        return PlayPacketIdHelper.byClazz(NetworkReflections.clazz$ClientboundSetTitleTextPacket, PacketFlow.CLIENTBOUND);
    }

    @Override
    public int clientboundSetSubtitleTextPacket() {
        return PlayPacketIdHelper.byClazz(NetworkReflections.clazz$ClientboundSetSubtitleTextPacket, PacketFlow.CLIENTBOUND);
    }

    @Override
    public int clientboundSetActionBarTextPacket() {
        return PlayPacketIdHelper.byClazz(NetworkReflections.clazz$ClientboundSetActionBarTextPacket, PacketFlow.CLIENTBOUND);
    }

    @Override
    public int clientboundBossEventPacket() {
        return PlayPacketIdHelper.byClazz(NetworkReflections.clazz$ClientboundBossEventPacket, PacketFlow.CLIENTBOUND);
    }

    @Override
    public int clientboundSystemChatPacket() {
        return PlayPacketIdHelper.byClazz(NetworkReflections.clazz$ClientboundSystemChatPacket, PacketFlow.CLIENTBOUND);
    }

    @Override
    public int clientboundTabListPacket() {
        return PlayPacketIdHelper.byClazz(NetworkReflections.clazz$ClientboundTabListPacket, PacketFlow.CLIENTBOUND);
    }

    @Override
    public int clientboundSetPlayerTeamPacket() {
        return PlayPacketIdHelper.byClazz(NetworkReflections.clazz$ClientboundSetPlayerTeamPacket, PacketFlow.CLIENTBOUND);
    }

    @Override
    public int clientboundSetObjectivePacket() {
        return PlayPacketIdHelper.byClazz(NetworkReflections.clazz$ClientboundSetObjectivePacket, PacketFlow.CLIENTBOUND);
    }

    @Override
    public int clientboundLevelChunkWithLightPacket() {
        return PlayPacketIdHelper.byClazz(NetworkReflections.clazz$ClientboundLevelChunkWithLightPacket, PacketFlow.CLIENTBOUND);
    }

    @Override
    public int clientboundPlayerInfoUpdatePacket() {
        return PlayPacketIdHelper.byClazz(NetworkReflections.clazz$ClientboundPlayerInfoUpdatePacket, PacketFlow.CLIENTBOUND);
    }

    @Override
    public int clientboundSetScorePacket() {
        return PlayPacketIdHelper.byClazz(NetworkReflections.clazz$ClientboundSetScorePacket, PacketFlow.CLIENTBOUND);
    }

    @Override
    public int clientboundContainerSetContentPacket() {
        return PlayPacketIdHelper.byClazz(NetworkReflections.clazz$ClientboundContainerSetContentPacket, PacketFlow.CLIENTBOUND);
    }

    @Override
    public int clientboundContainerSetSlotPacket() {
        return PlayPacketIdHelper.byClazz(NetworkReflections.clazz$ClientboundContainerSetSlotPacket, PacketFlow.CLIENTBOUND);
    }

    @Override
    public int clientboundSetCursorItemPacket() {
        return PlayPacketIdHelper.byClazz(NetworkReflections.clazz$ClientboundSetCursorItemPacket, PacketFlow.CLIENTBOUND);
    }

    @Override
    public int clientboundSetEquipmentPacket() {
        return PlayPacketIdHelper.byClazz(NetworkReflections.clazz$ClientboundSetEquipmentPacket, PacketFlow.CLIENTBOUND);
    }

    @Override
    public int clientboundSetPlayerInventoryPacket() {
        return PlayPacketIdHelper.byClazz(NetworkReflections.clazz$ClientboundSetPlayerInventoryPacket, PacketFlow.CLIENTBOUND);
    }

    @Override
    public int clientboundRecipeBookAddPacket() {
        return PlayPacketIdHelper.byClazz(NetworkReflections.clazz$ClientboundRecipeBookAddPacket, PacketFlow.CLIENTBOUND);
    }

    @Override
    public int clientboundPlaceGhostRecipePacket() {
        return PlayPacketIdHelper.byClazz(NetworkReflections.clazz$ClientboundPlaceGhostRecipePacket, PacketFlow.CLIENTBOUND);
    }

    @Override
    public int clientboundUpdateRecipesPacket() {
        return PlayPacketIdHelper.byClazz(NetworkReflections.clazz$ClientboundUpdateRecipesPacket, PacketFlow.CLIENTBOUND);
    }

    @Override
    public int clientboundUpdateAdvancementsPacket() {
        return PlayPacketIdHelper.byClazz(NetworkReflections.clazz$ClientboundUpdateAdvancementsPacket, PacketFlow.CLIENTBOUND);
    }

    @Override
    public int clientboundForgetLevelChunkPacket() {
        return PlayPacketIdHelper.byClazz(NetworkReflections.clazz$ClientboundForgetLevelChunkPacket, PacketFlow.CLIENTBOUND);
    }

    @Override
    public int clientboundBlockEventPacket() {
        return PlayPacketIdHelper.byClazz(NetworkReflections.clazz$ClientboundBlockEventPacket, PacketFlow.CLIENTBOUND);
    }

    @Override
    public int clientBoundMerchantOffersPacket() {
        return PlayPacketIdHelper.byClazz(NetworkReflections.clazz$ClientboundMerchantOffersPacket, PacketFlow.CLIENTBOUND);
    }

    @Override
    public int clientboundBlockEntityDataPacket() {
        return PlayPacketIdHelper.byClazz(NetworkReflections.clazz$ClientboundBlockEntityDataPacket, PacketFlow.CLIENTBOUND);
    }

    @Override
    public int serverboundContainerClickPacket() {
        return PlayPacketIdHelper.byClazz(NetworkReflections.clazz$ServerboundContainerClickPacket, PacketFlow.SERVERBOUND);
    }

    @Override
    public int serverboundSetCreativeModeSlotPacket() {
        return PlayPacketIdHelper.byClazz(NetworkReflections.clazz$ServerboundSetCreativeModeSlotPacket, PacketFlow.SERVERBOUND);
    }

    @Override
    public int serverboundInteractPacket() {
        return PlayPacketIdHelper.byClazz(NetworkReflections.clazz$ServerboundInteractPacket, PacketFlow.SERVERBOUND);
    }

    @Override
    public int serverboundCustomPayloadPacket() {
        return PlayPacketIdHelper.byClazz(NetworkReflections.clazz$ServerboundCustomPayloadPacket, PacketFlow.SERVERBOUND);
    }

    @Override
    public int clientboundPlayerChatPacket() {
        return PlayPacketIdHelper.byClazz(NetworkReflections.clazz$ClientboundPlayerChatPacket, PacketFlow.CLIENTBOUND);
    }
}
