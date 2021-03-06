package de.bund.bva.isyfact.benutzerverwaltung.core.benutzerverwaltung.impl;

/*-
 * #%L
 * IsyFact Benutzerverwaltung Core
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
import de.bund.bva.isyfact.benutzerverwaltung.common.exception.BenutzerverwaltungTechnicalRuntimeException;
import de.bund.bva.isyfact.benutzerverwaltung.common.exception.BenutzerverwaltungValidationException;
import de.bund.bva.isyfact.benutzerverwaltung.common.konstanten.FehlerSchluessel;
import de.bund.bva.isyfact.benutzerverwaltung.common.konstanten.ValidierungSchluessel;
import de.bund.bva.isyfact.benutzerverwaltung.core.basisdaten.daten.RolleDaten;
import de.bund.bva.isyfact.benutzerverwaltung.core.benutzerverwaltung.BenutzerStatus;
import de.bund.bva.isyfact.benutzerverwaltung.core.benutzerverwaltung.daten.*;
import de.bund.bva.isyfact.benutzerverwaltung.persistence.basisdaten.dao.BenutzerDao;
import de.bund.bva.isyfact.benutzerverwaltung.persistence.basisdaten.dao.RollenDao;
import de.bund.bva.isyfact.benutzerverwaltung.persistence.basisdaten.entity.Benutzer;
import de.bund.bva.isyfact.benutzerverwaltung.persistence.basisdaten.entity.Rolle;
import de.bund.bva.isyfact.logging.IsyLogger;
import de.bund.bva.isyfact.logging.IsyLoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.validation.Validator;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Diese Klasse bietet Funktionalität zum Erstellen, Bearbeiten und Loeschen
 * einer {@link Benutzer} Entität.
 *
 * @author Capgemini, Jonas Zitz
 * @version $Id: AwfBenutzerVerwalten.java 41877 2013-07-26 09:08:49Z jozitz $
 */
class AwfBenutzerVerwalten extends AbstractAnwendungsfall {
    private final static IsyLogger LOG = IsyLoggerFactory.getLogger(AwfBenutzerVerwalten.class);

    private final RollenDao rollenDao;

    private final PasswordEncoder passwordEncoder;

