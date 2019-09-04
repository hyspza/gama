/*******************************************************************************************************
 *
 * gaml.factories.ModelFactory.java, in plugin gama.core, is part of the source code of the GAMA modeling and
 * simulation platform (v. 1.8)
 *
 * (c) 2007-2018 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compilation.factories;

import java.util.Collections;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;

import gama.processor.annotations.ISymbolKind;
import gama.processor.annotations.GamlAnnotations.factory;
import gaml.compilation.ast.ISyntacticElement;
import gaml.descriptions.IDescription;
import gaml.descriptions.ModelDescription;
import gaml.descriptions.SpeciesDescription;
import gaml.descriptions.SymbolProto;
import gaml.descriptions.ValidationContext;
import gaml.statements.Facets;

/**
 * Written by drogoul Modified on 27 oct. 2009
 *
 * @todo Description
 */
@factory (
		handles = { ISymbolKind.MODEL })
public class ModelFactory extends SymbolFactory {

	final ModelAssembler assembler = new ModelAssembler();

	public ModelFactory(final int... handles) {
		super(handles);
	}

	public ModelDescription createModelDescription(final String projectPath, final String modelPath,
			final Iterable<ISyntacticElement> models, final ValidationContext collector, final boolean document,
			final Map<String, ModelDescription> mm) {
		return assembler.assemble(projectPath, modelPath, models, collector, document, mm);
	}

	@SuppressWarnings ("rawtypes")
	public static ModelDescription createRootModel(final String name, final Class clazz, final SpeciesDescription macro,
			final SpeciesDescription parent) {
		ModelDescription.ROOT = new ModelDescription(name, clazz, "", "", null, macro, parent, null, null,
				ValidationContext.NULL, Collections.EMPTY_SET);
		return ModelDescription.ROOT;
	}

	@Override
	protected IDescription buildDescription(final String keyword, final Facets facets, final EObject element,
			final Iterable<IDescription> children, final IDescription enclosing, final SymbolProto proto) {
		// This method is actually never called.
		return null;
	}

}