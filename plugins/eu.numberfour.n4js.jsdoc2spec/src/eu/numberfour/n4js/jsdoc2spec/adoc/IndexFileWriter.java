/**
 * Copyright (c) 2016 NumberFour AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   NumberFour AG - Initial API and implementation
 */
package eu.numberfour.n4js.jsdoc2spec.adoc;

/**
 * The index file contains a tree structure of all elements of the code base. The structure of the tree begins with the
 * repository and continues with the path, project, source folder, module folders, the module name, and ends with the
 * element name. In order to reduce the number of tabs, hence file size, some structure parts are collapsed. Each leave,
 * i.e. each code Element is followed by (1) its source code line in its original file, and the (2) beginning and (3)
 * end line of its generated adoc documentation. These numbers are separated by two colons (::). In case of polyfilled
 * files, the true location of a code element might be in a different source folder. If so, the true source folder is
 * specified in an additional postfix (again following on a double colon). <br/>
 * <br/>
 * A typical index file could start like this:
 *
 * <pre>
stdlib_api#packages
	eu.numberfour.stdlib.model.base.api#src.n4js
		n4.lang
			FixedPoint6.n4js
				FixedPoint6::72::31::105
				FixedPoint6#add::196::203::223
				FixedPoint6@fromString::137::507::526
				fp6::243::8::27
		n4.model
			AttributeDefaultState.n4js
				AttributeDefaultState::6::8::9
			Containable.n4js
				IContainable::12::8::9
				ITechnicallyContainable::6::13::14
			ContentViolationError.n4js
				ContentViolationError::10::8::12
				ContentViolationError#constructor::18::13::32
 * </pre>
 */
public class IndexFileWriter extends IndexEntryWriter {

	@Override
	protected String getFileName() {
		return FileSystem.INDEX_FILE_NAME;
	}

	@Override
	protected void appendEntry(IndexEntry ie) {
		boolean superDirChanged = false;
		for (int i = 0; i < ie.adocPathElems.length; i++) {
			if (superDirChanged || !isEqualFileName(ie, lastIE, i)) {
				superDirChanged = true;

				for (int j = 0; j < i; j++) {
					strb.append("\t");
				}
				String pathElem = setExtensionOnLastPathElem(ie, i);
				strb.append(pathElem);
				strb.append("\n");
			}
		}

		appendElement(ie);
	}

	private String setExtensionOnLastPathElem(IndexEntry ie, int i) {
		String pathElem = ie.adocPathElems[i];
		if (ie.adocPathElems.length - 1 == i)
			pathElem = pathElem.replace(".adoc", ie.extension);
		return pathElem;
	}

	private boolean isEqualFileName(IndexEntry ie1, IndexEntry ie2, int i) {
		if (ie1 == null || ie2 == null)
			return false;
		if (ie1.adocPathElems.length <= i || ie2.adocPathElems.length <= i)
			return false;

		return setExtensionOnLastPathElem(ie1, i).equals(setExtensionOnLastPathElem(ie2, i));
	}

	private void appendElement(IndexEntry ie) {
		strb.append("\t\t\t\t");
		strb.append(ie.element);
		strb.append(ie.delimiter);
		strb.append(ie.property);
		strb.append("::");
		strb.append(ie.sourceLine);
		strb.append("::");
		strb.append(ie.offsetStart);
		strb.append("::");
		strb.append(ie.offsetEnd);

		if (!ie.folder.equals(ie.trueFolder)) {
			strb.append("::");
			strb.append(ie.repository);
			strb.append(":");
			strb.append(ie.path.replace("/", "."));
			strb.append(":");
			strb.append(ie.project);
			strb.append(":");
			strb.append(ie.trueFolder.replace("/", "."));
		}

		strb.append(FileSystem.NL);
	}

}
