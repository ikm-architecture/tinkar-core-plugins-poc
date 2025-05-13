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
package dev.ikm.tinkar.integration.provider.spinedarray;

import dev.ikm.tinkar.common.util.io.FileUtil;
import dev.ikm.tinkar.coordinate.Coordinates;
import dev.ikm.tinkar.coordinate.view.ViewCoordinateRecord;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculatorWithCache;
import dev.ikm.tinkar.entity.EntityCountSummary;
import dev.ikm.tinkar.entity.load.LoadEntitiesFromProtobufFile;
import dev.ikm.tinkar.integration.TestConstants;
import dev.ikm.tinkar.integration.helper.DataStore;
import dev.ikm.tinkar.integration.helper.TestHelper;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SpinedArrayImportIT {
    private static final Logger LOG = LoggerFactory.getLogger(SpinedArrayImportIT.class);
    private static final File DATASTORE_ROOT = TestConstants.createFilePathInTargetFromClassName.apply(
            SpinedArrayImportIT.class);

    @BeforeAll
    static void beforeAll() {
        FileUtil.recursiveDelete(DATASTORE_ROOT);
        TestHelper.startDataBase(DataStore.SPINED_ARRAY_STORE, DATASTORE_ROOT);
        File file = TestConstants.PB_EXAMPLE_DATA_REASONED;
        LoadEntitiesFromProtobufFile loadProto = new LoadEntitiesFromProtobufFile(file);
        EntityCountSummary count = loadProto.compute();
        LOG.info(count + " entitles loaded from file: " + loadProto.summarize() + "\n\n");
    }

    @AfterAll
    static void afterAll() {
        TestHelper.stopDatabase();
    }

    @Test
    public void stampCalcRefreshAfterImport() {
        // Set up ViewCalculatorWithCache to replicate calculator for Komet window
        ViewCoordinateRecord viewCoord = Coordinates.View.DefaultView();
        ViewCalculatorWithCache viewCalc = ViewCalculatorWithCache.getCalculator(viewCoord);

        // Query concept using Calculator.latest() (use both methods: latest(entity) AND latest(nid))
        String fqnBeforeFromEntityFacade = viewCalc.getFullyQualifiedDescriptionTextWithFallbackOrNid(TinkarTerm.ACTIVE_STATE);
        String fqnBeforeFromNid = viewCalc.getFullyQualifiedDescriptionTextWithFallbackOrNid(TinkarTerm.ACTIVE_STATE.nid());

        // Import pb file
        URL pbResourceUrl = getClass().getClassLoader().getResource("active-state-fqn-change-ike-cs.zip");
        assert pbResourceUrl != null;
        File pbFile = new File(pbResourceUrl.getFile());

        LoadEntitiesFromProtobufFile loadProto = new LoadEntitiesFromProtobufFile(pbFile);
        EntityCountSummary count = loadProto.compute();
        LOG.info(count + " entitles loaded from file: " + loadProto.summarize() + "\n\n");

        // Query again and compare results
        String fqnAfterFromEntityFacade = viewCalc.getFullyQualifiedDescriptionTextWithFallbackOrNid(TinkarTerm.ACTIVE_STATE);
        String fqnAfterFromNid = viewCalc.getFullyQualifiedDescriptionTextWithFallbackOrNid(TinkarTerm.ACTIVE_STATE.nid());

        assertNotEquals(fqnBeforeFromEntityFacade, fqnAfterFromEntityFacade);
        assertNotEquals(fqnBeforeFromNid, fqnAfterFromNid);
    }

}
