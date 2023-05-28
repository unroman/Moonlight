package net.mehvahdjukaar.moonlight.api.map.type;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.api.map.CustomMapDecoration;
import net.mehvahdjukaar.moonlight.api.map.markers.GenericMapBlockMarker;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

//base type for simple data driven type. Basically a simple version of CustomDecorationType that can be serialized
public final class JsonDecorationType extends MapDecorationType<CustomMapDecoration, GenericMapBlockMarker<CustomMapDecoration>> {

    //using this and not block predicate since it requires a worldLevelGen...
    @Nullable
    private final RuleTest target;

    @Nullable
    private final String name;
    @Nullable
    private final HolderSet<Structure> structures;
    private final int mapColor;
    private final float rotation;


    public static final Codec<JsonDecorationType> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            RuleTest.CODEC.optionalFieldOf("target_block").forGetter(JsonDecorationType::getTarget),
            Codec.STRING.optionalFieldOf("name").forGetter(JsonDecorationType::getName),
            Codec.FLOAT.optionalFieldOf("rotation" ,0f).forGetter(JsonDecorationType::getRotation),
            Codec.INT.optionalFieldOf("map_color", 0).forGetter(JsonDecorationType::getDefaultMapColor),
            RegistryCodecs.homogeneousList(Registries.STRUCTURE).optionalFieldOf("target_structures")
                    .forGetter(JsonDecorationType::getAssociatedStructure)
    ).apply(instance, JsonDecorationType::new));


    public JsonDecorationType(Optional<RuleTest> target) {
        this(target, Optional.empty(), 0, 0,Optional.empty());
    }
    public JsonDecorationType(Optional<RuleTest> target, Optional<String> name, float rotation,
                              int mapColor, Optional<HolderSet<Structure>> structure) {
        this.target = target.orElse(null);
        this.name = name.orElse(null);
        this.rotation = rotation;
        this.structures = structure.orElse(null);
        this.mapColor = 0;
    }

    public Optional<RuleTest> getTarget() {
        return Optional.ofNullable(target);
    }

    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }

    public float getRotation() {
        return rotation;
    }

    public Optional<HolderSet<Structure>> getAssociatedStructure() {
        return Optional.ofNullable(structures);
    }

    public int getDefaultMapColor() {
        return mapColor;
    }

    @Override
    public boolean hasMarker() {
        return target != null;
    }

    public ResourceLocation getId() {
        return Utils.getID(this);
    }

    @Nullable
    @Override
    public CustomMapDecoration loadDecorationFromBuffer(FriendlyByteBuf buffer) {
        try {
            return new CustomMapDecoration(this, buffer);
        } catch (Exception e) {
            Moonlight.LOGGER.warn("Failed to load custom map decoration for decoration type" + this.getId() + ": " + e);
        }
        return null;
    }

    @Nullable
    @Override
    public GenericMapBlockMarker<CustomMapDecoration> loadMarkerFromNBT(CompoundTag compound) {
        if (this.hasMarker()) {
            GenericMapBlockMarker<CustomMapDecoration> marker = new GenericMapBlockMarker<>(this);
            try {
                marker.loadFromNBT(compound);
                return marker;
            } catch (Exception e) {
                Moonlight.LOGGER.warn("Failed to load world map marker for decoration type" + this.getId() + ": " + e);
            }
        }
        return null;
    }

    @Nullable
    @Override
    public GenericMapBlockMarker<CustomMapDecoration> getWorldMarkerFromWorld(BlockGetter reader, BlockPos pos) {
        if (this.target != null) {
            if (target.test(reader.getBlockState(pos), RandomSource.create())) {
                return new GenericMapBlockMarker<>(this, pos);
            }
        }
        return null;
    }
}