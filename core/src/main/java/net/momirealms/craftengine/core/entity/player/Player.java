package net.momirealms.craftengine.core.entity.player;

import net.kyori.adventure.text.Component;
import net.momirealms.craftengine.core.advancement.AdvancementType;
import net.momirealms.craftengine.core.entity.AbstractEntity;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.plugin.context.CooldownData;
import net.momirealms.craftengine.core.plugin.network.NetWorkUser;
import net.momirealms.craftengine.core.sound.SoundData;
import net.momirealms.craftengine.core.sound.SoundSource;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.Position;
import net.momirealms.craftengine.core.world.Vec3d;
import net.momirealms.craftengine.core.world.WorldPosition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public abstract class Player extends AbstractEntity implements NetWorkUser {
    private static final Key TYPE = Key.of("minecraft:player");

    public abstract boolean isSecondaryUseActive();

    @NotNull
    public abstract Item<?> getItemInHand(InteractionHand hand);

    @Override
    public abstract Object platformPlayer();

    @Override
    public abstract Object serverPlayer();

    public abstract float getDestroyProgress(Object blockState, BlockPos pos);

    public abstract void setClientSideCanBreakBlock(boolean canBreak);

    public abstract void stopMiningBlock();

    public abstract void preventMiningBlock();

    public abstract void abortMiningBlock();

    public abstract void breakBlock(int x, int y, int z);

    public abstract double getCachedInteractionRange();

    public abstract void onSwingHand();

    public abstract boolean isMiningBlock();

    public abstract boolean shouldSyncAttribute();

    public abstract boolean isSneaking();

    public abstract boolean isSwimming();

    public abstract boolean isClimbing();

    public abstract boolean isGliding();

    public abstract boolean isFlying();

    public abstract GameMode gameMode();

    public abstract void setGameMode(GameMode gameMode);

    public abstract boolean canBreak(BlockPos pos, Object state);

    public abstract boolean canPlace(BlockPos pos, Object state);

    public abstract void sendToast(Component text, Item<?> icon, AdvancementType type);

    public abstract void sendActionBar(Component text);

    public abstract void sendMessage(Component text, boolean overlay);

    public abstract void sendTitle(Component title, Component subtitle, int fadeIn, int stay, int fadeOut);

    public abstract boolean updateLastSuccessfulInteractionTick(int tick);

    public abstract int lastSuccessfulInteractionTick();

    public abstract void updateLastInteractEntityTick(@NotNull InteractionHand hand);

    public abstract boolean lastInteractEntityCheck(@NotNull InteractionHand hand);

    public abstract int gameTicks();

    public abstract void swingHand(InteractionHand hand);

    public abstract boolean hasPermission(String permission);

    public abstract boolean canInstabuild();

    public abstract String name();

    public void playSound(Key sound) {
        playSound(sound, 1f, 1f);
    }

    public void playSound(Key sound, float volume, float pitch) {
        playSound(sound, SoundSource.MASTER, volume, pitch);
    }

    public abstract void playSound(Key sound, SoundSource source, float volume, float pitch);

    public abstract void playSound(Position pos, Key sound, SoundSource source, float volume, float pitch);

    public void playSound(BlockPos pos, Key sound, SoundSource source, float volume, float pitch) {
        this.playSound(Vec3d.atCenterOf(pos), sound, source, volume, pitch);
    }

    public void playSound(BlockPos pos, SoundData data, SoundSource source) {
        this.playSound(pos, data.id(), source, data.volume().get(), data.pitch().get());
    }

    public void playSound(Position pos, SoundData data, SoundSource source) {
        this.playSound(pos, data.id(), source, data.volume().get(), data.pitch().get());
    }

    public abstract void giveItem(Item<?> item);

    public abstract void closeInventory();

    public abstract void clearView();

    public abstract void unloadCurrentResourcePack();

    public abstract void performCommand(String command, boolean asOp);

    public abstract void performCommandAsEvent(String command);

    public abstract double luck();

    @Override
    public Key type() {
        return TYPE;
    }

    public boolean isCreativeMode() {
        return gameMode() == GameMode.CREATIVE;
    }

    public boolean isSpectatorMode() {
        return gameMode() == GameMode.SPECTATOR;
    }

    public boolean isSurvivalMode() {
        return gameMode() == GameMode.SURVIVAL;
    }

    public boolean isAdventureMode() {
        return gameMode() == GameMode.ADVENTURE;
    }

    public abstract int foodLevel();

    public abstract void setFoodLevel(int foodLevel);

    public abstract float saturation();

    public abstract void setSaturation(float saturation);

    public abstract void addPotionEffect(Key potionEffectType, int duration, int amplifier, boolean ambient, boolean particles);

    public abstract void removePotionEffect(Key potionEffectType);

    public abstract void clearPotionEffects();

    public abstract CooldownData cooldown();

    public abstract void teleport(WorldPosition worldPosition);

    public abstract void damage(double amount, Key damageType);

    public abstract Locale locale();

    public abstract Locale selectedLocale();

    public abstract void setSelectedLocale(@Nullable Locale locale);

    @Override
    public void remove() {
    }
}
