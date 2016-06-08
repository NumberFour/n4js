package org.eclipse.platform.doc.isv.activeHelp;

import org.eclipse.help.ILiveHelpAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
/**
 * Sample Active Help action.
 */
public class ActiveHelpOpenDialogAction implements ILiveHelpAction {

     public void setInitializationString(String data) {
          // ignore the data.  We do not use any javascript parameters.
     }

     public void run() {
          // Active help does not run on the UI thread, so we must use syncExec
          Display.getDefault().syncExec(new Runnable() {
               public void run() {
                    IWorkbenchWindow window =
                         PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                    if (window != null) {
                         // Bring the Workbench window to the top of other windows;
                         // On some Windows systems, it will only flash the Workbench
                         // icon on the task bar
                         Shell shell = window.getShell();
                         shell.setMinimized(false);
                         shell.forceActive();
                         // Open a message dialog
                         MessageDialog.openInformation(
                              window.getShell(),
                              "Hello World.",
                              "Hello World.");
                    }
               }
          });
     }
}