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
package eu.numberfour.n4js.ui.wizard.classifiers

import com.google.inject.Inject
import eu.numberfour.n4js.projectModel.IN4JSCore
import eu.numberfour.n4js.projectModel.IN4JSProject
import eu.numberfour.n4js.ui.wizard.generator.N4JSImportRequirementResolver
import eu.numberfour.n4js.ui.wizard.generator.N4JSImportRequirementResolver.ImportAnalysis
import eu.numberfour.n4js.ui.wizard.generator.N4JSImportRequirementResolver.ImportRequirement
import eu.numberfour.n4js.ui.wizard.generator.WizardGeneratorHelper
import eu.numberfour.n4js.ui.wizard.generator.ContentBlock
import eu.numberfour.n4js.ui.wizard.generator.WorkspaceWizardGenerator
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import java.util.List
import java.util.Map
import org.eclipse.core.resources.IFile
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.core.runtime.CoreException
import org.eclipse.emf.common.util.URI
import org.apache.log4j.Logger
import org.eclipse.core.runtime.IProgressMonitor
import eu.numberfour.n4js.ui.wizard.classifiers.N4JSClassifierWizardModel

/**
 * A file generator for a {@link N4JSClassifierWizardModel}
 */
abstract class N4JSNewClassifierWizardGenerator<M extends N4JSClassifierWizardModel> implements WorkspaceWizardGenerator<M>{

	@Inject
	private IN4JSCore n4jsCore;

	@Inject
	private extension WizardGeneratorHelper generatorUtils;
	
	@Inject
	private N4JSImportRequirementResolver requirementResolver;
	
	private val LOGGER = Logger.getLogger(eu.numberfour.n4js.ui.wizard.classifiers.N4JSNewClassifierWizardGenerator);
	
