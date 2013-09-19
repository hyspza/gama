/**
 * Created by drogoul, 19 janv. 2012
 * 
 */
package msi.gama.gui.views.actions;

import msi.gama.gui.swt.IGamaIcons;
import msi.gama.gui.views.*;
import org.eclipse.jface.action.*;

/**
 * The class SnapshotItem.
 * 
 * @author drogoul
 * @since 19 janv. 2012
 * 
 */
public class SideBarItem extends GamaViewItem {

	/**
	 * @param view
	 */
	SideBarItem(final GamaViewPart view) {
		super(view);
	}

	/**
	 * @see msi.gama.gui.views.actions.GamaViewItem#createItem()
	 */
	@Override
	protected IContributionItem createItem() {
		IAction action =
			new GamaAction("Sidebar", "Show/hide side bar ", IAction.AS_PUSH_BUTTON,
				IGamaIcons.DISPLAY_TOOLBAR_SIDEBAR.descriptor()) {

				@Override
				public void run() {
					if ( view instanceof LayeredDisplayView ) {
						((LayeredDisplayView) view).toggleSideBar();
					}
				}

			};

		return new ActionContributionItem(action);
	}

}
