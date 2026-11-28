/*
 * Copyright 2024-2026 the original author or authors.
 *
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
 */
package io.agentscope.harness.agent.memory;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MemoryInputBudgetTest {

    /** "ab", one U+1F600 (two UTF-16 chars), "cd": six chars in total. */
    private static final String CONTENT = "ab\uD83D\uDE00cd";

    @Test
    void headKeepsLeadingCharsAndNamesWhatWasCut() {
        assertEquals(
                "ab\n[... 4 trailing characters of MEMORY.md omitted ...]\n",
                MemoryInputBudget.head("MEMORY.md", CONTENT, 3));
    }

    @Test
    void tailKeepsTrailingCharsAndNamesWhatWasCut() {
        assertEquals(
                "\n[... 4 earlier characters of memory/2026-01-01.md omitted ...]\ncd",
                MemoryInputBudget.tail("memory/2026-01-01.md", CONTENT, 3));
    }

    @Test
    void contentWithinLimitIsReturnedUnchanged() {
        assertEquals(CONTENT, MemoryInputBudget.head("MEMORY.md", CONTENT, 6));
        assertEquals(CONTENT, MemoryInputBudget.tail("ledger", CONTENT, 6));
    }
}
