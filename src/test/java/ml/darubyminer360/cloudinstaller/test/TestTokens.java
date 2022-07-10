/*
 * Copyright 2022 DaRubyMiner360 & Cloud Loader
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

package ml.darubyminer360.cloudinstaller.test;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import ml.darubyminer360.cloudinstaller.json.Util;
import static org.junit.jupiter.api.Assertions.*;

public class TestTokens {
    @Test
    public void testTokens() {
        Map<String, String> tokens = new HashMap<>();
        tokens.put("VERSION", "1.17");
        tokens.put("NAME", "Foo");
        assertEquals(Util.replaceTokens(tokens, "{VERSION}"), "1.17");
        assertEquals(Util.replaceTokens(tokens, "{NAME}"), "Foo");
        assertEquals(Util.replaceTokens(tokens, "{NAME}-{VERSION}"), "Foo-1.17");
        assertEquals(Util.replaceTokens(tokens, "{NAME}/{VERSION}/something"), "Foo/1.17/something");
        assertEquals(Util.replaceTokens(tokens, "{VERSION}}"), "1.17}");
        assertThrows(IllegalArgumentException.class, () -> Util.replaceTokens(tokens, "{{VERSION}"));
        assertEquals(Util.replaceTokens(tokens, "'{VERSION}'"), "{VERSION}");
        assertEquals(Util.replaceTokens(tokens, "'test'"), "test");
        assertEquals(Util.replaceTokens(tokens, "This is a \\'test\\'"), "This is a 'test'");
    }
}
