package org.jabref.gui.maintable;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import javafx.beans.binding.BooleanBinding;

import org.jabref.gui.DialogService;
import org.jabref.gui.StateManager;
import org.jabref.gui.actions.ActionHelper;
import org.jabref.gui.actions.SimpleCommand;
import org.jabref.gui.desktop.JabRefDesktop;
import org.jabref.logic.l10n.Localization;
import org.jabref.model.entry.BibEntry;
import org.jabref.model.entry.field.Field;
import org.jabref.model.entry.field.StandardField;

public class OpenUrlAction extends SimpleCommand {

    private final DialogService dialogService;
    private final StateManager stateManager;

    public OpenUrlAction(DialogService dialogService, StateManager stateManager) {
        this.dialogService = dialogService;
        this.stateManager = stateManager;

        BooleanBinding fieldIsSet = ActionHelper.isFieldSetForSelectedEntry(StandardField.URL, stateManager)
                                                .or(ActionHelper.isFieldSetForSelectedEntry(StandardField.DOI, stateManager)
                                                                .or(ActionHelper.isFieldSetForSelectedEntry(StandardField.URI, stateManager)
                                                                                .or(ActionHelper.isFieldSetForSelectedEntry(StandardField.EPRINT, stateManager))));
        this.executable.bind(ActionHelper.needsEntriesSelected(1, stateManager).and(fieldIsSet));
    }

    @Override
    public void execute() {
        stateManager.getActiveDatabase().ifPresent(databaseContext -> {
            final List<BibEntry> entries = stateManager.getSelectedEntries();

            if (entries.size() != 1) {
                dialogService.notify(Localization.lang("This operation requires exactly one item to be selected."));
                return;
            }

            BibEntry entry = entries.get(0);

            // ToDo: Create dialog or menu to chose which one to open
            // URL - DOI - DOI - EPRINT
            Optional<String> link = entry.getField(StandardField.EPRINT);
            Field field = StandardField.EPRINT;
            if (entry.hasField(StandardField.URI)) {
                link = entry.getField(StandardField.URI);
                field = StandardField.URI;
            }
            if (entry.hasField(StandardField.DOI)) {
                link = entry.getField(StandardField.DOI);
                field = StandardField.DOI;
            }
            if (entry.hasField(StandardField.URL)) {
                link = entry.getField(StandardField.URL);
                field = StandardField.URL;
            }

            if (link.isPresent()) {
                try {
                    JabRefDesktop.openExternalViewer(databaseContext, link.get(), field);
                } catch (IOException e) {
                    dialogService.showErrorDialogAndWait(Localization.lang("Unable to open link."), e);
                }
            }
        });
    }
}
