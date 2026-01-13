package net.momirealms.craftengine.core.world.generation.feature.modifier;

import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.util.random.RandomSource;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.GeneratingWorld;
import net.momirealms.craftengine.core.world.SectionPos;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public final class FixedPlacement extends PlacementModifier {
    public static final PlacementModifierFactory<FixedPlacement> FACTORY = new Factory();
    private final List<BlockPos> positions;

    private FixedPlacement(List<BlockPos> positions) {
        this.positions = positions;
    }

    @Override
    public Stream<BlockPos> getPositions(GeneratingWorld world, BlockPos origin, RandomSource random) {
        int sectionPosX = SectionPos.blockToSectionCoord(origin.x);
        int sectionPosZ = SectionPos.blockToSectionCoord(origin.z);
        boolean sameChunk = false;
        for (BlockPos blockPos : this.positions) {
            if (isSameChunk(sectionPosX, sectionPosZ, blockPos)) {
                sameChunk = true;
                break;
            }
        }
        return !sameChunk ? Stream.empty() : this.positions.stream().filter((pos) -> isSameChunk(sectionPosX, sectionPosZ, pos));
    }

    private static boolean isSameChunk(int x, int z, BlockPos pos) {
        return x == SectionPos.blockToSectionCoord(pos.x) && z == SectionPos.blockToSectionCoord(pos.z);
    }

    private static class Factory implements PlacementModifierFactory<FixedPlacement> {

        @Override
        public FixedPlacement create(Map<String, Object> args) {
            List<String> positions = MiscUtils.getAsStringList(ResourceConfigUtils.get(args, "positions"));
            return new FixedPlacement(positions.stream().map(it -> {
                String[] split = it.split(",");
                return new BlockPos(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
            }).toList());
        }
    }
}
