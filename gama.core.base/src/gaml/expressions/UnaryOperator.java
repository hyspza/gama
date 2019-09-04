/*******************************************************************************************************
 *
 * gaml.expressions.UnaryOperator.java, in plugin gama.core, is part of the source code of the GAMA modeling and
 * simulation platform (v. 1.8)
 *
 * (c) 2007-2018 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.expressions;

import static gama.processor.annotations.ITypeProvider.CONTENT_TYPE_AT_INDEX;
import static gama.processor.annotations.ITypeProvider.DENOTED_TYPE_AT_INDEX;
import static gama.processor.annotations.ITypeProvider.FIRST_CONTENT_TYPE_OR_TYPE;
import static gama.processor.annotations.ITypeProvider.FIRST_ELEMENT_CONTENT_TYPE;
import static gama.processor.annotations.ITypeProvider.FLOAT_IN_CASE_OF_INT;
import static gama.processor.annotations.ITypeProvider.KEY_TYPE_AT_INDEX;
import static gama.processor.annotations.ITypeProvider.TYPE_AT_INDEX;
import static gama.processor.annotations.ITypeProvider.WRAPPED;

import java.util.Collection;
import java.util.function.Predicate;

import gama.common.preferences.GamaPreferences;
import gama.common.util.TextBuilder;
import gama.runtime.exceptions.GamaRuntimeException;
import gama.runtime.scope.IScope;
import gaml.compilation.GAML;
import gaml.compilation.interfaces.GamaGetter;
import gaml.descriptions.IDescription;
import gaml.descriptions.OperatorProto;
import gaml.descriptions.SpeciesDescription;
import gaml.descriptions.VariableDescription;
import gaml.types.GamaType;
import gaml.types.IContainerType;
import gaml.types.IType;
import gaml.types.Types;

/**
 * The Class UnaryOperator.
 */
@SuppressWarnings ({ "rawtypes" })
public class UnaryOperator extends AbstractExpression implements IOperator {

	final protected IExpression child;
	final OperatorProto prototype;

	public static IExpression create(final OperatorProto proto, final IDescription context, final IExpression child) {
		final UnaryOperator u = new UnaryOperator(proto, context, child);
		if (u.isConst() && GamaPreferences.External.CONSTANT_OPTIMIZATION.getValue()) {
			final IExpression e =
					GAML.getExpressionFactory().createConst(u.getConstValue(), u.getGamlType(), u.serialize(false));
			return e;
		}
		return u;
	}

	@Override
	public boolean isConst() {
		return prototype.canBeConst && child.isConst();
	}

	@Override
	public String getDefiningPlugin() {
		return prototype.getDefiningPlugin();
	}

	public UnaryOperator(final OperatorProto proto, final IDescription context, final IExpression... child) {
		// setName(proto.getName());
		this.child = child[0];
		this.prototype = proto;
		if (proto != null) {
			type = proto.returnType;
			computeType();
			proto.verifyExpectedTypes(context, child[0].getGamlType().getContentType());
		}
	}

	@Override
	public Object _value(final IScope scope) throws GamaRuntimeException {
		final Object childValue = prototype.lazy[0] ? child : child.value(scope);
		try {
			return ((GamaGetter.Unary) prototype.helper).get(scope, childValue);
		} catch (final GamaRuntimeException e1) {
			e1.addContext("when applying the " + literalValue() + " operator on " + childValue);
			throw e1;
		} catch (final Throwable e) {
			// DEBUG.LOG(e + " when applying the " + literalValue() + "
			// operator on " + childValue);
			final GamaRuntimeException ee = GamaRuntimeException.create(e, scope);
			ee.addContext("when applying the " + literalValue() + " operator on " + childValue);
			throw ee;
		}
	}

	@Override
	public String serialize(final boolean includingBuiltIn) {
		final String s = literalValue();
		try (TextBuilder sb = TextBuilder.create()) {
			if (OperatorProto.noMandatoryParenthesis.contains(s)) {
				parenthesize(sb, child);
			} else {
				sb.append("(").append(child.serialize(includingBuiltIn)).append(")");
			}
			return sb.toString();
		}
	}

	@Override
	public boolean shouldBeParenthesized() {
		return false;
	}

	@Override
	public String toString() {
		return literalValue() + "(" + child + ")";
	}

