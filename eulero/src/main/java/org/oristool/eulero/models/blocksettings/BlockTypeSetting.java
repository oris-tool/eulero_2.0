package org.oristool.eulero.models.blocksettings;

public abstract class BlockTypeSetting {
    private String type;
    private double probability;

    public BlockTypeSetting(String type, double probability) {
        this.type = type;
        this.probability = probability;
    }

    public String getType() {
        return type;
    }

    public double getProbability() {
        return probability;
    }
}
