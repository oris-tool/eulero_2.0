package org.oristool.eulero.modeling.updates;

public interface DFSNestedObserver extends DFSObserver{
    default boolean onNestedStart(Activity nested) { return true; };
    default boolean onNestedEnd(Activity nested) { return true; };
}
