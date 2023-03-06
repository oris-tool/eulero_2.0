package org.oristool.eulero.modeling.updates;

import org.oristool.eulero.modeling.Activity;
import org.oristool.petrinet.PetriNet;
import org.oristool.petrinet.Place;

import java.math.BigDecimal;
import java.math.BigInteger;

public class Simple extends Activity {
    @Override
    public Activity copyRecursive(String suffix) {
        return null;
    }

    @Override
    public BigInteger computeQ(boolean getSimplified) {
        return null;
    }

    @Override
    public void resetSupportBounds() {

    }

    @Override
    public void buildTPN(PetriNet pn, Place in, Place out, int prio) {

    }

    @Override
    public int buildSTPN(PetriNet pn, Place in, Place out, int prio) {
        return 0;
    }

    @Override
    public BigDecimal low() {
        return null;
    }

    @Override
    public BigDecimal upp() {
        return null;
    }

    @Override
    public boolean isWellNested() {
        return false;
    }
}
