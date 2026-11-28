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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bounds the memory files that are replayed into the flush and consolidation prompts.
 *
 * <p>{@code MEMORY.md} is only size-bounded while consolidation succeeds: {@code memory_save}
 * and the agent's own file tools append to it unbounded. Once a memory or ledger outgrows the
 * model's input window, an unbounded replay fails every flush and consolidation, so neither
 * can ever shrink the file again. The prompts therefore carry a bounded slice of each file.
 */
final class MemoryInputBudget {

    private static final Logger log = LoggerFactory.getLogger(MemoryInputBudget.class);

    private MemoryInputBudget() {}

    /** Keeps the leading {@code maxChars}: MEMORY.md leads with its most important entries. */
    static String head(String label, String content, int maxChars) {
        if (content.length() <= maxChars) {
            return content;
        }
        warn(label, content, maxChars);
        // Never cut between the two halves of a surrogate pair.
        int end = Character.isHighSurrogate(content.charAt(maxChars - 1)) ? maxChars - 1 : maxChars;
        return content.substring(0, end)
                + "\n[... "
                + (content.length() - end)
                + " trailing characters of "
                + label
                + " omitted ...]\n";
    }

    /** Keeps the trailing {@code maxChars}: a daily ledger is append-only, so its tail is newest. */
    static String tail(String label, String content, int maxChars) {
        if (content.length() <= maxChars) {
            return content;
        }
        warn(label, content, maxChars);
        // Never cut between the two halves of a surrogate pair.
        int start = content.length() - maxChars;
        if (Character.isHighSurrogate(content.charAt(start - 1))) {
            start++;
        }
        return "\n[... "
                + start
                + " earlier characters of "
                + label
                + " omitted ...]\n"
                + content.substring(start);
    }

    private static void warn(String label, String content, int keptChars) {
        log.warn(
                "{} is {} chars; replaying only {} of them to the memory model",
                label,
                content.length(),
                keptChars);
    }
}
