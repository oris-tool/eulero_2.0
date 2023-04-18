package org.oristool.eulero.modeling;

public interface DFSObserver {
    default boolean onOpen(Activity opened, Activity from) { return true; };
    default boolean onClose(Activity closed) { return true; };
    default boolean onSkip(Activity skipped, Activity from) { return true; };
}
