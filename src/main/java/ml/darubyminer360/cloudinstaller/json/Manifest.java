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

package ml.darubyminer360.cloudinstaller.json;

import java.util.List;

public class Manifest {
    private List<Info> versions;

    public String getUrl(String version) {
        return versions == null ? null : versions.stream().filter(v -> version.equals(v.getId())).map(Info::getUrl).findFirst().orElse(null);
    }

    public static class Info {
        private String id;
        private String url;

        public String getId() {
            return id;
        }

        public String getUrl() {
            return url;
        }
    }
}
