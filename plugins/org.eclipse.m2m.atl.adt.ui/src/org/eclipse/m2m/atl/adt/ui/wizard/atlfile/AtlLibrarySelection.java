/*******************************************************************************
 * Copyright (c) 2009 Obeo.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Obeo - initial API and implementation
 *******************************************************************************/
package org.eclipse.m2m.atl.adt.ui.wizard.atlfile;

import java.util.Arrays;
import java.util.Set;

import org.eclipse.m2m.atl.adt.ui.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * The library selection dialog.
 * 
 * @author <a href="mailto:william.piers@obeo.fr">William Piers</a>
 */
public class AtlLibrarySelection extends AbstractAtlSelection {

	private static final String DEFAULT_NAME = "LIB"; //$NON-NLS-1$

	private Text libraryNameText;

	/**
	 * Constructor.
	 * 
	 * @param parentScreen
	 *            the parent screen
	 * @param parent
	 *            the parent shell
	 * @param title
	 *            the title
	 */
	public AtlLibrarySelection(AtlFileScreen parentScreen, Shell parent, String title) {
		super(parentScreen, parent, title);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.ui.dialogs.SelectionStatusDialog#computeResult()
	 */
	@Override
	protected void computeResult() {
		setResult(Arrays.asList(new String[] {libraryNameText.getText()}));
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.verticalSpacing = 15;
		container.setLayout(layout);
		GridData data = new GridData(GridData.FILL_BOTH);
		container.setLayoutData(data);

		Group namingSection = new Group(container, SWT.NULL);
		namingSection.setText(Messages.getString("AtlLibrarySelection.NAMING")); //$NON-NLS-1$
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 10;
		namingSection.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		namingSection.setLayoutData(data);

		new Label(namingSection, SWT.NULL).setText(Messages.getString("AtlLibrarySelection.LIBRARY_NAME")); //$NON-NLS-1$
		libraryNameText = new Text(namingSection, SWT.BORDER);
		libraryNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		libraryNameText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				textChanged();
			}
		});
		return container;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.m2m.atl.adt.ui.wizard.atlfile.AbstractAtlSelection#create()
	 */
	@Override
	public void create() {
		super.create();
		libraryNameText.setText(getDefaultName(DEFAULT_NAME, getExistingLibraries()));
		libraryNameText.selectAll();
	}

	private void textChanged() {
		String message = checkText(libraryNameText.getText());
		if (message != null) {
			nok(message);
		} else {
			ok();
		}
	}

	private String checkText(String text) {
		if (text == null || "".equals(text)) { //$NON-NLS-1$
			return Messages.getString("AtlLibrarySelection.SET_NAME"); //$NON-NLS-1$
		} else if (!text.matches(NAMING_REGEX)) {
			return Messages.getString("AtlLibrarySelection.INVALID_NAME"); //$NON-NLS-1$
		} else if (getExistingLibraries().contains(text)) {
			return Messages.getString("AtlLibrarySelection.LIBRARY_EXISTS"); //$NON-NLS-1$
		}
		return null;
	}

	private Set<String> getExistingLibraries() {
		return parentScreen.getLibraries().keySet();
	}

}