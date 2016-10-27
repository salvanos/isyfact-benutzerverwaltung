package de.bund.bva.isyfact.benutzerverwaltung.gui.rollenverwaltung.awkwrapper;

import de.bund.bva.isyfact.benutzerverwaltung.common.datentyp.Paginierung;
import de.bund.bva.isyfact.benutzerverwaltung.common.datentyp.Sortierung;
import de.bund.bva.isyfact.benutzerverwaltung.common.exception.BenutzerverwaltungBusinessException;
import de.bund.bva.isyfact.benutzerverwaltung.common.exception.BenutzerverwaltungValidationException;
import de.bund.bva.isyfact.benutzerverwaltung.gui.common.model.RolleModel;
import de.bund.bva.isyfact.benutzerverwaltung.gui.common.model.SuchergebnisModel;
import de.bund.bva.isyfact.benutzerverwaltung.gui.rollenverwaltung.rollesuchen.RolleSuchkriterienModel;

/**
 * Wrapper für die Komponente Rollenverwaltung des Anwendungskerns. Bietet die Möglichkeit zur Verwaltung
 * (Lesen, Suchen, Anlegen, Ändern, Löschen) von Rollen.
 *
 * @author msg systems ag, Stefan Dellmuth
 */
public interface RollenverwaltungAwkWrapper {

    /**
     * Liest eine Rolle anhand ihrer Rollen-ID aus.
     *
     * @param rolleId ID der Rolle
     * @return die Daten der Rolle, falls vorhanden, oder {@code null}.
     * @throws BenutzerverwaltungBusinessException falls die Rolle nicht existiert.
     */
    RolleModel leseRolle(String rolleId) throws BenutzerverwaltungBusinessException;

    /**
     * Sucht Rollen anhand von Suchkriterien und erzeugt eine Trefferliste anhand der übergebenen Sortierung
     * und Paginierung.
     *
     * @param filter      enthält die Suchkriterien
     * @param sortierung  enthält das Sortierattribut und die Reihenfolge der Sortierung (aufsteigend,
     *                    absteigend)
     * @param paginierung enthält den ersten Treffer und die maximale Größe der Trefferliste
     * @return eine sortierte Liste von Rollen.
     * @throws BenutzerverwaltungValidationException falls die Eingabedaten ungültig sind.
     */
    SuchergebnisModel<RolleModel> sucheRollen(RolleSuchkriterienModel filter, Sortierung sortierung,
        Paginierung paginierung) throws BenutzerverwaltungValidationException;

    /**
     * Legt eine neue Rolle an.
     *
     * @param rolleModel Daten der neuen Rolle
     * @return die neue Rolle.
     * @throws BenutzerverwaltungValidationException falls die Eingabedaten ungültig sind.
     */
    RolleModel legeRolleAn(RolleModel rolleModel) throws BenutzerverwaltungValidationException;

    /**
     * Ändert die Daten einer Rolle.
     *
     * @param rolleId    ID der Rolle
     * @param rolleModel neue Daten der Rolle
     * @return die aktualisierte Rolle.
     * @throws BenutzerverwaltungValidationException falls die Eingabedaten ungültig sind.
     */
    RolleModel aendereRolle(String rolleId, RolleModel rolleModel)
        throws BenutzerverwaltungValidationException;

    /**
     * Löscht eine Rolle anhand ihrer ID.
     *
     * @param rolleId ID der Rolle
     * @throws BenutzerverwaltungBusinessException falls die Eingabedaten ungültig sind.
     */
    void loescheRolle(String rolleId) throws BenutzerverwaltungBusinessException;

}