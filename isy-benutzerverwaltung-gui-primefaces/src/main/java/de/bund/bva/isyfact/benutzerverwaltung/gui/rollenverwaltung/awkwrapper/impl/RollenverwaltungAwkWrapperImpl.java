package de.bund.bva.isyfact.benutzerverwaltung.gui.rollenverwaltung.awkwrapper.impl;

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


import de.bund.bva.isyfact.benutzerverwaltung.common.datentyp.Paginierung;
import de.bund.bva.isyfact.benutzerverwaltung.common.datentyp.Sortierrichtung;
import de.bund.bva.isyfact.benutzerverwaltung.common.datentyp.Sortierung;
import de.bund.bva.isyfact.benutzerverwaltung.common.datentyp.Suchergebnis;
import de.bund.bva.isyfact.benutzerverwaltung.common.exception.BenutzerverwaltungBusinessException;
import de.bund.bva.isyfact.benutzerverwaltung.common.exception.BenutzerverwaltungValidationException;
import de.bund.bva.isyfact.benutzerverwaltung.core.basisdaten.daten.RolleDaten;
import de.bund.bva.isyfact.benutzerverwaltung.core.rollenverwaltung.RolleSortierattribut;
import de.bund.bva.isyfact.benutzerverwaltung.core.rollenverwaltung.RolleSuchkriterien;
import de.bund.bva.isyfact.benutzerverwaltung.core.rollenverwaltung.Rollenverwaltung;
import de.bund.bva.isyfact.benutzerverwaltung.core.rollenverwaltung.daten.RolleAendern;
import de.bund.bva.isyfact.benutzerverwaltung.core.rollenverwaltung.daten.RolleAnlegen;
import de.bund.bva.isyfact.benutzerverwaltung.gui.common.model.RolleModel;
import de.bund.bva.isyfact.benutzerverwaltung.gui.common.model.SuchergebnisModel;
import de.bund.bva.isyfact.benutzerverwaltung.gui.rollenverwaltung.awkwrapper.RollenverwaltungAwkWrapper;
import de.bund.bva.isyfact.benutzerverwaltung.gui.rollenverwaltung.rollesuchen.RolleSuchkriterienModel;
import org.dozer.Mapper;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Standard-Implementierung des AWK-Wrappers für die Komponente Rollenverwaltung.
 *
 * @author msg systems ag, Stefan Dellmuth
 */
@Transactional(rollbackFor = Throwable.class, propagation = Propagation.REQUIRED)
public class RollenverwaltungAwkWrapperImpl implements RollenverwaltungAwkWrapper {

    private final Rollenverwaltung rollenverwaltung;

    private final Mapper mapper;

    public RollenverwaltungAwkWrapperImpl(Rollenverwaltung rollenverwaltung, Mapper mapper) {
        this.rollenverwaltung = rollenverwaltung;
        this.mapper = mapper;
    }

    @Override
    public RolleModel leseRolle(String rolleId) throws BenutzerverwaltungBusinessException {
        return mapper.map(rollenverwaltung.leseRolle(rolleId), RolleModel.class);
    }

    @Override
    public SuchergebnisModel<RolleModel> sucheRollen(RolleSuchkriterienModel filter, Sortierung sortierung,
        Paginierung paginierung) throws BenutzerverwaltungValidationException {
        RolleSuchkriterien coreSuchkriterien = mapper.map(filter, RolleSuchkriterien.class);

        // Sortierung mappen
        Sortierung coreSortierung =
            new Sortierung(RolleSortierattribut.getStandard(), Sortierrichtung.getStandard());
        if (sortierung != null) {
            coreSortierung.setAttribut(sortierung.getAttribut());
            coreSortierung.setRichtung(sortierung.getRichtung());
        }

        Suchergebnis<RolleDaten> suchergebnis =
            rollenverwaltung.sucheRollen(coreSuchkriterien, coreSortierung, paginierung);

        SuchergebnisModel<RolleModel> suchergebnisModel = new SuchergebnisModel<>();
        for (RolleDaten treffer : suchergebnis.getTrefferliste()) {
            suchergebnisModel.getTrefferliste().add(mapper.map(treffer, RolleModel.class));
        }
        suchergebnisModel.setAnzahlTreffer(suchergebnis.getAnzahlTreffer());

        return suchergebnisModel;
    }

    @Override
    public RolleModel legeRolleAn(RolleModel rolleModel) throws BenutzerverwaltungValidationException {
        RolleAnlegen rolleAnlegen = new RolleAnlegen(rolleModel.getRollenId(), rolleModel.getRollenName());
        return mapper.map(rollenverwaltung.legeRolleAn(rolleAnlegen), RolleModel.class);
    }

    @Override
    public RolleModel aendereRolle(String rolleId, RolleModel rolleModel)
        throws BenutzerverwaltungValidationException {
        // Neue ID nur mitliefern, wenn sie sich wirklich geändert hat.
        String neueRollenId = rolleId.equals(rolleModel.getRollenId()) ? null : rolleModel.getRollenId();
        RolleAendern rolleAendern = new RolleAendern(rolleId, neueRollenId, rolleModel.getRollenName());
        return mapper.map(rollenverwaltung.aendereRolle(rolleAendern), RolleModel.class);
    }

    @Override
    public void loescheRolle(String rolleId) throws BenutzerverwaltungBusinessException {
        rollenverwaltung.loescheRolle(rolleId);
    }

}
