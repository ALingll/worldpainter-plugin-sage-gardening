package org.cti.wpplugin.myplants;

import org.pepsoft.minecraft.Material;
import org.pepsoft.worldpainter.Dimension;
import org.pepsoft.worldpainter.exporting.MinecraftWorld;

import java.io.Serializable;
import java.util.Optional;

import static org.pepsoft.minecraft.Constants.MC_CACTUS;
import static org.pepsoft.minecraft.Constants.MC_LAVA;

/**
 * @author: ALingll
 * @desc:The PlantHabit enum is used to describe the growth or generation location of features
 * (such as plants and fungi) and even non-living things (such as inorganic substances and artificial
 * ornaments). Although fungi do not belong to plants in taxonomy, the growth mode of their fruiting
 * bodies (such as mushrooms) or lichens is similar to the niche of plants, so this enum is
 * also applicable to them. In addition, the enumeration can be widely used to describe surface
 * features, such as pebbles, chained lanterns, etc.
 * @create: 2025-03-09 02:38
 **/
public enum PlantHabit implements Serializable {
    TERRESTRIAL("terrestrial","陆生，生长在陆地上"){
        @Override
        public Optional<Integer> isValidHabit(MinecraftWorld world, int x, int y, int z){
            if(!isWatery(world,x,y,z)){
                return Optional.of(z);
            }
            return Optional.empty();
        }
        @Override
        public Optional<Integer> isValidHabit(Dimension dimension, int x, int y){
            int z = dimension.getIntHeightAt(x,y);
            if(z >= dimension.getWaterLevelAt(x,y))
                return Optional.of(z+1);
            return Optional.empty();
        }
    },
    SUBMERGED("submerged","沉水，完全浸没在水中生长"){
        @Override
        public Optional<Integer> isValidHabit(MinecraftWorld world, int x, int y, int z){
            if(isSolid(world,x,y,z-1) && isWatery(world,x,y,z))
                return Optional.of(z);
            return Optional.empty();
        }
        @Override
        public Optional<Integer> isValidHabit(Dimension dimension, int x, int y){
            int z = dimension.getIntHeightAt(x,y);
            if(z < dimension.getWaterLevelAt(x,y))
                return Optional.of(z+1);
            return Optional.empty();
        }
    },
    EMERGENT("emergent","挺水，根部在水中，茎叶伸出水面"),
    FLOATING("floating","漂浮，漂浮在水面上"){
        @Override
        public Optional<Integer> isValidHabit(MinecraftWorld world, int x, int y, int z){
            if(!isWatery(world,x,y,z))
                return Optional.empty();
            int h = z;
            while (isWatery(world,x,y,h)){
                h++;
            }
            if(!isSolid(world,x,y,z))
                return Optional.of(h);
            else {
                return Optional.empty();
            }
        }
        @Override
        public Optional<Integer> isValidHabit(Dimension dimension, int x, int y){
            int z = dimension.getIntHeightAt(x,y);
            int z2 = dimension.getWaterLevelAt(x,y);
            if(z < z2)
                return Optional.of(z2+1);
            return Optional.empty();
        }
    },
    FLOATING_LEAF("floating_leaf","浮叶，漂浮在水面上"),
    EPIPHYTE("epiphyte","附生，依附在其他植物或物体上生长"),
    LIANA("liana","攀缘，依附其他物体攀爬"),

    @Deprecated
    VINE("vine","非木质藤本植物，依靠卷须或缠绕生长"),

    HANGING("hanging","悬垂");

    public final String description;
    public final String name;

    PlantHabit(String name, String description) {
        this.name = name;
        this.description = description;
    }

    /**
     * Check the environment where the plant may generate
     * @param z The position of the root block where the plant will be placed, rather than the foundation position.
     * @return
     */
    public Optional<Integer> isValidHabit(MinecraftWorld world, int x, int y, int z){
        return Optional.empty();
    }

    public Optional<Integer> isValidHabit(Dimension dimension, int x, int y){
        return Optional.empty();
    }

    public static PlantHabit of(String name) {
        for (PlantHabit e : values()) {
            if (e.name.equals(name)) {
                return e;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return name() + " (" + description + ")";
    }

    protected final boolean isWatery(MinecraftWorld world, int x, int y, int z) {
        return world.getMaterialAt(x, y, z).containsWater();
    }

    protected final boolean isFlooded(MinecraftWorld world, int x, int y, int z) {
        final Material materialAbove = world.getMaterialAt(x, y, z + 1);
        return materialAbove.containsWater() || materialAbove.isNamed(MC_LAVA);
    }

    protected final boolean isSolid(MinecraftWorld world, int x, int y, int z) {
        Material material = world.getMaterialAt(x, y, z);
        return ! material.veryInsubstantial;
    }



}

