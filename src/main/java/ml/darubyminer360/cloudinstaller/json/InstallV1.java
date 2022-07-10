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

/*
 * Changes in v1 of the spec:
 * Adds a new value into the processor argument types:
 *    {ROOT} the root directory that we are installing to.
 *    {INSTALLER} the absolute path to the currently running installer.
 *    {MINECRAFT_VERSION} the version number specified in the config.
 *    {LIBRARY_DIR} Path to libraries folder. Typically {ROOT}/libraries/ but can be changed in the future.
 * Expands the token replacement for processors to allow in-line replacements. See Util.replaceTokens
 */
public class InstallV1 extends Install {
    /*
     *  The path to install the server jar to, defaults to {ROOT}/minecraft_server.{MINECRAFT_VERSION}.jar
     *  However, this is important to 'hide' the server jar in the libraries folder for Forge 1.17+
     */
    protected String serverJarPath;

    public InstallV1(Install v0) {
        this.profile = v0.profile;
        this.version = v0.version;
        this.icon = v0.icon;
        this.minecraft = v0.minecraft;
        this.json = v0.json;
        this.logo = v0.logo;
        this.path = v0.path;
        this.urlIcon = v0.urlIcon;
        this.welcome = v0.welcome;
        this.mirrorList = v0.mirrorList;
        this.hideClient = v0.hideClient;
        this.hideServer = v0.hideServer;
        this.hideExtract = v0.hideExtract;
        this.libraries = v0.libraries;
        this.processors = v0.processors;
        this.data = v0.data;
    }

    public String getServerJarPath() {
        if (this.serverJarPath == null) {
            return "{ROOT}/minecraft_server.{MINECRAFT_VERSION}.jar";
        }
        return this.serverJarPath;
    }

}
