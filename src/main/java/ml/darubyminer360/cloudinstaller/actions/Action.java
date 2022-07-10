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
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import javax.swing.JOptionPane;

import ml.darubyminer360.cloudinstaller.DownloadUtils;
import ml.darubyminer360.cloudinstaller.SimpleInstaller;
import ml.darubyminer360.cloudinstaller.json.Artifact;
import ml.darubyminer360.cloudinstaller.json.InstallV1;
import ml.darubyminer360.cloudinstaller.json.Util;
import ml.darubyminer360.cloudinstaller.json.Version;
import ml.darubyminer360.cloudinstaller.json.Version.Library;
import ml.darubyminer360.cloudinstaller.json.Version.LibraryDownload;

public abstract class Action {
    protected final InstallV1 profile;
    protected final ProgressCallback monitor;
    protected final PostProcessors processors;
    protected final Version version;
    private List<Artifact> grabbed = new ArrayList<>();

    protected Action(InstallV1 profile, ProgressCallback monitor, boolean isClient) {
        this.profile = profile;
        this.monitor = monitor;
        this.processors = new PostProcessors(profile, isClient, monitor);
        this.version = Util.loadVersion(profile);
    }

    protected void error(String message) {
        if (!SimpleInstaller.headless)
            JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
        monitor.stage(message);
    }

    public abstract boolean run(File target, Predicate<String> optionals, File installer) throws ActionCanceledException;
    public abstract boolean isPathValid(File targetDir);
    public abstract String getFileError(File targetDir);
    public abstract String getSuccessMessage();

    public String getSponsorMessage() {
        return profile.getMirror() != null ? String.format(SimpleInstaller.headless ? "Data kindly mirrored by %2$s at %1$s" : "<html><a href=\'%s\'>Data kindly mirrored by %s</a></html>", profile.getMirror().getHomepage(), profile.getMirror().getName()) : null;
    }

    protected boolean downloadLibraries(File librariesDir, Predicate<String> optionals, List<File> additionalLibDirs) throws ActionCanceledException {
        monitor.start("Downloading libraries");
        String userHome = System.getProperty("user.home");
        if (userHome != null && !userHome.isEmpty()) {
            File mavenLocalHome = new File(userHome, ".m2/repository");
            if (mavenLocalHome.exists()) {
                additionalLibDirs.add(mavenLocalHome);
            }
        }
        monitor.message(String.format("Found %d additional library directories", additionalLibDirs.size()));

        List<Library> libraries = new ArrayList<>();
        libraries.addAll(Arrays.asList(version.getLibraries()));
        libraries.addAll(Arrays.asList(processors.getLibraries()));

        StringBuilder output = new StringBuilder();
        final double steps = libraries.size();
        int progress = 1;

        for (Library lib : libraries) {
            checkCancel();
            monitor.progress(progress++ / steps);
            if (!DownloadUtils.downloadLibrary(monitor, profile.getMirror(), lib, librariesDir, optionals, grabbed, additionalLibDirs)) {
                LibraryDownload download = lib.getDownloads() == null ? null :  lib.getDownloads().getArtifact();
                if (download != null && !download.getUrl().isEmpty()) // If it doesn't have a URL we can't download it, assume we install it later
                    output.append('\n').append(lib.getName());
            }
        }
        String bad = output.toString();
        if (!bad.isEmpty()) {
            error("These libraries failed to download. Try again.\n" + bad);
            return false;
        }
        return true;
    }

    protected int downloadedCount() {
        return grabbed.size();
    }

    protected int getTaskCount() {
        return profile.getLibraries().length + processors.getTaskCount();
    }

    protected void checkCancel() throws ActionCanceledException {
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            throw new ActionCanceledException(e);
        }
    }
}