	@Override
	public String getTitle() {
		try (TextBuilder sb = TextBuilder.create()) {
			sb.append("operator ").append(getName()).append(" (");
			sb.append(child == null ? prototype.signature : child.getGamlType().getTitle());
			sb.append(") returns ").append(getGamlType().getTitle());
			return sb.toString();
		}
	}

	@Override
	public String getDocumentation() {
		return prototype.getDocumentation();
	}

	private IType computeType(final int theType, final IType def) {
		int t = theType;
		final boolean returnFloatsInsteadOfInts = t < FLOAT_IN_CASE_OF_INT;
		if (returnFloatsInsteadOfInts) {
			t = t - FLOAT_IN_CASE_OF_INT;
		}
		IType result = def;
		if (t == WRAPPED) {
			result = child.getGamlType().getWrappedType();
		} else if (t == FIRST_ELEMENT_CONTENT_TYPE) {
			if (child instanceof ListExpression) {
				final IExpression[] array = ((ListExpression) child).getElements();
				if (array.length == 0) {
					result = Types.NO_TYPE;
				} else {
					result = array[0].getGamlType().getContentType();
				}
			} else if (child instanceof MapExpression) {
				final IExpression[] array = ((MapExpression) child).valuesArray();
				if (array.length == 0) {
					result = Types.NO_TYPE;
				} else {
					result = array[0].getGamlType().getContentType();
				}
			} else {
				final IType tt = child.getGamlType().getContentType().getContentType();
				if (tt != Types.NO_TYPE) {
					result = tt;
				}
			}
		} else if (t == FIRST_CONTENT_TYPE_OR_TYPE) {
			final IType firstType = child.getGamlType();
			final IType t2 = firstType.getContentType();
			if (t2 == Types.NO_TYPE) {
				result = firstType;
			} else {
				result = t2;
			}
		} else {
			result = t == TYPE_AT_INDEX + 1 ? child.getGamlType()
					: t == CONTENT_TYPE_AT_INDEX + 1 ? child.getGamlType().getContentType() : t == KEY_TYPE_AT_INDEX + 1
							? child.getGamlType().getKeyType()
							: t >= 0 ? Types.get(t) : t == DENOTED_TYPE_AT_INDEX + 1 ? child.getDenotedType() : def;
		}
		if (returnFloatsInsteadOfInts && result == Types.INT) { return Types.FLOAT; }
		return result;
	}

	protected void computeType() {
		type = computeType(prototype.typeProvider, type);
		if (type.isContainer()) {
			IType contentType = computeType(prototype.contentTypeProvider, type.getContentType());
			if (contentType.isContainer()) {
				// WARNING Special case for pairs of map. See if it works for other
				// fields as well
				if (contentType.getKeyType() == Types.NO_TYPE && contentType.getContentType() == Types.NO_TYPE) {
					contentType = GamaType.from(contentType, child.getGamlType().getKeyType(),
							child.getGamlType().getContentType());
				}
				final IType contentContentType =
						computeType(prototype.contentTypeContentTypeProvider, contentType.getContentType());
				contentType = ((IContainerType<?>) contentType).of(contentContentType);
			}

			final IType keyType = computeType(prototype.keyTypeProvider, type.getKeyType());
			type = GamaType.from(type, keyType, contentType);

		}

	}

	@Override
	public IOperator resolveAgainst(final IScope scope) {
		return new UnaryOperator(prototype, null, child.resolveAgainst(scope));
	}

	@Override
	public String getName() {
		return prototype.getName();
	}

	@Override
	public IExpression arg(final int i) {
		return i == 0 ? child : null;
	}

	// @Override
	// public void collectMetaInformation(final GamlProperties meta) {
	// prototype.collectMetaInformation(meta);
	// child.collectMetaInformation(meta);
	// }

	@Override
	public void collectUsedVarsOf(final SpeciesDescription species, final Collection<VariableDescription> result) {
		prototype.collectImplicitVarsOf(species, result);
		child.collectUsedVarsOf(species, result);
	}

	@Override
	public boolean isContextIndependant() {
		return child.isContextIndependant();
	}

	@Override
	public OperatorProto getPrototype() {
		return prototype;
	}

	@Override
	public void visitSuboperators(final IOperatorVisitor visitor) {
		if (child instanceof IOperator) {
			visitor.visit((IOperator) child);
		}

	}

	@Override
	public boolean findAny(final Predicate<IExpression> predicate) {
		if (predicate.test(this)) { return true; }
		return child != null && child.findAny(predicate);
	}

}