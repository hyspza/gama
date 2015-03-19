/**
 * Created by drogoul, 22 nov. 2014
 * 
 */
package msi.gama.gui.swt.controls;

import msi.gama.gui.swt.*;
import msi.gama.gui.swt.GamaColors.GamaUIColor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public class FlatButtonAsLabel extends CLabel implements /* PaintListener, */Listener {

	public static FlatButtonAsLabel create(final Composite comp, final int style) {
		return new FlatButtonAsLabel(comp, style);
	}

	public static FlatButtonAsLabel label(final Composite comp, final GamaUIColor color, final String text) {
		return button(comp, color, text).disabled();
	}

	public static FlatButtonAsLabel label(final Composite comp, final GamaUIColor color, final String text, final Image image) {
		FlatButtonAsLabel label = label(comp, color, text);
		label.setImage(image);
		return label;
	}

	public static FlatButtonAsLabel button(final Composite comp, final GamaUIColor color, final String text) {
		FlatButtonAsLabel button = create(comp, SWT.None);
		button.setText(text);
		button.setColor(color);
		return button;
	}

	public static FlatButtonAsLabel
		button(final Composite comp, final GamaUIColor color, final String text, final Image image) {
		FlatButtonAsLabel button = button(comp, color, text);
		button.setImage(image);
		return button;
	}

	public static FlatButtonAsLabel menu(final Composite comp, final GamaUIColor color, final String text) {
		FlatButtonAsLabel button = button(comp, color, text)/* .setImageStyle(IMAGE_RIGHT) */;
		button.setImage(GamaIcons.create("small.dropdown").image());
		return button;
	}

	private static int FIXED_HEIGHT = 22;

	private int height = FIXED_HEIGHT;
	// private int width;
	// private String text;
	private RGB colorCode;
	// private final int innerMarginWidth = 5;
	// private final int imagePadding = 5;
	private boolean enabled = true;
	private boolean hovered = false;

	public static int IMAGE_LEFT = 0;
	public static int IMAGE_RIGHT = 1;

	// private final int imageStyle = IMAGE_LEFT;

	private FlatButtonAsLabel(final Composite parent, final int style) {
		super(parent, style | SWT.NO_BACKGROUND);
		setBackground(parent.getBackground());
		setFont(SwtGui.getLabelfont());
		// this.setBackgroundMode(SWT.INHERIT_DEFAULT);
		// addPaintListener(this);
		addListeners();
	}

	public int getHeight() {
		return height;
	}

	@Override
	public void handleEvent(final Event e) {
		switch (e.type) {
			case SWT.MouseExit:
				doHover(false);
				break;
			case SWT.MouseEnter:
			case SWT.MouseHover:
				doHover(true);
				e.doit = true;
				break;
			case SWT.MouseUp:
				if ( e.button == 1 && e.count == 1 && getClientArea().contains(e.x, e.y) ) {
					doButtonClicked();
				}
				break;
			case SWT.MouseDown:
				if ( e.button == 1 ) {
					doHover(true);
				}
		}
	}

	/**
	 * SelectionListeners are notified when the button is clicked
	 * 
	 * @param listener
	 */
	public void addSelectionListener(final SelectionListener listener) {
		addListener(SWT.Selection, new TypedListener(listener));
	}

	public void removeSelectionListener(final SelectionListener listener) {
		removeListener(SWT.Selection, listener);
	}

	private void doButtonClicked() {
		if ( !enabled ) { return; }
		Event e = new Event();
		e.item = this;
		e.widget = this;
		e.type = SWT.Selection;
		notifyListeners(SWT.Selection, e);
	}

	private void doHover(final boolean hover) {
		if ( hover && hovered || !hover && !hovered ) { return; }
		hovered = hover;
		if ( hovered ) {
			setForeground(IGamaColors.BLACK.color());
		} else {
			setForeground(GamaColors.get(colorCode).color());
		}

	}

	//
	// private Image getImage() {
	// return super.getBackgroundImage();
	// }

	// @Override
	// public void paintControl(final PaintEvent e) {
	// // Init GC
	// GC gc = e.gc;
	// // gc.setAntialias(SWT.ON);
	// // gc.setAdvanced(true);
	// gc.setFont(getFont());
	// int width = getSize().x;
	// int v_inset = e.height < height ? 1 : (e.height - height) / 2 + 1;
	// int h_inset = e.width < width ? 5 : (e.width - width) / 2 + 5;
	// width = Math.min(width, getParent().getBounds().width);
	// Rectangle rect = new Rectangle(h_inset, v_inset, width - h_inset, height - 1);
	//
	// // add transparency by making the canvas background the same as
	// // the parent background (only needed for rounded corners)
	// gc.setBackground(getParent().getBackground());
	// gc.fillRectangle(rect);
	// gc.setForeground(IGamaColors.BLACK.color());
	// // gc.setForeground(color.isDark() ? IGamaColors.WHITE.color() : IGamaColors.BLACK.color());
	// gc.drawRoundRectangle(rect.x, rect.y, rect.width - 1, rect.height - 1, 3, 6);
	// GamaUIColor color = GamaColors.get(colorCode);
	// gc.setForeground(/* !enabled ? color.inactive() : */hovered ? /* color.darker() */IGamaColors.BLACK.color()
	// : color.color());
	//
	// int x = this.innerMarginWidth + h_inset;
	// int y_image = v_inset;
	// Image image = getImage();
	// if ( image != null ) {
	// y_image += (height - image.getBounds().height) / 2 - 1;
	// }
	// int y_text = v_inset;
	// String text = newText();
	// if ( text != null ) {
	// y_text += (height - gc.textExtent(text).y) / 2 - 1;
	// }
	//
	// if ( imageStyle == IMAGE_RIGHT ) {
	// gc.drawText(text, x, y_text, SWT.DRAW_TRANSPARENT);
	// if ( image != null ) {
	// x = rect.width + h_inset - x - image.getBounds().width + imagePadding;
	// drawImage(gc, image, x, y_image);
	// }
	// } else {
	// x = drawImage(gc, image, x, y_image);
	// gc.drawText(text, x, y_text, SWT.DRAW_TRANSPARENT);
	// }
	// // if ( text.equals("Tutorials/Incremental Model/models/Incremental Model 5.gaml") ) {
	// // System.out.println("Button " + text + "; rect: " + rect + "; parent bounds " + getParent().getBounds());
	// // }
	// }

	// private int drawImage(final GC gc, final Image image, final int x, final int y) {
	// if ( image == null ) { return x; }
	// gc.drawImage(image, x, y);
	// return x + image.getBounds().width + imagePadding;
	// }

	// @Override
	// public Point computeSize(final int wHint, final int hHint, final boolean changed) {
	// int width = 0;
	// if ( wHint != SWT.DEFAULT ) {
	// width = wHint;
	// } else {
	// width = computeMinWidth();
	// }
	// int clientWidth = getClientArea().width;
	// Point result = new Point(Math.max(width + 2, clientWidth), height);
	// // System.out.println(text + ": wHint " + wHint + "; client area " + getClientArea() + "; result " + result);
	// return result;
	// }

	// public int computeMinWidth() {
	// int width = 0;
	// Image image = getImage();
	// if ( image != null ) {
	// Rectangle bounds = image.getBounds();
	// width = bounds.width + imagePadding * 2;
	// }
	// if ( text != null ) {
	// GC gc = new GC(this);
	// gc.setFont(getFont());
	// Point extent = gc.textExtent(text);
	// gc.dispose();
	// width += extent.x + this.innerMarginWidth * 2;
	// }
	// return width;
	// }
	//
	// public String newText() {
	// if ( text == null ) { return null; }
	// int parentWidth = getParent().getBounds().width;
	// int width = computeMinWidth();
	// if ( parentWidth < width ) {
	// int imageWidth = 0;
	// Image image = getImage();
	// if ( image != null ) {
	// Rectangle bounds = image.getBounds();
	// imageWidth = bounds.width + imagePadding;
	// }
	// float r = (float) (parentWidth - imageWidth) / (float) width;
	// int nbChars = text.length();
	// int newNbChars = Math.max(0, (int) (nbChars * r));
	// String newText =
	// text.substring(0, newNbChars / 2) + "..." + text.substring(nbChars - newNbChars / 2, nbChars);
	// // System.out.println("Parent width =" + parentWidth + "; new nb chars = " + newNbChars + "; new text = " +
	// // newText);
	// return newText;
	// }
	// return text;
	// }

	/**
	 * This is an image that will be displayed to the side of the
	 * text inside the button (if any). By default the image will be
	 * to the left of the text; however, setImageStyle can be used to
	 * specify that it's either to the right or left. If there is no
	 * text, the image will be centered inside the button.
	 * 
	 * @param image
	 */
	// public FlatButton setImage(final Image image) {
	// super.setBackgroundImage(image);
	// redraw();
	// return this;
	// }

	/**
	 * Set the style with which the side image is drawn, either IMAGE_LEFT
	 * or IMAGE_RIGHT (default is IMAGE_LEFT).
	 * 
	 * @param imageStyle
	 */
	// public FlatButton setImageStyle(final int imageStyle) {
	// this.imageStyle = imageStyle;
	// return this;
	// }
	//
	// public int getImageStyle() {
	// return imageStyle;
	// }

	//
	// public String getText() {
	// return text;
	// }
	//
	// public FlatButton setText(final String text) {
	// if ( text == null ) { return this; }
	// if ( text.equals(this.text) ) { return this; }
	// this.text = text;
	// redraw();
	// return this;
	// }

	private void addListeners() {
		addListener(SWT.MouseDown, this);
		addListener(SWT.MouseExit, this);
		addListener(SWT.MouseEnter, this);
		addListener(SWT.MouseHover, this);
		addListener(SWT.MouseUp, this);
	}

	@Override
	public void setEnabled(final boolean enabled) {
		boolean oldSetting = this.enabled;
		this.enabled = enabled;
		// boolean oldSetting = super.getEnabled();
		// super.setEnabled(enabled);
		if ( oldSetting != enabled ) {
			if ( enabled ) {
				addListeners();
			} else {
				removeListener(SWT.MouseDown, this);
				removeListener(SWT.MouseExit, this);
				removeListener(SWT.MouseEnter, this);
				removeListener(SWT.MouseHover, this);
				removeListener(SWT.MouseUp, this);
			}
			redraw();
		}
	}

	public ToolItem item() {
		if ( getParent() instanceof GamaToolbarSimple ) {
			GamaToolbarSimple p = (GamaToolbarSimple) getParent();
			return p.control(this, computeSize(SWT.DEFAULT, height, false).x + 4);
		}
		ToolItem t = new ToolItem((ToolBar) getParent(), SWT.SEPARATOR);
		int width = this.computeSize(SWT.DEFAULT, height, false).x + 4;
		t.setControl(this);
		t.setWidth(width);
		return t;
	}

	// public ToolItem item(final int side /* SWT.LEFT or SWT.RIGHT */) {
	// if ( getParent() instanceof GamaToolbar2 ) {
	// GamaToolbar2 p = (GamaToolbar2) getParent();
	// return p.control(this, computeSize(SWT.DEFAULT, height, false).x + 4, side);
	// }
	// ToolItem t = new ToolItem((ToolBar) getParent(), SWT.SEPARATOR);
	// int width = this.computeSize(SWT.DEFAULT, height, false).x + 4;
	// t.setControl(this);
	// t.setWidth(width);
	// return t;
	// }

	public FlatButtonAsLabel disabled() {
		setEnabled(false);
		return this;
	}

	public FlatButtonAsLabel enabled() {
		setEnabled(true);
		return this;
	}

	public FlatButtonAsLabel light() {
		// if ( getFont().equals(SwtGui.getParameterEditorsFont()) ) { return this; }
		// setFont(SwtGui.getParameterEditorsFont());
		// redraw();
		return this;
	}

	public FlatButtonAsLabel small() {
		if ( height == 20 ) { return this; }
		height = 20;
		redraw();
		return this;
	}

	public FlatButtonAsLabel setColor(final GamaUIColor c) {
		RGB oldColorCode = colorCode;
		RGB newColorCode = c.getRGB();
		if ( newColorCode.equals(oldColorCode) ) { return this; }
		colorCode = c.getRGB();
		setForeground(c.color());
		// redraw();
		return this;
	}

	/**
	 * @return
	 */
	public GamaUIColor getColor() {
		return GamaColors.get(colorCode);
	}
}