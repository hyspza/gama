/**
 * generated by Xtext
 */
package gama.core.lang.gaml;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Action Arguments</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link gama.core.lang.gaml.ActionArguments#getArgs <em>Args</em>}</li>
 * </ul>
 *
 * @see gama.core.lang.gaml.GamlPackage#getActionArguments()
 * @model
 * @generated
 */
public interface ActionArguments extends EObject
{
  /**
   * Returns the value of the '<em><b>Args</b></em>' containment reference list.
   * The list contents are of type {@link gama.core.lang.gaml.ArgumentDefinition}.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the value of the '<em>Args</em>' containment reference list.
   * @see gama.core.lang.gaml.GamlPackage#getActionArguments_Args()
   * @model containment="true"
   * @generated
   */
  EList<ArgumentDefinition> getArgs();

} // ActionArguments