package org.oristool.eulero.randomgenerator.blocksettings;

public class DAGBlockSetting extends BlockTypeSetting{
    private final int minimumLevels;
    private final int maximumLevels;
    private final int minimumLevelBreadth;
    private final int maximumLevelBreadth;
    private final int maximumAdjacencyDistance;
    private final int maximumNodeConnection;
    private final int minimumNodeConnection;

    public DAGBlockSetting(double probability, int minimumLevels, int maximumLevels, int minimumLevelBreadth, int maximumLevelBreadth, int maximumAdjacencyDistance, int minimumNodeConnection, int maximumNodeConnection) {
        super("DAG", probability);
        this.minimumLevels = minimumLevels;
        this.maximumLevels = maximumLevels;
        this.minimumLevelBreadth = minimumLevelBreadth;
        this.maximumLevelBreadth = maximumLevelBreadth;
        this.maximumAdjacencyDistance = maximumAdjacencyDistance;
        this.maximumNodeConnection = maximumNodeConnection;
        this.minimumNodeConnection = minimumNodeConnection;
    }

    public int getMinimumLevels() {
        return minimumLevels;
    }

    public int getMaximumLevels() {
        return maximumLevels;
    }

    public int getMinimumLevelBreadth() {
        return minimumLevelBreadth;
    }

    public int getMaximumLevelBreadth() {
        return maximumLevelBreadth;
    }

    public int getMaximumAdjacencyDistance() {
        return maximumAdjacencyDistance;
    }

    public int getMaximumNodeConnection() {
        return maximumNodeConnection;
    }

    public int getMinimumNodeConnection() {
        return minimumNodeConnection;
    }

}
