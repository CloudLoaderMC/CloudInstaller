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

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class OptionalLibrary {
    private String name;
    private String artifact;
    private String maven;

    private boolean client = false;
    private boolean server = false;
    private boolean _default = true;
    private boolean inject = true;
    private String desc;
    private String url;

    public boolean isValid() {
        return this.name != null && this.artifact != null && this.maven != null;
    }

    public String getName()     { return this.name;     }
    public String getArtifact() { return this.artifact; }
    public String getMaven()    { return this.maven;    }
    public boolean isClient()   { return this.client;   }
    public boolean isServer()   { return this.server;   }
    public boolean getDefault() { return this._default; }
    public boolean isInjected() { return this.inject;   }
    public String getDesc()     { return this.desc;     }
    public String getURL()      { return this.url;      }

    public static boolean saveModListJson(File root, File json, List<OptionalLibrary> libs, Predicate<String> filter)
    {
        List<String> artifacts = new ArrayList<>();
        for (OptionalLibrary lib : libs)
        {
            if (filter.test(lib.getArtifact()))
                artifacts.add(lib.getArtifact());
        }

        if (artifacts.size() == 0)
            return true;

        File parent = json.getParentFile();
        if (!parent.exists())
            parent.mkdirs();

        System.out.println("Saving optional modlist to: " + json);

        StringBuilder buf = new StringBuilder();
        buf.append("{\n");
        buf.append("    \"repositoryRoot\": \"").append(root.getAbsolutePath().replace('\\', '/')).append("\",\n");
        buf.append("    \"modRef\": [\n");
        for (int x = 0; x < artifacts.size(); x++)
        {
            buf.append("        \"").append(artifacts.get(x)).append('"');
            if (x < artifacts.size() - 1)
                buf.append(',');
            buf.append('\n');
        }
        buf.append("    ]\n");
        buf.append("}\n");

        try
        {
            Files.write(json.toPath(), buf.toString().getBytes(StandardCharsets.UTF_8));
            return true;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }
    }
}
