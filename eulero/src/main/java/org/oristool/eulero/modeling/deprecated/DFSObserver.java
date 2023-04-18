/* This program is called EULERO.
 * Copyright (C) 2022 The EULERO Authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.oristool.eulero.modeling.deprecated;

/**
 * Callbacks for DFS.
 * 
 * The observer can modify the dependencies of a node during 
 * onOpen and onClose.
 * 
 * If a method returns false, the visit stops immediately.
 */
public interface DFSObserver {
    default boolean onOpen(Activity opened, Activity from) { return true; };
    default boolean onClose(Activity closed) { return true; };
    default boolean onSkip(Activity skipped, Activity from) { return true; };
}
