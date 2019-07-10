/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.sdc.toscaparser.api.extensions;

import org.onap.sdc.toscaparser.api.common.JToscaValidationIssue;
import org.onap.sdc.toscaparser.api.utils.ThreadLocalsHolder;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExtTools {

    private static Logger log = LoggerFactory.getLogger(ExtTools.class.getName());

    private static LinkedHashMap<String, Object> extensionInfo = new LinkedHashMap<>();

    public ExtTools() {
        extensionInfo = loadExtensions();
    }

    private LinkedHashMap<String, Object> loadExtensions() {

        LinkedHashMap<String, Object> extensions = new LinkedHashMap<>();

        Reflections reflections = new Reflections("extensions", new ResourcesScanner());
        Set<String> resourcePaths = reflections.getResources(Pattern.compile(".*\\.py$"));

        for (String resourcePath : resourcePaths) {
            try (InputStream is = ExtTools.class.getClassLoader().getResourceAsStream(resourcePath);
                 InputStreamReader isr = new InputStreamReader(is, Charset.forName("UTF-8"));
                 BufferedReader br = new BufferedReader(isr);) {
                String version = null;
                ArrayList<String> sections = null;
                String defsFile = null;
                String line;

                Pattern pattern = Pattern.compile("^([^#]\\S+)\\s*=\\s*(\\S.*)$");
                while ((line = br.readLine()) != null) {
                    line = line.replace("'", "\"");
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        if (matcher.group(1).equals("VERSION")) {
                            version = matcher.group(2);
                            if (version.startsWith("'") || version.startsWith("\"")) {
                                version = version.substring(1, version.length() - 1);
                            }
                        } else if (matcher.group(1).equals("DEFS_FILE")) {
                            String fn = matcher.group(2);
                            if (fn.startsWith("'") || fn.startsWith("\"")) {
                                fn = fn.substring(1, fn.length() - 1);
                            }
                            defsFile = resourcePath.replaceFirst("\\w*.py$", fn);
                        } else if (matcher.group(1).equals("SECTIONS")) {
                            sections = new ArrayList<>();
                            Pattern secpat = Pattern.compile("\"([^\"]+)\"");
                            Matcher secmat = secpat.matcher(matcher.group(2));
                            while (secmat.find()) {
                                sections.add(secmat.group(1));
                            }
                        }
                    }
                }

                if (version != null && defsFile != null) {
                    LinkedHashMap<String, Object> ext = new LinkedHashMap<>();
                    ext.put("defs_file", defsFile);
                    if (sections != null) {
                        ext.put("sections", sections);
                    }
                    extensions.put(version, ext);
                }
            } catch (Exception e) {
                log.error("ExtTools - loadExtensions - {}", e);
                ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue(
                        "JE281", "Failed to load extensions" + e.getMessage()));
            }
        }
        return extensions;
    }

    public ArrayList<String> getVersions() {
        return new ArrayList<String>(extensionInfo.keySet());
    }

    public LinkedHashMap<String, ArrayList<String>> getSections() {
        LinkedHashMap<String, ArrayList<String>> sections = new LinkedHashMap<>();
        for (String version : extensionInfo.keySet()) {
            LinkedHashMap<String, Object> eiv = (LinkedHashMap<String, Object>) extensionInfo.get(version);
            sections.put(version, (ArrayList<String>) eiv.get("sections"));
        }
        return sections;
    }

    public String getDefsFile(String version) {
        LinkedHashMap<String, Object> eiv = (LinkedHashMap<String, Object>) extensionInfo.get(version);
        return (String) eiv.get("defs_file");
    }

}

/*python

from toscaparser.common.exception import ToscaExtAttributeError
from toscaparser.common.exception import ToscaExtImportError

log = logging.getLogger("tosca.model")

REQUIRED_ATTRIBUTES = ['VERSION', 'DEFS_FILE']


class ExtTools(object):
    def __init__(self):
        self.extensionInfo = self._load_extensions()

    def _load_extensions(self):
        '''Dynamically load all the extensions .'''
        extensions = {}

        # Use the absolute path of the class path
        abs_path = os.path.dirname(os.path.abspath(__file__))

        extdirs = [e for e in os.listdir(abs_path) if
                   not e.startswith('tests') and
                   os.path.isdir(os.path.join(abs_path, e))]

        for e in extdirs:
            log.info(e)
            extpath = abs_path + '/' + e
            # Grab all the extension files in the given path
            ext_files = [f for f in os.listdir(extpath) if f.endswith('.py')
                         and not f.startswith('__init__')]

            # For each module, pick out the target translation class
            for f in ext_files:
                log.info(f)
                ext_name = 'toscaparser/extensions/' + e + '/' + f.strip('.py')
                ext_name = ext_name.replace('/', '.')
                try:
                    extinfo = importlib.import_module(ext_name)
                    version = getattr(extinfo, 'VERSION')
                    defs_file = extpath + '/' + getattr(extinfo, 'DEFS_FILE')

                    # Sections is an optional attribute
                    sections = getattr(extinfo, 'SECTIONS', ())

                    extensions[version] = {'sections': sections,
                                           'defs_file': defs_file}
                except ImportError:
                    raise ToscaExtImportError(ext_name=ext_name)
                except AttributeError:
                    attrs = ', '.join(REQUIRED_ATTRIBUTES)
                    raise ToscaExtAttributeError(ext_name=ext_name,
                                                 attrs=attrs)

        print 'Extensions ',extensions#GGG
        return extensions

    def get_versions(self):
        return self.extensionInfo.keys()

    def get_sections(self):
        sections = {}
        for version in self.extensionInfo.keys():
            sections[version] = self.extensionInfo[version]['sections']

        return sections

    def get_defs_file(self, version):
        versiondata = self.extensionInfo.get(version)

        if versiondata:
            return versiondata.get('defs_file')
        else:
            return None
*/
