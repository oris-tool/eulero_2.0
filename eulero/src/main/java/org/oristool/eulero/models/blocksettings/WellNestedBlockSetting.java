package org.oristool.eulero.models.blocksettings;

public class WellNestedBlockSetting extends BlockTypeSetting {
    int maximumBreadth;
    int minimumBreadth;

    public WellNestedBlockSetting(String type, double probability, int maximumBreadth, int minimumBreadth) {
        super(type, probability);
        this.maximumBreadth = maximumBreadth;
        this.minimumBreadth = minimumBreadth;
    }

    public WellNestedBlockSetting(String type, double probability, int maximumBreadth) {
        this(type, probability, maximumBreadth, 2);
    }

    public int getMaximumBreadth() {
        return maximumBreadth;
    }

    public int getMinimumBreadth() {
        return minimumBreadth;
    }
}
