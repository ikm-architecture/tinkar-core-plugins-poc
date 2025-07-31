/*
 * Copyright © 2015 Integrated Knowledge Management (support@ikm.dev)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.ikm.tinkar.integration.coordinate;

import dev.ikm.tinkar.common.id.IntIdList;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.coordinate.Calculators;
import dev.ikm.tinkar.coordinate.Coordinates;
import dev.ikm.tinkar.coordinate.PathService;
import dev.ikm.tinkar.coordinate.language.LanguageCoordinateRecord;
import dev.ikm.tinkar.coordinate.language.calculator.LanguageCalculatorWithCache;
import dev.ikm.tinkar.coordinate.stamp.StampCoordinateRecord;
import dev.ikm.tinkar.coordinate.stamp.StampPositionRecord;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculatorWithCache;
import dev.ikm.tinkar.coordinate.view.ViewCoordinateRecord;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculatorWithCache;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.entity.ConceptEntityVersion;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.StampRecord;
import dev.ikm.tinkar.integration.TestConstants;
import dev.ikm.tinkar.integration.helper.DataStore;
import dev.ikm.tinkar.integration.helper.TestHelper;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.set.ImmutableSet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static dev.ikm.tinkar.terms.TinkarTerm.PATH_ORIGINS_PATTERN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LanguageCalculatorIT {

    private static final Logger LOG = LoggerFactory.getLogger(LanguageCalculatorIT.class);
    private static final File DATASTORE_ROOT = TestConstants.createFilePathInTargetFromClassName.apply(
            LanguageCalculatorIT.class);

    // TODO: Update this reference to use bindings
    private static final String conceptDescription = "Concept for ViewCoordinate tests";
    private static final String conceptDescriptionSpanish = "Concepto para pruebas ViewCoordinate";
    private static final EntityProxy.Concept conceptForViewCoordTests = EntityProxy.Concept.make(conceptDescription, PublicIds.of(UUID.nameUUIDFromBytes(conceptDescription.getBytes())));

    @BeforeAll
    static void beforeAll() {
        TestHelper.startDataBase(DataStore.SPINED_ARRAY_STORE, DATASTORE_ROOT);
        TestHelper.loadDataFile(TestConstants.PB_EXAMPLE_DATA_REASONED);
    }

    @Test
    void compareLangCalcAndDelegate_getFullyQualifiedNameText() {
        // Make View Calculator for Spanish Text
        ViewCoordinateRecord viewCoord = Coordinates.View.DefaultView()
                .withLanguageCoordinateList(Lists.immutable.of(Coordinates.Language.SpanishFullyQualifiedName()));
        ViewCalculatorWithCache viewCalc = new ViewCalculatorWithCache(viewCoord);

        // Get FQN Text
        String fqnTextFromLangCalc =
                viewCalc.languageCalculator().getFullyQualifiedNameText(conceptForViewCoordTests).orElse("");
        String fqnTextFromLangCalcDelegate =
                viewCalc.getFullyQualifiedNameText(conceptForViewCoordTests).orElse("");

        // Validate FQN Texts
        assertEquals(conceptDescriptionSpanish + " - ES FQN", fqnTextFromLangCalc, "LanguageCalculator: FQN text does not match.");
        assertEquals(conceptDescriptionSpanish + " - ES FQN", fqnTextFromLangCalcDelegate, "LanguageCalculatorDelegate: FQN text does not match.");
    }

    @Test
    void USEnglishFqnCalculatorDescriptions() {
        LanguageCalculatorWithCache langCalcUSEnglishFQNLatest = Calculators.Language.UsEnglishFullyQualifiedName(Coordinates.Stamp.DevelopmentLatest());

        // Get FQN Text
        String fqnWithFallbackOrNid = langCalcUSEnglishFQNLatest.getFullyQualifiedDescriptionTextWithFallbackOrNid(conceptForViewCoordTests);
        Optional<String> fqnText = langCalcUSEnglishFQNLatest.getDescriptionText(conceptForViewCoordTests);
        // Validate FQN Text
        assertEquals(conceptDescription + " - US FQN", fqnWithFallbackOrNid);
        assertTrue(fqnText.isPresent());
        assertEquals(conceptDescription + " - US FQN", fqnText.get());

        // Get Synonym Text
        String preferredDescriptionTextWithFallbackOrNid = langCalcUSEnglishFQNLatest.getPreferredDescriptionTextWithFallbackOrNid(conceptForViewCoordTests);
        // Validate Synonym Text
        assertEquals(conceptDescription + " - US Synonym", preferredDescriptionTextWithFallbackOrNid);
    }

    @Test
    void USEnglishSynonymCalculatorDescriptions() {
        LanguageCalculatorWithCache langCalcUSEnglishSynonymLatest = Calculators.Language.UsEnglishRegularName(Coordinates.Stamp.DevelopmentLatest());

        // Get FQN Text
        String fqnWithFallbackOrNid = langCalcUSEnglishSynonymLatest.getFullyQualifiedDescriptionTextWithFallbackOrNid(conceptForViewCoordTests);
        // Validate FQN Text
        assertEquals(conceptDescription + " - US FQN", fqnWithFallbackOrNid);

        // Get Synonym Text
        String preferredDescriptionTextWithFallbackOrNid = langCalcUSEnglishSynonymLatest.getPreferredDescriptionTextWithFallbackOrNid(conceptForViewCoordTests);
        Optional<String> synonymText = langCalcUSEnglishSynonymLatest.getDescriptionText(conceptForViewCoordTests);
        // Validate Synonym Text
        assertEquals(conceptDescription + " - US Synonym", preferredDescriptionTextWithFallbackOrNid);
        assertTrue(synonymText.isPresent());
        assertEquals(conceptDescription + " - US Synonym", synonymText.get());
    }

    @Test
    void GBEnglishFqnCalculatorDescriptions() {
        LanguageCalculatorWithCache langCalcGBEnglishFQNLatest = Calculators.Language.GbEnglishFullyQualifiedName(Coordinates.Stamp.DevelopmentLatest());

        // Get FQN Text
        String fqnWithFallbackOrNid = langCalcGBEnglishFQNLatest.getFullyQualifiedDescriptionTextWithFallbackOrNid(conceptForViewCoordTests);
        Optional<String> fqnText = langCalcGBEnglishFQNLatest.getDescriptionText(conceptForViewCoordTests);
        // Validate FQN Text
        assertEquals(conceptDescription + " - GB FQN", fqnWithFallbackOrNid);
        assertTrue(fqnText.isPresent());
        assertEquals(conceptDescription + " - GB FQN", fqnText.get());

        // Get Synonym Text
        String preferredDescriptionTextWithFallbackOrNid = langCalcGBEnglishFQNLatest.getPreferredDescriptionTextWithFallbackOrNid(conceptForViewCoordTests);
        // Validate Synonym Text
        assertEquals(conceptDescription + " - GB Synonym", preferredDescriptionTextWithFallbackOrNid);
    }

    @Test
    void GBEnglishSynonymCalculatorDescriptions() {
        LanguageCalculatorWithCache langCalcGBEnglishSynonymLatest = Calculators.Language.GbEnglishPreferredName(Coordinates.Stamp.DevelopmentLatest());

        // Get FQN Text
        String fqnWithFallbackOrNid = langCalcGBEnglishSynonymLatest.getFullyQualifiedDescriptionTextWithFallbackOrNid(conceptForViewCoordTests);
        // Validate FQN Text
        assertEquals(conceptDescription + " - GB FQN", fqnWithFallbackOrNid);

        // Get Synonym Text
        String preferredDescriptionTextWithFallbackOrNid = langCalcGBEnglishSynonymLatest.getPreferredDescriptionTextWithFallbackOrNid(conceptForViewCoordTests);
        Optional<String> synonymText = langCalcGBEnglishSynonymLatest.getDescriptionText(conceptForViewCoordTests);
        // Validate Synonym Text
        assertEquals(conceptDescription + " - GB Synonym", preferredDescriptionTextWithFallbackOrNid);
        assertTrue(synonymText.isPresent());
        assertEquals(conceptDescription + " - GB Synonym", synonymText.get());
    }

    @Test
    void SpanishFqnCalculatorDescriptions() {
        LanguageCalculatorWithCache langCalcSpanishFQNLatest = Calculators.Language.SpanishFullyQualifiedName(Coordinates.Stamp.DevelopmentLatest());

        // Get FQN Text
        String fqnWithFallbackOrNid = langCalcSpanishFQNLatest.getFullyQualifiedDescriptionTextWithFallbackOrNid(conceptForViewCoordTests);
        Optional<String> fqnText = langCalcSpanishFQNLatest.getDescriptionText(conceptForViewCoordTests);
        // Validate FQN Text
        assertEquals(conceptDescriptionSpanish + " - ES FQN", fqnWithFallbackOrNid);
        assertTrue(fqnText.isPresent());
        assertEquals(conceptDescriptionSpanish + " - ES FQN", fqnText.get());

        // Get Synonym Text
        String preferredDescriptionTextWithFallbackOrNid = langCalcSpanishFQNLatest.getPreferredDescriptionTextWithFallbackOrNid(conceptForViewCoordTests);
        // Validate Synonym Text
        assertEquals(conceptDescriptionSpanish + " - ES Synonym", preferredDescriptionTextWithFallbackOrNid);
    }

    @Test
    void SpanishSynonymCalculatorDescriptions() {
        LanguageCalculatorWithCache langCalcSpanishSynonymLatest = Calculators.Language.SpanishPreferredName(Coordinates.Stamp.DevelopmentLatest());

        // Get FQN Text
        String fqnWithFallbackOrNid = langCalcSpanishSynonymLatest.getFullyQualifiedDescriptionTextWithFallbackOrNid(conceptForViewCoordTests);
        // Validate FQN Text
        assertEquals(conceptDescriptionSpanish + " - ES FQN", fqnWithFallbackOrNid);

        // Get Synonym Text
        String preferredDescriptionTextWithFallbackOrNid = langCalcSpanishSynonymLatest.getPreferredDescriptionTextWithFallbackOrNid(conceptForViewCoordTests);
        Optional<String> synonymText = langCalcSpanishSynonymLatest.getDescriptionText(conceptForViewCoordTests);
        // Validate Synonym Text
        assertEquals(conceptDescriptionSpanish + " - ES Synonym", preferredDescriptionTextWithFallbackOrNid);
        assertTrue(synonymText.isPresent());
        assertEquals(conceptDescriptionSpanish + " - ES Synonym", synonymText.get());
    }

}
