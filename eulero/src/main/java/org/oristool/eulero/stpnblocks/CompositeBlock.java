package org.oristool.eulero.stpnblocks;

import org.oristool.eulero.graph.Activity;
import org.oristool.petrinet.PetriNet;
import org.oristool.petrinet.Place;

import java.math.BigDecimal;
import java.util.List;

public abstract class CompositeBlock extends STPNBlock{
    private BigDecimal SimplifiedC;
    private BigDecimal SimplifiedR;
    private List<STPNBlock> children;

    // Qui devo implementare i metodi che mi permettono di generare un Composite Block
    // quindi avrò i vari metodi statici, che però mi richiameranno quelli sotto (AND, XOR; DAG, REPEAT, SEQ)
    public CompositeBlock(String name, List<STPNBlock> children) {
        super(name);
        this.children = children;
    }

    public static CompositeBlock SEQ(String name, List<STPNBlock> blocks){
        return null;
    }

    public static CompositeBlock AND(String name, List<STPNBlock> blocks){
        return null;
    }

    public static CompositeBlock XOR(String name, List<STPNBlock> blocks, List<Double> probs){
        return null;
    }

    public static CompositeBlock REP(String name, STPNBlock block, Double repProb){
        return null;
    }

    public List<STPNBlock> children() {
        return children;
    }
}
