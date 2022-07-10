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

import java.util.function.BiFunction;
import java.util.function.Supplier;

import ml.darubyminer360.cloudinstaller.json.InstallV1;

public enum Actions
{
    CLIENT("Install client", "Install a new profile to the Mojang client launcher", ClientInstall::new, () -> "Successfully installed client into launcher."),
    SERVER("Install server", "Create a new modded server installation", ServerInstall::new, () -> "The server installed successfully"),
    EXTRACT("Extract", "Extract the contained jar file", ExtractAction::new, () -> "All files successfully extract.");

    private String label;
    private String tooltip;
    private BiFunction<InstallV1, ProgressCallback, Action> action;
    private Supplier<String> success;

    private Actions(String label, String tooltip, BiFunction<InstallV1, ProgressCallback, Action> action, Supplier<String> success)
    {
        this.label = label;
        this.tooltip = tooltip;
        this.success = success;
        this.action = action;
    }

    public String getButtonLabel()
    {
        return label;
    }

    public String getTooltip()
    {
        return tooltip;
    }

    public String getSuccess()
    {
        return success.get();
    }

    public Action getAction(InstallV1 profile, ProgressCallback monitor) {
        return action.apply(profile, monitor);
    }
}
