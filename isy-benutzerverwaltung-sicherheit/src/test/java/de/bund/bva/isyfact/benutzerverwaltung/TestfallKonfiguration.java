package de.bund.bva.isyfact.benutzerverwaltung;

/*-
 * #%L
 * IsyFact Benutzerverwaltung Sicherheit
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

import de.bund.bva.pliscommon.konfiguration.common.Konfiguration;
import de.bund.bva.pliscommon.konfiguration.common.impl.ReloadablePropertyKonfiguration;
import de.bund.bva.pliscommon.util.spring.MessageSourceHolder;
import org.springframework.beans.factory.config.CustomScopeConfigurer;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.context.support.SimpleThreadScope;

/**
 * Enthält die Konfiguration für einen Testfall.
 *
 * @author Stefan Dellmuth, msg systems ag
 */
@Configuration
@Import(TestPersistenceConfiguration.class)
@ImportResource(
    { "classpath:resources/isy-benutzerverwaltung/spring/isy-benutzerverwaltung-sicherheit-modul.xml",
        "classpath:resources/isy-benutzerverwaltung/spring/isy-benutzerverwaltung-core-modul.xml" })
public class TestfallKonfiguration {

    private static final String[] BASENAMES = { "resources/isy-benutzerverwaltung/nachrichten/fehler",
        "resources/isy-benutzerverwaltung/nachrichten/hinweise",
        "resources/isy-benutzerverwaltung/nachrichten/validation" };

    @Bean
    public static CustomScopeConfigurer customScopeConfigurer() {
        CustomScopeConfigurer customScopeConfigurer = new CustomScopeConfigurer();
        customScopeConfigurer.addScope("session", new SimpleThreadScope());
        customScopeConfigurer.addScope("request", new SimpleThreadScope());
        return customScopeConfigurer;
    }

    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasenames(BASENAMES);
        return messageSource;
    }

    @Bean
    public MessageSourceHolder messageSourceHolder() {
        return new MessageSourceHolder();
    }

    @Bean
    public Konfiguration konfiguration() {
        String[] konfigurationsDateien = { "/config/isy-benutzerverwaltung.properties" };
        return new ReloadablePropertyKonfiguration(konfigurationsDateien);
    }

}
