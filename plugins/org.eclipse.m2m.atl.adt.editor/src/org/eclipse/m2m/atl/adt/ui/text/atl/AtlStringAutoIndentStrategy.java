/*******************************************************************************
 * Copyright (c) 2004 INRIA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    INRIA - initial API and implementation
 *******************************************************************************/
package org.eclipse.m2m.atl.adt.ui.text.atl;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultIndentLineAutoEditStrategy;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.m2m.atl.adt.ui.AtlPreferenceConstants;
import org.eclipse.m2m.atl.adt.ui.AtlUIPlugin;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.texteditor.ITextEditorExtension3;

/**
 * Auto indent strategy for strings.
 */
public class AtlStringAutoIndentStrategy extends DefaultIndentLineAutoEditStrategy {

	private String fPartitioning;

	/**
	 * Creates a new string auto indent strategy for the given document partitioning.
	 * 
	 * @param partitioning
	 *            the document partitioning
	 */
	public AtlStringAutoIndentStrategy(String partitioning) {
		super();
		fPartitioning = partitioning;
	}

	private void atlStringIndentAfterNewLine(IDocument document, DocumentCommand command)
			throws BadLocationException {

		ITypedRegion partition = TextUtilities.getPartition(document, fPartitioning, command.offset, true);
		int offset = partition.getOffset();
		int length = partition.getLength();

		if (command.offset == offset + length && document.getChar(offset + length - 1) == '\'') {
			return;
		}

		String indentation = getLineIndentation(document, command.offset);
		String delimiter = TextUtilities.getDefaultLineDelimiter(document);

		IRegion line = document.getLineInformationOfOffset(offset);
		String string = document.get(line.getOffset(), offset - line.getOffset());
		if (string.trim().length() != 0) {
			indentation += String.valueOf("\t\t"); //$NON-NLS-1$
		}

		IPreferenceStore preferenceStore = AtlUIPlugin.getDefault().getPreferenceStore();
		if (isLineDelimiter(document, command.text)) {
			command.text = "\' +" + command.text + indentation + "\'"; //$NON-NLS-1$ //$NON-NLS-2$
		} else if (command.text.length() > 1
				&& preferenceStore.getBoolean(AtlPreferenceConstants.TYPING_ESCAPE_STRINGS)) {
			command.text = getModifiedText(command.text, indentation, delimiter);
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.jface.text.DefaultIndentLineAutoEditStrategy#customizeDocumentCommand(org.eclipse.jface.text.IDocument, org.eclipse.jface.text.DocumentCommand)
	 */
	@Override
	public void customizeDocumentCommand(IDocument document, DocumentCommand command) {
		try {
			if (command.length != 0 || command.text == null)
				return;

			IPreferenceStore preferenceStore = AtlUIPlugin.getDefault().getPreferenceStore();

			if (preferenceStore.getBoolean(AtlPreferenceConstants.TYPING_WRAP_STRINGS) && isSmartMode()) {
				atlStringIndentAfterNewLine(document, command);
			}

		} catch (BadLocationException e) {
			// do nothing
		}
	}

	/**
	 * The input string doesn't contain any line delimiter.
	 * 
	 * @param inputString
	 *            the given input string
	 * @return the displayable string.
	 */
	private String displayString(String inputString, String indentation, String delimiter) {

		int length = inputString.length();
		StringBuffer buffer = new StringBuffer(length);
		java.util.StringTokenizer tokenizer = new java.util.StringTokenizer(inputString, "\n\r", true); //$NON-NLS-1$
		while (tokenizer.hasMoreTokens()) {

			String token = tokenizer.nextToken();
			if (token.equals("\r")) { //$NON-NLS-1$
				buffer.append("\\r"); //$NON-NLS-1$
				if (tokenizer.hasMoreTokens()) {
					token = tokenizer.nextToken();
					if (token.equals("\n")) { //$NON-NLS-1$
						buffer.append("\\n"); //$NON-NLS-1$
						buffer.append("\' + " + delimiter); //$NON-NLS-1$
						buffer.append(indentation);
						buffer.append("\'"); //$NON-NLS-1$
						continue;
					} else {
						buffer.append("\' + " + delimiter); //$NON-NLS-1$
						buffer.append(indentation);
						buffer.append("\'"); //$NON-NLS-1$
					}
				} else {
					continue;
				}
			} else if (token.equals("\n")) { //$NON-NLS-1$
				buffer.append("\\n"); //$NON-NLS-1$
				buffer.append("\' + " + delimiter); //$NON-NLS-1$
				buffer.append(indentation);
				buffer.append("\'"); //$NON-NLS-1$
				continue;
			}

			StringBuffer tokenBuffer = new StringBuffer();
			for (int i = 0; i < token.length(); i++) {
				char c = token.charAt(i);
				switch (c) {
					case '\r':
						tokenBuffer.append("\\r"); //$NON-NLS-1$
						break;
					case '\n':
						tokenBuffer.append("\\n"); //$NON-NLS-1$
						break;
					case '\b':
						tokenBuffer.append("\\b"); //$NON-NLS-1$
						break;
					case '\t':
						// keep tabs verbatim
						tokenBuffer.append("\t"); //$NON-NLS-1$
						break;
					case '\f':
						tokenBuffer.append("\\f"); //$NON-NLS-1$
						break;
					case '\"':
						tokenBuffer.append("\\\""); //$NON-NLS-1$
						break;
					case '\'':
						tokenBuffer.append("\\'"); //$NON-NLS-1$
						break;
					case '\\':
						tokenBuffer.append("\\\\"); //$NON-NLS-1$
						break;
					default:
						tokenBuffer.append(c);
				}
			}
			buffer.append(tokenBuffer);
		}
		return buffer.toString();
	}

	private String getLineIndentation(IDocument document, int offset) throws BadLocationException {

		// find start of line
		int adjustedOffset = offset == document.getLength() ? offset - 1 : offset;
		IRegion line = document.getLineInformationOfOffset(adjustedOffset);
		int start = line.getOffset();

		// find white spaces
		int end = findEndOfWhiteSpace(document, start, offset);

		return document.get(start, end - start);
	}

	private String getModifiedText(String string, String indentation, String delimiter) {
		return displayString(string, indentation, delimiter);
	}

	private boolean isLineDelimiter(IDocument document, String text) {
		String[] delimiters = document.getLegalLineDelimiters();
		if (delimiters != null) {
			return TextUtilities.equals(delimiters, text) > -1;
		}
		return false;
	}

	private boolean isSmartMode() {
		IWorkbenchPage page = AtlUIPlugin.getActivePage();
		if (page != null) {
			IEditorPart part = page.getActiveEditor();
			if (part instanceof ITextEditorExtension3) {
				ITextEditorExtension3 extension = (ITextEditorExtension3)part;
				return extension.getInsertMode() == ITextEditorExtension3.SMART_INSERT;
			}
		}
		return false;
	}
}
