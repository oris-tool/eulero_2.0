package org.oristool.eulero.models.blocksettings;

public class ANDBlockSetting extends WellNestedBlockSetting {
    public ANDBlockSetting(double probability, int maximumBreadth) {
        super("AND", probability, maximumBreadth);
    }
}