    AwfBenutzerVerwalten(BenutzerDao benutzerDao, RollenDao rollenDao, PasswordEncoder passwordEncoder,
        Validator validator) {
        super(benutzerDao, validator);
        this.rollenDao = rollenDao;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Legt einen neuen Benutzer an.
     *
     * @param benutzerAnlegen Daten des neuen Benutzers
     * @return den neuen Benutzer.
     * @throws BenutzerverwaltungValidationException falls die Eingabedaten ungültig sind.
     */
    Benutzer legeBenutzerAn(BenutzerAnlegen benutzerAnlegen) throws BenutzerverwaltungValidationException {
        if (benutzerAnlegen == null) {
            throw new BenutzerverwaltungTechnicalRuntimeException(FehlerSchluessel.MSG_EINGABEDATEN_FEHLEN);
        }

        validiere(benutzerAnlegen);

        Benutzer benutzer = new Benutzer();
        benutzer.setBenutzername(benutzerAnlegen.getBenutzername());
        benutzer.setStatus(benutzerAnlegen.getStatus());
        benutzer.setRollen(konvertiereRollen(benutzerAnlegen.getRollen()));
        benutzer.setPasswort(passwordEncoder.encode(benutzerAnlegen.getPasswort()));
        benutzer.setBemerkung(benutzerAnlegen.getBemerkung());

        benutzer.setNachname(benutzerAnlegen.getNachname());
        benutzer.setVorname(benutzerAnlegen.getVorname());
        benutzer.setBehoerde(benutzerAnlegen.getBehoerde());
        benutzer.setEmailAdresse(benutzerAnlegen.getEmailAdresse());
        benutzer.setTelefonnummer(benutzerAnlegen.getTelefonnummer());

        LOG.debug("Speichere neuen Benutzer.");
        benutzerDao.speichere(benutzer);

        return benutzer;
    }

    /**
     * Ändert das Passwort des Benutzers. Diese Aktion kann von Benutzern selbst durchgeführt werden.
     *
     * @param passwortAendern Eingabedaten zur Änderung des Passworts
     * @return den aktualisierten Benutzer.
     * @throws BenutzerverwaltungBusinessException falls die Eingabedaten nicht valide sind.
     */
    Benutzer setzePasswort(PasswortAendern passwortAendern) throws BenutzerverwaltungBusinessException {
        if (passwortAendern == null) {
            throw new BenutzerverwaltungTechnicalRuntimeException(FehlerSchluessel.MSG_EINGABEDATEN_FEHLEN);
        }

        // Gleichheit des alten Passworts über Bean Validation geprüft
        validiere(passwortAendern);
        return setzePasswort(passwortAendern.getBenutzername(), passwortAendern.getNeuesPasswort());
    }

    /**
     * Setzt das Passwort eines Benutzers zurück. Diese Aktion kann nur von einem Administrator durchgeführt
     * werden.
     *
     * @param passwortZuruecksetzen Eingabedaten zum Zurücksetzen eines Passworts
     * @return den aktualisierten Benutzer.
     * @throws BenutzerverwaltungValidationException falls die Eingabedaten nicht valide sind.
     */
    Benutzer setzePasswortZurueck(PasswortZuruecksetzen passwortZuruecksetzen)
        throws BenutzerverwaltungBusinessException {
        if (passwortZuruecksetzen == null) {
            throw new BenutzerverwaltungTechnicalRuntimeException(FehlerSchluessel.MSG_EINGABEDATEN_FEHLEN);
        }

        validiere(passwortZuruecksetzen);
        return setzePasswort(passwortZuruecksetzen.getBenutzername(),
            passwortZuruecksetzen.getNeuesPasswort());
    }

    private Benutzer setzePasswort(String benutzername, String neuesPasswort)
        throws BenutzerverwaltungBusinessException {
        Benutzer benutzer = leseBenutzer(benutzername);
        benutzer.setPasswort(passwordEncoder.encode(neuesPasswort));
        benutzerDao.speichere(benutzer);
        return benutzer;
    }

    /**
     * Setzt den Status einen Benutzers.
     *
     * @param benutzername Benutzername
     * @param neuerStatus  neuer Status
     * @return den aktualisierten Benutzer.
     * @throws BenutzerverwaltungValidationException falls der Benutzer nicht existiert.
     */
    Benutzer setzeStatus(String benutzername, BenutzerStatus neuerStatus)
        throws BenutzerverwaltungBusinessException {
        Benutzer benutzer = leseBenutzer(benutzername);
        BenutzerStatus alterStatus = benutzer.getStatus();

        if (alterStatus == neuerStatus) {
            return benutzer;
        }

        // Fehlerhafte Anmeldungen beim Entsperren zurücksetzen.
        if (alterStatus == BenutzerStatus.GESPERRT) {
            benutzer.setFehlanmeldeVersuche(0);
        }
        benutzer.setStatus(neuerStatus);
        benutzerDao.speichere(benutzer);
        return benutzer;
    }

    /**
     * Ändert die Daten eines Benutzers durch einen Administrator.
     *
     * @param benutzerAendern neue Benutzerdaten
     * @return den aktualisierten Benutzer.
     * @throws BenutzerverwaltungBusinessException falls die Eingabedaten ungültig sind.
     */
    Benutzer aendereBenutzer(BenutzerAendern benutzerAendern) throws BenutzerverwaltungBusinessException {

        validiere(benutzerAendern);

        // 1. Neuen Status setzen
        Benutzer benutzer = setzeStatus(benutzerAendern.getAlterBenutzername(), benutzerAendern.getStatus());

        // 2. Restliche Werte setzen
        if (benutzerAendern.getNeuerBenutzername() != null) {
            benutzer.setBenutzername(benutzerAendern.getNeuerBenutzername());
        }
        benutzer.setRollen(konvertiereRollen(benutzerAendern.getRollen()));
        benutzer.setNachname(benutzerAendern.getNachname());
        benutzer.setVorname(benutzerAendern.getVorname());
        benutzer.setBehoerde(benutzerAendern.getBehoerde());
        benutzer.setEmailAdresse(benutzerAendern.getEmailAdresse());
        benutzer.setTelefonnummer(benutzerAendern.getTelefonnummer());
        benutzer.setBemerkung(benutzerAendern.getBemerkung());

        benutzerDao.speichere(benutzer);

        return benutzer;
    }

    /**
     * Ändert die Daten eines Benutzers durch den Benutzer selbst.
     *
     * @param benutzerSelbstAendern neue Benutzerdaten
     * @return den aktualisierten Benutzer.
     * @throws BenutzerverwaltungBusinessException falls die Eingabedaten ungültig sind.
     */
    Benutzer aendereBenutzerSelbst(BenutzerSelbstAendern benutzerSelbstAendern)
        throws BenutzerverwaltungBusinessException {

        validiere(benutzerSelbstAendern);

        Benutzer benutzer = leseBenutzer(benutzerSelbstAendern.getBenutzername());

        benutzer.setNachname(benutzerSelbstAendern.getNachname());
        benutzer.setVorname(benutzerSelbstAendern.getVorname());
        benutzer.setBehoerde(benutzerSelbstAendern.getBehoerde());
        benutzer.setEmailAdresse(benutzerSelbstAendern.getEmailAdresse());
        benutzer.setTelefonnummer(benutzerSelbstAendern.getTelefonnummer());

        benutzerDao.speichere(benutzer);

        return benutzer;
    }

    /**
     * Weist einer Menge von Benutzern eine Rolle zu.
     *
     * @param rolleDaten    Daten der   Rolle
     * @param benutzernamen Benutzer, welche die Rolle erhalten sollen
     * @throws BenutzerverwaltungBusinessException falls die Rolle oder einer der Benutzer nicht existiert.
     */
    void weiseRolleZu(RolleDaten rolleDaten, List<String> benutzernamen)
        throws BenutzerverwaltungBusinessException {
        Rolle rolle = rollenDao.sucheMitRollenId(rolleDaten.getRollenId());

        if (rolle == null) {
            throw new BenutzerverwaltungBusinessException(ValidierungSchluessel.MSG_ROLLE_NICHT_VORHANDEN,
                rolleDaten.getRollenId());
        }

        for (String benutzername : benutzernamen) {
            Benutzer benutzer = benutzerDao.sucheMitBenutzername(benutzername);
            if (benutzer == null) {
                throw new BenutzerverwaltungBusinessException(
                    ValidierungSchluessel.MSG_BENUTZERNAME_NICHT_VORHANDEN, benutzername);
            } else {
                benutzer.getRollen().add(rolle);
                benutzerDao.speichere(benutzer);
            }
        }
    }

    /**
     * Entzieht einer Menge von Benutzern eine Rolle.
     *
     * @param rolleDaten    Daten der Rolle
     * @param benutzernamen Benutzer, welchen die Rolle entzogen werden soll.
     * @throws BenutzerverwaltungValidationException falls die Rolle oder einer der Benutzer nicht existiert.
     */
    void entzieheRolle(RolleDaten rolleDaten, List<String> benutzernamen)
        throws BenutzerverwaltungBusinessException {
        Rolle rolle = rollenDao.sucheMitRollenId(rolleDaten.getRollenId());

        if (rolle == null) {
            throw new BenutzerverwaltungBusinessException(ValidierungSchluessel.MSG_ROLLE_NICHT_VORHANDEN,
                rolleDaten.getRollenId());
        }

        for (String benutzername : benutzernamen) {
            Benutzer benutzer = benutzerDao.sucheMitBenutzername(benutzername);
            if (benutzer == null) {
                throw new BenutzerverwaltungBusinessException(
                    ValidierungSchluessel.MSG_BENUTZERNAME_NICHT_VORHANDEN, benutzername);
            } else {
                benutzer.getRollen().remove(rolle);
                benutzerDao.speichere(benutzer);
            }
        }
    }

    private List<Rolle> konvertiereRollen(List<RolleDaten> rolleDaten) {
        if (rolleDaten == null) {
            return Collections.emptyList();
        }

        List<String> rollenIds =
            rolleDaten.stream().map(RolleDaten::getRollenId).collect(Collectors.toList());
        return rollenDao.sucheMitRollenIds(rollenIds);
    }

    /**
     * Löscht einen Benutzer anhand des Benutzernamens.
     *
     * @param benutzername Benutzername
     * @throws BenutzerverwaltungValidationException wenn die Eingabedaten ungültig sind.
     */
    void loescheBenutzer(String benutzername) throws BenutzerverwaltungBusinessException {
        Benutzer benutzer = leseBenutzer(benutzername);
        benutzerDao.loesche(benutzer);
        LOG.debugFachdaten("Der Benutzer '{}' wurde gelöscht.", benutzer.getBenutzername());
    }

}
