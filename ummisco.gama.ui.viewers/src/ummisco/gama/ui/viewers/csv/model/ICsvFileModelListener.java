/*********************************************************************************************
 *
 * 'ICsvFileModelListener.java, in plugin ummisco.gama.ui.viewers, is part of the source code of the
 * GAMA modeling and simulation platform.
 * (c) 2007-2016 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 * 
 *
 **********************************************************************************************/
package ummisco.gama.ui.viewers.csv.model;

/**
 *
 * @author fhenri
 *
 */
public interface ICsvFileModelListener {

    void entryChanged(CSVRow row, int rowIndex);

}