	override generateContentPreview(M model) {
		val modulePath = model.computeFileLocation;

		// For classifiers with existing target module files
		if (generatorUtils.exists(modulePath)) {
			// Retrieve resource
			val resource = getResource(URI.createPlatformResourceURI(modulePath.toString, true));
			val requirements = model.importRequirements;
			
			val importAnalysis = requirementResolver.analyzeImportRequirements(requirements, resource);
			
			// Retrieve file content
			val workspaceRoot = ResourcesPlugin.workspace.root;	
			val file = workspaceRoot.getFile(modulePath);
			
			val fileContent = try { 
				readFileAsString(file)
			} catch (Exception e) {
				LOGGER.error("Failed to create a content preview by overlaying existing module " + modulePath.toString);
				return #[];
			}
			
			// Generate contents
			val importStatements = requirementResolver.generateImportStatements(importAnalysis.importRequirements);
			val classCode = generateClassifierCode(model, importAnalysis.aliasBindings);
			
			val importStatementOffset = requirementResolver.getImportStatementOffset(resource);
			
			val importStatementFileContent = fileContent.substring(0, importStatementOffset);
			
			// Allow for empty body files  
			val bodyFileContent = if (fileContent.length > 0) { 
				fileContent.substring(importStatementOffset, fileContent.length).addLineBreak
			} else {
				""
			}
			
			// Return to be inserted content as highlighted and 
			// the existing file content as unhighlighted.
			return #[
				ContentBlock.unhighlighted(importStatementFileContent),
				ContentBlock.highlighted(importStatements.addLineBreak),
				ContentBlock.unhighlighted(bodyFileContent),
				ContentBlock.highlighted(classCode)
			] 
		} else { // For new target module files
			//Collect the import requirements
			val importRequirements = model.importRequirements;
			
			//Resolve occurring name conflicts
			val aliasBindings = requirementResolver.resolveImportNameConflicts(importRequirements, null)
			var importStatements = requirementResolver.generateImportStatements(importRequirements);
			
			if (!importStatements.empty) {
				importStatements += "\n\n";
			}
			
			return #[ ContentBlock.highlighted(importStatements + generateClassifierCode(model, aliasBindings)) ];	
		}
	}
	

	/**
	 * Writes the new classifier specified by the model to a file.
	 *
	 * Depending on the model's module specifier the classifier will be written to a new file or inserted into an existing file.
	 *
	 * <p>Note: Make sure to only a pass a valid model, no model validation is done. </p>
	 *
	 * @param model The classifier wizard model to write to file
	 *
	 */
	override boolean writeToFile(M model, IProgressMonitor monitor) {
		val modulePath = model.computeFileLocation();
		val moduleFile = ResourcesPlugin.workspace.root.getFile(modulePath);

		try {
			if (moduleFile.exists) {
				insertIntoFile(moduleFile, model);
			} else {
				//Collect the import requirements
				val importRequirements = model.importRequirements;
				
				//Resolve occurring name conflicts
				val aliasBindings = requirementResolver.resolveImportNameConflicts(importRequirements, null)

				//Generate import statements
				var importStatements = requirementResolver.generateImportStatements(importRequirements)
				//If non empty import Statements add line break after statements and an additional empty line to have some space to the code
				if (!importRequirements.empty)
					importStatements = importStatements + "\n\n";

				//Generate classifier code
				val classifierCode = generateClassifierCode(model, aliasBindings);

				//Write to file
				val classifierTextStream = new ByteArrayInputStream((importStatements+classifierCode).getBytes(StandardCharsets.UTF_8));
				moduleFile.create(classifierTextStream, true, null);
			}
		} catch (CoreException e) {
			return false;
		}

		return true;
	}

	
	/**
	 * Performs the manifest changes required by the classifier specified by the model.
	 *
	 * This means for now the computation of necessary project dependencies and their addition to the project manifest file.
	 *
	 * <p> IMPORTANT: This method should always be called before invoking {@link #writeToFile(N4JSClassWizardModel)} as
	 * writeToFile may need manifest changes to resolve all imports.</p>
	 */
	override performManifestChanges(M model, IProgressMonitor monitor) {
		monitor.subTask("Performing manifest changes");
		
		val project = n4jsCore.findProject(URI.createPlatformResourceURI(model.computeFileLocation.toString, true));

		if (!project.present) {
			return false;
		}
		
		val manifestLocation = project.get().manifestLocation;
		val manifest = getResource(manifestLocation.get());
			
		// Gather referenced projects
		var referencedProjects = getReferencedProjects(model);
			
		// Create manifest changes
		val moduleURI = URI.createPlatformResourceURI(model.computeFileLocation.toString,true);
		val manifestChanges = manifest.manifestChanges(model, referencedProjects, moduleURI);
		
			
		//Only perform non-empty changes. (To prevent useless history entries)
		if (manifestChanges.length > 0) {
			manifest.applyChanges(manifestChanges);
		}
		return true;
	}

	/**
	 *  Generates the classifier code with regard to the given alias bindings.
	 */
	abstract protected def String generateClassifierCode(M model, Map<URI,String> aliasBindings);

	/**
	 * Returns all projects referenced by the given model.
	 * 
	 * This includes uses of interfaces or super classes from other projects.
	 */
	abstract protected def List<IN4JSProject> getReferencedProjects(M model);

	/**
	 * Returns the import requirements of the model.
	 */
	abstract protected def List<ImportRequirement> getImportRequirements(M model);
	
	private def void insertIntoFile(IFile file, M model) throws CoreException {
		//Retrieve XtextResource
		val moduleURI = URI.createPlatformResourceURI(model.computeFileLocation.toString, true);
		val moduleResource = getResource(moduleURI);

		//Collect the import requirements
		var demandedImports = model.importRequirements

		//Analyze import requirements
		val ImportAnalysis importAnalysis = requirementResolver.analyzeImportRequirements(demandedImports, moduleResource);

		//Generate classifier code
		var classCode = generateClassifierCode(model,importAnalysis.aliasBindings);

		//Add an additional line break for non-line-break terminated files
		if (lastCharacterInFile(file) != "\n") {
			classCode = "\n"+classCode;
		}

		//Get stream for code
		val classCodeStream = new ByteArrayInputStream(classCode.getBytes(StandardCharsets.UTF_8));

		//Insert import statement at the top
		insertImportStatements(moduleResource,importAnalysis.importRequirements);

		//Append classifier code
		file.appendContents(classCodeStream, true, true, null);

		//Finally organize imports
		organizeImports(file, null);
	}
}
