package org.nilis.nilismath.essentials;

import java.util.Collection;
import java.util.Set;


public interface DataCollectionAtomsFinder<TData> {
	Set<TData> findAtoms(Set<TData> input);
}
