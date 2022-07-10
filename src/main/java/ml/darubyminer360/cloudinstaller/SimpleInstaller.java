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

package ml.darubyminer360.cloudinstaller;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import ml.darubyminer360.cloudinstaller.actions.Actions;
import ml.darubyminer360.cloudinstaller.actions.ProgressCallback;
import ml.darubyminer360.cloudinstaller.json.Util;
import ml.darubyminer360.cloudinstaller.json.InstallV1;

public class SimpleInstaller
{
    public static boolean headless = false;
    public static boolean debug = false;
    public static URL mirror = null;

    public static void main(String[] args) throws IOException, URISyntaxException
    {
        ProgressCallback monitor;
        try
        {
            monitor = ProgressCallback.withOutputs(System.out, getLog());
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
            monitor = ProgressCallback.withOutputs(System.out);
        }
        hookStdOut(monitor);

        if (System.getProperty("java.net.preferIPv4Stack") == null) //This is a dirty hack, but screw it, i'm hoping this as default will fix more things then it breaks.
        {
            System.setProperty("java.net.preferIPv4Stack", "true");
        }
        String vendor = System.getProperty("java.vendor", "missing vendor");
        String javaVersion = System.getProperty("java.version", "missing java version");
        String jvmVersion = System.getProperty("java.vm.version", "missing jvm version");
        monitor.message(String.format("JVM info: %s - %s - %s", vendor, javaVersion, jvmVersion));
        monitor.message("java.net.preferIPv4Stack=" + System.getProperty("java.net.preferIPv4Stack"));

        File installer = new File(SimpleInstaller.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        if (installer.getAbsolutePath().contains("!/"))
        {
            monitor.stage("Due to java limitation, please do not run this jar in a folder ending with !");
            monitor.message(installer.getAbsolutePath());
            return;
        }

        OptionParser parser = new OptionParser();
        OptionSpec<File> serverInstallOption = parser.accepts("installServer", "Install a server to the current directory").withOptionalArg().ofType(File.class).defaultsTo(new File("."));
        OptionSpec<File> extractOption = parser.accepts("extract", "Extract the contained jar file to the specified directory").withOptionalArg().ofType(File.class).defaultsTo(new File("."));
        OptionSpec<Void> helpOption = parser.acceptsAll(Arrays.asList("h", "help"),"Help with this installer");
        OptionSpec<Void> offlineOption = parser.accepts("offline", "Don't attempt any network calls");
        OptionSpec<Void> debugOption = parser.accepts("debug", "Run in debug mode -- don't delete any files");
        OptionSpec<URL> mirrorOption = parser.accepts("mirror", "Use a specific mirror URL").withRequiredArg().ofType(URL.class);
        OptionSet optionSet = parser.parse(args);

        if (optionSet.has(helpOption)) {
            parser.printHelpOn(System.out);
            return;
        }

        debug = optionSet.has(debugOption);
        if (optionSet.has(mirrorOption)) {
            mirror = optionSet.valueOf(mirrorOption);
        }

        if (optionSet.has(offlineOption))
        {
            DownloadUtils.OFFLINE_MODE = true;
            monitor.message("ENABLING OFFLINE MODE");
        }
        else
        {
            FixSSL.fixup(monitor);
        }

        Actions action = null;
        File target = null;
        if (optionSet.has(serverInstallOption)) {
            action = Actions.SERVER;
            target = optionSet.valueOf(serverInstallOption);
        } else if (optionSet.has(extractOption)) {
            action = Actions.EXTRACT;
            target = optionSet.valueOf(extractOption);
        }

        if (action != null)
        {
            try
            {
                SimpleInstaller.headless = true;
                monitor.message("Target Directory: " + target);
                InstallV1 install = Util.loadInstallProfile();
                if (!action.getAction(install, monitor).run(target, a -> true, installer))
                {
                    monitor.stage("There was an error during installation");
                    System.exit(1);
                }
                else
                {
                    monitor.message(action.getSuccess());
                    monitor.stage("You can delete this installer file now if you wish");
                }
                System.exit(0);
            }
            catch (Throwable e)
            {
                monitor.stage("A problem installing was detected, install cannot continue");
                System.exit(1);
            }
        }
        else
            launchGui(monitor, installer);
    }

    public static File getMCDir()
    {
        String userHomeDir = System.getProperty("user.home", ".");
        String osType = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
        String mcDir = ".minecraft";
        if (osType.contains("win") && System.getenv("APPDATA") != null)
            return new File(System.getenv("APPDATA"), mcDir);
        else if (osType.contains("mac"))
            return new File(new File(new File(userHomeDir, "Library"),"Application Support"),"minecraft");
        return new File(userHomeDir, mcDir);
    }

    private static void launchGui(ProgressCallback monitor, File installer)
    {
        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception e)
        {
        }

        try {
            InstallV1 profile = Util.loadInstallProfile();
            InstallerPanel panel = new InstallerPanel(getMCDir(), profile, installer);
            panel.run(monitor);
        } catch (Throwable e) {
            JOptionPane.showMessageDialog(null,"Something went wrong while installing.<br />Check log for more details:<br/>" + e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static OutputStream getLog() throws FileNotFoundException
    {
        File f = new File(SimpleInstaller.class.getProtectionDomain().getCodeSource().getLocation().getFile());
        File output;
        if (f.isFile()) output = new File(f.getName() + ".log");
        else            output = new File("installer.log");

        return new BufferedOutputStream(new FileOutputStream(output));
    }

    static void hookStdOut(ProgressCallback monitor)
    {
        final Pattern endingWhitespace = Pattern.compile("\\r?\\n$");
        final OutputStream monitorStream = new OutputStream() {

            @Override
            public void write(byte[] buf, int off, int len)
            {
                byte[] toWrite = new byte[len];
                System.arraycopy(buf, off, toWrite, 0, len);
                write(toWrite);
            }

            @Override
            public void write(byte[] b)
            {
                String toWrite = new String(b);
                toWrite = endingWhitespace.matcher(toWrite).replaceAll("");
                if (!toWrite.isEmpty()) {
                    monitor.message(toWrite);
                }
            }

            @Override
            public void write(int b)
            {
                write(new byte[] { (byte) b });
            }
        };

        System.setOut(new PrintStream(monitorStream));
        System.setErr(new PrintStream(monitorStream));
    }
}
