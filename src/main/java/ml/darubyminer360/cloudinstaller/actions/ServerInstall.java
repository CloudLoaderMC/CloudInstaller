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

package ml.darubyminer360.cloudinstaller.actions;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import ml.darubyminer360.cloudinstaller.SimpleInstaller;
import ml.darubyminer360.cloudinstaller.json.InstallV1;
import ml.darubyminer360.cloudinstaller.json.Util;
import ml.darubyminer360.cloudinstaller.json.Version;
import ml.darubyminer360.cloudinstaller.DownloadUtils;
import ml.darubyminer360.cloudinstaller.json.Artifact;

public class ServerInstall extends Action {
    private List<Artifact> grabbed = new ArrayList<>();

    public ServerInstall(InstallV1 profile, ProgressCallback monitor) {
        super(profile, monitor, false);
    }

    @Override
    public boolean run(File target, Predicate<String> optionals, File installer) throws ActionCanceledException {
        if (target.exists() && !target.isDirectory()) {
            error("There is a file at this location, the server cannot be installed here!");
            return false;
        }

        File librariesDir = new File(target,"libraries");
        if (!target.exists())
            target.mkdirs();
        librariesDir.mkdir();
        if (profile.getMirror() != null)
            monitor.stage(getSponsorMessage());
        checkCancel();

        // Extract main executable jar
        Artifact contained = profile.getPath();
        if (contained != null) {
            monitor.stage("Extracting main jar:");
            if (!DownloadUtils.extractFile(contained, new File(target, contained.getFilename()), null)) {
                error("  Failed to extract main jar: " + contained.getFilename());
                return false;
            } else
                monitor.stage("  Extracted successfully");
        }
        checkCancel();

        //Download MC Server jar
        monitor.stage("Considering minecraft server jar");
        Map<String, String> tokens = new HashMap<>();
        tokens.put("ROOT", target.getAbsolutePath());
        tokens.put("MINECRAFT_VERSION", profile.getMinecraft());
        tokens.put("LIBRARY_DIR", librariesDir.getAbsolutePath());

        String path = Util.replaceTokens(tokens, profile.getServerJarPath());
        File serverTarget = new File(path);
        if (!serverTarget.exists()) {
            File parent = serverTarget.getParentFile();
            if (!parent.exists()) {
                parent.mkdirs();
            }

            File versionJson = new File(target, profile.getMinecraft() + ".json");
            Version vanilla = Util.getVanillaVersion(profile.getMinecraft(), versionJson);
            if (vanilla == null) {
                error("Failed to download version manifest, can not find server jar URL.");
                return false;
            }
            Version.Download server = vanilla.getDownload("server");
            if (server == null) {
                error("Failed to download minecraft server, info missing from manifest: " + versionJson);
                return false;
            }

            versionJson.delete();

            if (!DownloadUtils.download(monitor, profile.getMirror(), server, serverTarget)) {
                serverTarget.delete();
                error("Downloading minecraft server failed, invalid checksum.\n" +
                      "Try again, or manually place server jar to skip download.");
                return false;
            }
        }
        checkCancel();

        // Download Libraries
        List<File> libDirs = new ArrayList<>();
        File mcLibDir = new File(SimpleInstaller.getMCDir(), "libraries");
        if (mcLibDir.exists()) {
            libDirs.add(mcLibDir);
        }
        if (!downloadLibraries(librariesDir, optionals, libDirs))
            return false;

        checkCancel();
        if (!processors.process(librariesDir, serverTarget, target, installer))
            return false;

        // TODO: Optionals
        //if (!OptionalLibrary.saveModListJson(librariesDir, new File(target, "mods/mod_list.json"), VersionInfo.getOptionals(), optionals))
        //    return false;

        return true;
    }

    @Override
    public boolean isPathValid(File targetDir) {
        return targetDir.exists() && targetDir.isDirectory() && targetDir.list().length == 0;
    }

    @Override
    public String getFileError(File targetDir) {
        if (!targetDir.exists())
            return "The specified directory does not exist<br/>It will be created";
        else if (!targetDir.isDirectory())
            return "The specified path needs to be a directory";
        else
            return "There are already files at the target directory";
    }

    @Override
    public String getSuccessMessage() {
        if (grabbed.size() > 0)
            return String.format("Successfully downloaded minecraft server, downloaded %d libraries and installed %s", grabbed.size(), profile.getVersion());
        return String.format("Successfully downloaded minecraft server and installed %s", profile.getVersion());
    }
}
