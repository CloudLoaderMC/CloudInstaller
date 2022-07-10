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

import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

public class Mirror {
    private String name;
    private String image;
    private String homepage;
    private String url;
    private boolean triedImage;
    private Icon _image_;

    public Mirror() {}

    public Mirror(String name, String image, String homepage, String url) {
        this.name = name;
        this.image = image;
        this.homepage = homepage;
        this.url = url;
    }

    public Icon getImage() {
        if (!triedImage) {
            try {
                if (getImageAddress() != null)
                    _image_ = new ImageIcon(ImageIO.read(new URL(getImageAddress())));
            } catch (Exception e) {
                _image_ = null;
            } finally {
                triedImage = true;
            }
        }
        return _image_;
    }

    public String getName() {
        return name;
    }
    public String getImageAddress() {
        return image;
    }
    public String getHomepage() {
        return homepage;
    }
    public String getUrl() {
        return url;
    }
}
