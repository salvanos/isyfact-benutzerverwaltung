package de.bund.bva.isyfact.benutzerverwaltung.gui.benutzerverwaltung.benutzerbearbeiten;

/*-
 * #%L
 * IsyFact Benutzerverwaltung GUI mit Primefaces
 * %%
 * Copyright (C) 2016 - 2017 Bundesverwaltungsamt (BVA)
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import de.bund.bva.isyfact.benutzerverwaltung.common.exception.BenutzerverwaltungBusinessException;
import de.bund.bva.isyfact.benutzerverwaltung.common.exception.BenutzerverwaltungValidationException;
import de.bund.bva.isyfact.benutzerverwaltung.gui.benutzerverwaltung.awkwrapper.BenutzerverwaltungAwkWrapper;
import de.bund.bva.isyfact.benutzerverwaltung.gui.benutzerverwaltung.common.controller.AbstractBenutzerverwaltungController;
import de.bund.bva.isyfact.benutzerverwaltung.gui.common.konstanten.HinweisSchluessel;
import de.bund.bva.isyfact.benutzerverwaltung.gui.common.model.BenutzerModel;
import de.bund.bva.isyfact.common.web.global.MessageController;
import de.bund.bva.pliscommon.aufrufkontext.AufrufKontext;
import de.bund.bva.pliscommon.aufrufkontext.AufrufKontextVerwalter;
import de.bund.bva.pliscommon.sicherheit.Sicherheit;
import de.bund.bva.pliscommon.util.spring.MessageSourceHolder;
import org.springframework.context.MessageSource;

/**
 * Controller zum Bearbeiten von Benutzern.
 *
 * @author msg systems ag, Stefan Dellmuth
 * @version $Id: RolleBearbeitenController.java 41870 2013-07-25 13:54:34Z jozitz $
 */
public class BenutzerBearbeitenController
    extends AbstractBenutzerverwaltungController<BenutzerBearbeitenModel> {

    private final AufrufKontextVerwalter<?> aufrufKontextVerwalter;

    private final Sicherheit<?> sicherheit;

    public BenutzerBearbeitenController(MessageController messageController, MessageSource messageSource,
        BenutzerverwaltungAwkWrapper awkWrapper, AufrufKontextVerwalter<?> aufrufKontextVerwalter,
        Sicherheit<?> sicherheit) {
        super(messageController, messageSource, awkWrapper);
        this.aufrufKontextVerwalter = aufrufKontextVerwalter;
        this.sicherheit = sicherheit;
    }

    @Override
    public void initialisiereModel(BenutzerBearbeitenModel model) {
        try {
            model.setAlleRollen(getAwkWrapper().leseAlleRollen());
        } catch (BenutzerverwaltungValidationException e) {
            erzeugeNachrichten(e);
        }
    }

    /**
     * Diese Methode setzt den uebergebenen {@link BenutzerModel} in das Model und initialisiert es.
     *
     * @param model    das Model
     * @param benutzer der uebergebene {@link BenutzerModel}
     */
    public void setzeBenutzer(BenutzerBearbeitenModel model, BenutzerModel benutzer) {
        model.setBenutzer(benutzer);
    }

    /**
     * Funktion zum Aktualisieren / Bearbeiten eines Benutzers
     *
     * @param model das {@link BenutzerBearbeitenModel}
     */
    public void benutzerSpeichern(BenutzerBearbeitenModel model) {
        try {
            BenutzerModel ergebnis = getAwkWrapper().aendereBenutzer(model);
            // Das Leeren des Caches zwingt alle Benutzer intern zur erneuten Anmeldung.
            // Der gelöschte Benutzer stößt auf einen Fehler und ist so nicht mehr angemeldet.
            sicherheit.leereCache();

            // Wenn der Benutzer seinen eigenen Namen geändert hat, muss der Aufrufkontext geändert werden.
            // Außerdem muss sich der Benutzer neu autorisieren, falls sich auch die Rollen geändert haben.
            AufrufKontext angemeldeterBenutzer = aufrufKontextVerwalter.getAufrufKontext();
            if (angemeldeterBenutzer.getDurchfuehrenderBenutzerKennung()
                .equals(model.getAlterBenutzername())) {
                angemeldeterBenutzer.setDurchfuehrenderBenutzerKennung(model.getNeuerBenutzername());
                angemeldeterBenutzer.setRollenErmittelt(false);
            }
            getMessageController().writeSuccessMessage(MessageSourceHolder
                .getMessage(HinweisSchluessel.BENUTZER_AKTUALISIERT, ergebnis.getBenutzername()));
        } catch (BenutzerverwaltungBusinessException validationException) {
            erzeugeNachrichten(validationException);
        }
    }

    @Override
    protected Class<BenutzerBearbeitenModel> getMaskenModelKlasseZuController() {
        return BenutzerBearbeitenModel.class;
    }

}
