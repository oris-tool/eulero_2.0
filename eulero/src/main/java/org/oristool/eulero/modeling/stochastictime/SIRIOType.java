package org.oristool.eulero.modeling.stochastictime;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;

@XmlType(name = "StochasticBidType")
@XmlEnum
public enum SIRIOType {
    UNI,
    EXP,
    DET,
    IMM,
    EXPO,
    HIST
}
