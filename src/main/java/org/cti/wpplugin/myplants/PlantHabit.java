package org.cti.wpplugin.myplants;

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
public enum PlantHabit {
    TERRESTRIAL("陆生，生长在陆地上"),
    SUBMERGED("沉水，完全浸没在水中生长"),
    EMERGENT("挺水，根部在水中，茎叶伸出水面"),
    FREE_FLOATING("漂浮，漂浮在水面上"),
    FLOAT_LEAF("浮叶，漂浮在水面上"),
    EPIPHYTE("附生，依附在其他植物或物体上生长"),
    LIANA("攀缘，依附其他物体攀爬"),

    @Deprecated
    VINE("非木质藤本植物，依靠卷须或缠绕生长"),

    HANGING("悬垂");

    private final String description;

    PlantHabit(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return name() + " (" + description + ")";
    }

    public static void main(String[] args) {
        for (PlantHabit habit : PlantHabit.values()) {
            System.out.println(habit);
        }
    }
}

