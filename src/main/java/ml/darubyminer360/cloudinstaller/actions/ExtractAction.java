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
import java.util.function.Predicate;

import ml.darubyminer360.cloudinstaller.DownloadUtils;
import ml.darubyminer360.cloudinstaller.json.Artifact;
import ml.darubyminer360.cloudinstaller.json.InstallV1;

public class ExtractAction extends Action {

    public ExtractAction(InstallV1 profile, ProgressCallback monitor) {
        super(profile, monitor, true);
    }

    public static boolean headless;
    @Override
    public boolean run(File target, Predicate<String> optionals, File Installer)
    {
        boolean result = true;
        String failed = "An error occurred extracting the files:";

        Artifact contained = profile.getPath();
        if (contained != null) {
            File file = new File(target, contained.getFilename());

            if (!DownloadUtils.extractFile(contained, file, null)) {
                result = false;
                failed += "\n" + contained.getFilename();
            }
        }

        /*
        for (OptionalLibrary opt : VersionInfo.getOptionals())
        {
            Artifact art = new Artifact(opt.getArtifact());
            InputStream input = ExtractAction.class.getResourceAsStream("/maven/" + art.getPath());
            if (input == null)
                continue;

            File path = art.getLocalPath(new File(target, "libraries"));
            File outFolder = art.getLocalPath(path).getParentFile();

            if (!outFolder.exists())
                outFolder.mkdirs();

            OutputSupplier<FileOutputStream> outputSupplier = Files.newOutputStreamSupplier(path);
            try
            {
                ByteStreams.copy(input, outputSupplier);
            }
            catch (IOException e)
            {
                result = false;
                failed += "\n" + opt.getArtifact();
            }
        }
        */

        if (!result)
            error(failed);

        return result;
    }

    @Override
    public boolean isPathValid(File targetDir)
    {
        return targetDir.exists() && targetDir.isDirectory();
    }

    @Override
    public String getFileError(File targetDir)
    {
        return !targetDir.exists() ? "Target directory does not exist" : !targetDir.isDirectory() ? "Target is not a directory" : "";
    }

    @Override
    public String getSuccessMessage() {
        return "Extracted successfully";
    }
}
