/*********************************************************************************************
 *
 *
 * 'SymbolFactory.java', in plugin 'msi.gama.core', is part of the source code of the
 * GAMA modeling and simulation platform.
 * (c) 2007-2014 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://code.google.com/p/gama-platform/ for license information and developers contact.
 *
 *
 **********************************************************************************************/
package msi.gaml.factories;

import java.util.List;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;

import gnu.trove.set.hash.TIntHashSet;
import msi.gaml.descriptions.IDescription;
import msi.gaml.descriptions.SymbolProto;
import msi.gaml.statements.Facets;

/**
 * Written by Alexis Drogoul Modified on 11 mai 2010
 *
 * @todo Description
 *
 */
// @factory(handles = { ISymbolKind.ENVIRONMENT })
public abstract class SymbolFactory {

	protected final TIntHashSet kindsHandled;

	public SymbolFactory(final List<Integer> handles) {
		kindsHandled = new TIntHashSet(handles);
	}

	TIntHashSet getHandles() {
		return kindsHandled;
	}

	protected abstract IDescription buildDescription(String keyword, Facets facets, EObject element,
			ChildrenProvider children, IDescription enclosing, SymbolProto proto, Set<String> dependencies);

}
