/*
 * Copyright 2014 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.spinnaker.echo.cassandra

import com.netflix.astyanax.MutationBatch
import spock.lang.Ignore
import spock.lang.Shared

class HistoryRepositorySpec extends AbstractCassandraBackedSpec {

    @Shared
    HistoryRepository repo

    void setupSpec() {
        repo = new HistoryRepository()
        repo.keyspace = keyspace
        repo.onApplicationEvent(null)
    }

    void setup() {
        MutationBatch m = keyspace.prepareMutationBatch()
        m.withRow(HistoryRepository.CF_HISTORY, HistoryRepository.CF_NAME).delete()
        m.execute()
    }

    void 'initial list of history is empty'() {
        expect:
        repo.listHistory().empty
    }

    void 'can add, delete and retrieve history'() {
        when:
        repo.saveHistory('s1', '''{"step":"s1"}''')
        repo.saveHistory('s2', '''{"step":"s2"}''')

        and:
        List<Map> histories = repo.listHistory()

        then:
        histories.size() == 2
        histories[0].step == 's1'

        when:
        Map history = repo.getHistory('s2')

        then:
        history.step == 's2'

        when:
        repo.deleteHistory('s2')
        histories = repo.listHistory()

        then:
        histories.size() == 1

        when:
        repo.deleteHistory('s1')
        histories = repo.listHistory()

        then:
        histories.empty
    }

    void 'returns null when requested history does not exist'() {
        expect:
        repo.getHistory('does-not-exist') == null
    }

}
