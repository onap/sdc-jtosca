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

package org.onap.sdc.toscaparser.api.prereq;

import org.onap.sdc.toscaparser.api.ImportsLoader;
import org.onap.sdc.toscaparser.api.common.JToscaValidationIssue;
import org.onap.sdc.toscaparser.api.utils.ThreadLocalsHolder;
import org.onap.sdc.toscaparser.api.utils.UrlUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.onap.sdc.toscaparser.api.common.JToscaException;
import org.onap.sdc.toscaparser.api.utils.JToscaErrorCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

public class CSAR {

    private static Logger log = LoggerFactory.getLogger(CSAR.class.getName());
    private static final ArrayList<String> META_PROPERTIES_FILES = new ArrayList<>(Arrays.asList("TOSCA-Metadata/TOSCA.meta", "csar.meta"));

    private String path;
    private boolean isFile;
    private boolean isValidated;
    private boolean errorCaught;
    private String csar;
    private String tempDir;
    //    private Metadata metaData;
    private File tempFile;
    private LinkedHashMap<String, LinkedHashMap<String, Object>> metaProperties;

    public CSAR(String csarPath, boolean aFile) {
        path = csarPath;
        isFile = aFile;
        isValidated = false;
        errorCaught = false;
        csar = null;
        tempDir = null;
        tempFile = null;
        metaProperties = new LinkedHashMap<>();
    }

    public boolean validate() throws JToscaException {
        isValidated = true;

        //validate that the file or URL exists

        if (isFile) {
            File f = new File(path);
            if (!f.isFile()) {
                ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE220", String.format("\"%s\" is not a file", path)));
                return false;
            } else {
                this.csar = path;
            }
        } else {
            if (!UrlUtils.validateUrl(path)) {
                ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE221", String.format("ImportError: \"%s\" does not exist", path)));
                return false;
            }
            // get it to a local file
            try {
                File tempFile = File.createTempFile("csartmp", ".csar");
                Path ptf = Paths.get(tempFile.getPath());
                URL webfile = new URL(path);
                InputStream in = webfile.openStream();
                Files.copy(in, ptf, StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
                ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE222", "ImportError: failed to load CSAR from " + path));
                return false;
            }

            log.debug("CSAR - validate - currently only files are supported");
            return false;
        }

        _parseAndValidateMetaProperties();

        if (errorCaught) {
            return false;
        }

        // validate that external references in the main template actually exist and are accessible
        _validateExternalReferences();

        return !errorCaught;

    }

    private void _parseAndValidateMetaProperties() throws JToscaException {

        ZipFile zf = null;

        try {

            // validate that it is a valid zip file
            RandomAccessFile raf = new RandomAccessFile(csar, "r");
            long n = raf.readInt();
            raf.close();
            // check if Zip's magic number
            if (n != 0x504B0304) {
                String errorString = String.format("\"%s\" is not a valid zip file", csar);
                log.error(errorString);
                throw new JToscaException(errorString, JToscaErrorCodes.INVALID_CSAR_FORMAT.getValue());
            }

            // validate that it contains the metadata file in the correct location
            zf = new ZipFile(csar);
            ZipEntry ze = zf.getEntry("TOSCA-Metadata/TOSCA.meta");
            if (ze == null) {

                String errorString = String.format(
                        "\"%s\" is not a valid CSAR as it does not contain the " +
                                "required file \"TOSCA.meta\" in the folder \"TOSCA-Metadata\"", csar);
                log.error(errorString);
                throw new JToscaException(errorString, JToscaErrorCodes.MISSING_META_FILE.getValue());
            }

            //Going over expected metadata files and parsing them
            for (String metaFile : META_PROPERTIES_FILES) {

                byte ba[] = new byte[4096];
                ze = zf.getEntry(metaFile);
                if (ze != null) {
                    InputStream inputStream = zf.getInputStream(ze);
                    n = inputStream.read(ba, 0, 4096);
                    String md = new String(ba);
                    md = md.substring(0, (int) n);

                    String errorString = String.format(
                            "The file \"%s\" in the" +
                                    " CSAR \"%s\" does not contain valid YAML content", ze.getName(), csar);

                    try {
                        Yaml yaml = new Yaml();
                        Object mdo = yaml.load(md);
                        if (!(mdo instanceof LinkedHashMap)) {
                            log.error(errorString);
                            throw new JToscaException(errorString, JToscaErrorCodes.INVALID_META_YAML_CONTENT.getValue());
                        }

                        String[] split = ze.getName().split("/");
                        String fileName = split[split.length - 1];

                        if (!metaProperties.containsKey(fileName)) {
                            metaProperties.put(fileName, (LinkedHashMap<String, Object>) mdo);
                        }
                    } catch (Exception e) {
                        log.error(errorString);
                        throw new JToscaException(errorString, JToscaErrorCodes.INVALID_META_YAML_CONTENT.getValue());
                    }
                }
            }

            // verify it has "Entry-Definition"
            String edf = _getMetadata("Entry-Definitions");
            if (edf == null) {
                String errorString = String.format(
                        "The CSAR \"%s\" is missing the required metadata " +
                                "\"Entry-Definitions\" in \"TOSCA-Metadata/TOSCA.meta\"", csar);
                log.error(errorString);
                throw new JToscaException(errorString, JToscaErrorCodes.ENTRY_DEFINITION_NOT_DEFINED.getValue());
            }

            //validate that "Entry-Definitions' metadata value points to an existing file in the CSAR
            boolean foundEDF = false;
            Enumeration<? extends ZipEntry> entries = zf.entries();
            while (entries.hasMoreElements()) {
                ze = entries.nextElement();
                if (ze.getName().equals(edf)) {
                    foundEDF = true;
                    break;
                }
            }
            if (!foundEDF) {
                String errorString = String.format(
                        "The \"Entry-Definitions\" file defined in the CSAR \"%s\" does not exist", csar);
                log.error(errorString);
                throw new JToscaException(errorString, JToscaErrorCodes.MISSING_ENTRY_DEFINITION_FILE.getValue());
            }
        } catch (JToscaException e) {
            //ThreadLocalsHolder.getCollector().appendCriticalException(e.getMessage());
            throw e;
        } catch (Exception e) {
            ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE223", "ValidationError: " + e.getMessage()));
            errorCaught = true;
        }

        try {
            if (zf != null) {
                zf.close();
            }
        } catch (IOException e) {
        }
    }

    public void cleanup() {
        try {
            if (tempFile != null) {
                tempFile.delete();
            }
        } catch (Exception e) {
        }
    }

    private String _getMetadata(String key) throws JToscaException {
        if (!isValidated) {
            validate();
        }
        Object value = _getMetaProperty("TOSCA.meta").get(key);
        return value != null ? value.toString() : null;
    }

    public String getAuthor() throws JToscaException {
        return _getMetadata("Created-By");
    }

    public String getVersion() throws JToscaException {
        return _getMetadata("CSAR-Version");
    }

    public LinkedHashMap<String, LinkedHashMap<String, Object>> getMetaProperties() {
        return metaProperties;
    }

    private LinkedHashMap<String, Object> _getMetaProperty(String propertiesFile) {
        return metaProperties.get(propertiesFile);
    }

    public String getMainTemplate() throws JToscaException {
        String entryDef = _getMetadata("Entry-Definitions");
        ZipFile zf;
        boolean ok = false;
        try {
            zf = new ZipFile(path);
            ok = (zf.getEntry(entryDef) != null);
            zf.close();
        } catch (IOException e) {
            if (!ok) {
                log.error("CSAR - getMainTemplate - failed to open {}", path);
            }
        }
        if (ok) {
            return entryDef;
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public LinkedHashMap<String, Object> getMainTemplateYaml() throws JToscaException {
        String mainTemplate = tempDir + File.separator + getMainTemplate();
        if (mainTemplate != null) {
            try (InputStream input = new FileInputStream(new File(mainTemplate));) {
                Yaml yaml = new Yaml();
                Object data = yaml.load(input);
                if (!(data instanceof LinkedHashMap)) {
                    throw new IOException();
                }
                return (LinkedHashMap<String, Object>) data;
            } catch (Exception e) {
                ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE224", String.format(
                        "The file \"%s\" in the CSAR \"%s\" does not " +
                                "contain valid TOSCA YAML content",
                        mainTemplate, csar)));
            }
        }
        return null;
    }

    public String getDescription() throws JToscaException {
        String desc = _getMetadata("Description");
        if (desc != null) {
            return desc;
        }

        Map<String, Object> metaData = metaProperties.get("TOSCA.meta");
        metaData.put("Description", getMainTemplateYaml().get("description"));
        return _getMetadata("Description");
    }

    public String getTempDir() {
        return tempDir;
    }

    public void decompress() throws IOException, JToscaException {
        if (!isValidated) {
            validate();
        }

        if (tempDir == null || tempDir.isEmpty()) {
            tempDir = Files.createTempDirectory("JTP").toString();
            unzip(path, tempDir);
        }
    }

    private void _validateExternalReferences() throws JToscaException {
        // Extracts files referenced in the main template
        // These references are currently supported:
        // * imports
        // * interface implementations
        // * artifacts
        try {
            decompress();
            String mainTplFile = getMainTemplate();
            if (mainTplFile == null) {
                return;
            }

            LinkedHashMap<String, Object> mainTpl = getMainTemplateYaml();
            if (mainTpl.get("imports") != null) {
                // this loads the imports
                ImportsLoader il = new ImportsLoader((ArrayList<Object>) mainTpl.get("imports"),
                        tempDir + File.separator + mainTplFile,
                        (Object) null,
                        (LinkedHashMap<String, Object>) null);
            }

            if (mainTpl.get("topology_template") != null) {
                LinkedHashMap<String, Object> topologyTemplate =
                        (LinkedHashMap<String, Object>) mainTpl.get("topology_template");

                if (topologyTemplate.get("node_templates") != null) {
                    LinkedHashMap<String, Object> nodeTemplates =
                            (LinkedHashMap<String, Object>) topologyTemplate.get("node_templates");
                    for (String nodeTemplateKey : nodeTemplates.keySet()) {
                        LinkedHashMap<String, Object> nodeTemplate =
                                (LinkedHashMap<String, Object>) nodeTemplates.get(nodeTemplateKey);
                        if (nodeTemplate.get("artifacts") != null) {
                            LinkedHashMap<String, Object> artifacts =
                                    (LinkedHashMap<String, Object>) nodeTemplate.get("artifacts");
                            for (String artifactKey : artifacts.keySet()) {
                                Object artifact = artifacts.get(artifactKey);
                                if (artifact instanceof String) {
                                    _validateExternalReference(mainTplFile, (String) artifact, true);
                                } else if (artifact instanceof LinkedHashMap) {
                                    String file = (String) ((LinkedHashMap<String, Object>) artifact).get("file");
                                    if (file != null) {
                                        _validateExternalReference(mainTplFile, file, true);
                                    }
                                } else {
                                    ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE225", String.format(
                                            "ValueError: Unexpected artifact definition for \"%s\"",
                                            artifactKey)));
                                    errorCaught = true;
                                }
                            }
                        }
                        if (nodeTemplate.get("interfaces") != null) {
                            LinkedHashMap<String, Object> interfaces =
                                    (LinkedHashMap<String, Object>) nodeTemplate.get("interfaces");
                            for (String interfaceKey : interfaces.keySet()) {
                                LinkedHashMap<String, Object> _interface =
                                        (LinkedHashMap<String, Object>) interfaces.get(interfaceKey);
                                for (String operationKey : _interface.keySet()) {
                                    Object operation = _interface.get(operationKey);
                                    if (operation instanceof String) {
                                        _validateExternalReference(mainTplFile, (String) operation, false);
                                    } else if (operation instanceof LinkedHashMap) {
                                        String imp = (String) ((LinkedHashMap<String, Object>) operation).get("implementation");
                                        if (imp != null) {
                                            _validateExternalReference(mainTplFile, imp, true);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            errorCaught = true;
        } finally {
            // delete tempDir (only here?!?)
            File fdir = new File(tempDir);
            deleteDir(fdir);
            tempDir = null;
        }
    }

    public static void deleteDir(File fdir) {
        try {
            if (fdir.isDirectory()) {
                for (File c : fdir.listFiles())
                    deleteDir(c);
            }
            fdir.delete();
        } catch (Exception e) {
        }
    }

    private void _validateExternalReference(String tplFile, String resourceFile, boolean raiseExc) {
        // Verify that the external resource exists

        // If resource_file is a URL verify that the URL is valid.
        // If resource_file is a relative path verify that the path is valid
        // considering base folder (self.temp_dir) and tpl_file.
        // Note that in a CSAR resource_file cannot be an absolute path.
        if (UrlUtils.validateUrl(resourceFile)) {
            String msg = String.format("URLException: The resource at \"%s\" cannot be accessed", resourceFile);
            try {
                if (UrlUtils.isUrlAccessible(resourceFile)) {
                    return;
                } else {
                    ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE226", msg));
                    errorCaught = true;
                }
            } catch (Exception e) {
                ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE227", msg));
            }
        }

        String dirPath = Paths.get(tplFile).getParent().toString();
        String filePath = tempDir + File.separator + dirPath + File.separator + resourceFile;
        File f = new File(filePath);
        if (f.isFile()) {
            return;
        }

        if (raiseExc) {
            ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE228", String.format(
                    "ValueError: The resource \"%s\" does not exist", resourceFile)));
        }
        errorCaught = true;
    }

    private void unzip(String zipFilePath, String destDirectory) throws IOException {
        File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            destDir.mkdir();
        }

        try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));) {
            ZipEntry entry = zipIn.getNextEntry();
            // iterates over entries in the zip file
            while (entry != null) {
                // create all directories needed for nested items
                String[] parts = entry.getName().split("/");
                String s = destDirectory + File.separator;
                for (int i = 0; i < parts.length - 1; i++) {
                    s += parts[i];
                    File idir = new File(s);
                    if (!idir.exists()) {
                        idir.mkdir();
                    }
                    s += File.separator;
                }
                String filePath = destDirectory + File.separator + entry.getName();
                if (!entry.isDirectory()) {
                    // if the entry is a file, extracts it
                    extractFile(zipIn, filePath);
                } else {
                    // if the entry is a directory, make the directory
                    File dir = new File(filePath);
                    dir.mkdir();
                }
                zipIn.closeEntry();
                entry = zipIn.getNextEntry();
            }
        }
    }

    /**
     * Extracts a zip entry (file entry)
     *
     * @param zipIn
     * @param filePath
     * @throws IOException
     */
    private static final int BUFFER_SIZE = 4096;

    private void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        //BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        try (FileOutputStream fos = new FileOutputStream(filePath);
             BufferedOutputStream bos = new BufferedOutputStream(fos);) {
            byte[] bytesIn = new byte[BUFFER_SIZE];
            int read = 0;
            while ((read = zipIn.read(bytesIn)) != -1) {
                bos.write(bytesIn, 0, read);
            }
        }
    }

}	

/*python

from toscaparser.common.exception import ValidationIssueCollector
from toscaparser.common.exception import URLException
from toscaparser.common.exception import ValidationError
from toscaparser.imports import ImportsLoader
from toscaparser.utils.gettextutils import _
from toscaparser.utils.urlutils import UrlUtils

try:  # Python 2.x
    from BytesIO import BytesIO
except ImportError:  # Python 3.x
    from io import BytesIO


class CSAR(object):

    def __init__(self, csar_file, a_file=True):
        self.path = csar_file
        self.a_file = a_file
        self.is_validated = False
        self.error_caught = False
        self.csar = None
        self.temp_dir = None

    def validate(self):
        """Validate the provided CSAR file."""

        self.is_validated = True

        # validate that the file or URL exists
        missing_err_msg = (_('"%s" does not exist.') % self.path)
        if self.a_file:
            if not os.path.isfile(self.path):
                ValidationIssueCollector.appendException(
                    ValidationError(message=missing_err_msg))
                return False
            else:
                self.csar = self.path
        else:  # a URL
            if not UrlUtils.validate_url(self.path):
                ValidationIssueCollector.appendException(
                    ValidationError(message=missing_err_msg))
                return False
            else:
                response = requests.get(self.path)
                self.csar = BytesIO(response.content)

        # validate that it is a valid zip file
        if not zipfile.is_zipfile(self.csar):
            err_msg = (_('"%s" is not a valid zip file.') % self.path)
            ValidationIssueCollector.appendException(
                ValidationError(message=err_msg))
            return False

        # validate that it contains the metadata file in the correct location
        self.zfile = zipfile.ZipFile(self.csar, 'r')
        filelist = self.zfile.namelist()
        if 'TOSCA-Metadata/TOSCA.meta' not in filelist:
            err_msg = (_('"%s" is not a valid CSAR as it does not contain the '
                         'required file "TOSCA.meta" in the folder '
                         '"TOSCA-Metadata".') % self.path)
            ValidationIssueCollector.appendException(
                ValidationError(message=err_msg))
            return False

        # validate that 'Entry-Definitions' property exists in TOSCA.meta
        data = self.zfile.read('TOSCA-Metadata/TOSCA.meta')
        invalid_yaml_err_msg = (_('The file "TOSCA-Metadata/TOSCA.meta" in '
                                  'the CSAR "%s" does not contain valid YAML '
                                  'content.') % self.path)
        try:
            meta = yaml.load(data)
            if type(meta) is dict:
                self.metadata = meta
            else:
                ValidationIssueCollector.appendException(
                    ValidationError(message=invalid_yaml_err_msg))
                return False
        except yaml.YAMLError:
            ValidationIssueCollector.appendException(
                ValidationError(message=invalid_yaml_err_msg))
            return False

        if 'Entry-Definitions' not in self.metadata:
            err_msg = (_('The CSAR "%s" is missing the required metadata '
                         '"Entry-Definitions" in '
                         '"TOSCA-Metadata/TOSCA.meta".')
                       % self.path)
            ValidationIssueCollector.appendException(
                ValidationError(message=err_msg))
            return False

        # validate that 'Entry-Definitions' metadata value points to an
        # existing file in the CSAR
        entry = self.metadata.get('Entry-Definitions')
        if entry and entry not in filelist:
            err_msg = (_('The "Entry-Definitions" file defined in the '
                         'CSAR "%s" does not exist.') % self.path)
            ValidationIssueCollector.appendException(
                ValidationError(message=err_msg))
            return False

        # validate that external references in the main template actually
        # exist and are accessible
        self._validate_external_references()
        return not self.error_caught

    def get_metadata(self):
        """Return the metadata dictionary."""

        # validate the csar if not already validated
        if not self.is_validated:
            self.validate()

        # return a copy to avoid changes overwrite the original
        return dict(self.metadata) if self.metadata else None

    def _get_metadata(self, key):
        if not self.is_validated:
            self.validate()
        return self.metadata.get(key)

    def get_author(self):
        return self._get_metadata('Created-By')

    def get_version(self):
        return self._get_metadata('CSAR-Version')

    def get_main_template(self):
        entry_def = self._get_metadata('Entry-Definitions')
        if entry_def in self.zfile.namelist():
            return entry_def

    def get_main_template_yaml(self):
        main_template = self.get_main_template()
        if main_template:
            data = self.zfile.read(main_template)
            invalid_tosca_yaml_err_msg = (
                _('The file "%(template)s" in the CSAR "%(csar)s" does not '
                  'contain valid TOSCA YAML content.') %
                {'template': main_template, 'csar': self.path})
            try:
                tosca_yaml = yaml.load(data)
                if type(tosca_yaml) is not dict:
                    ValidationIssueCollector.appendException(
                        ValidationError(message=invalid_tosca_yaml_err_msg))
                return tosca_yaml
            except Exception:
                ValidationIssueCollector.appendException(
                    ValidationError(message=invalid_tosca_yaml_err_msg))

    def get_description(self):
        desc = self._get_metadata('Description')
        if desc is not None:
            return desc

        self.metadata['Description'] = \
            self.get_main_template_yaml().get('description')
        return self.metadata['Description']

    def decompress(self):
        if not self.is_validated:
            self.validate()
        self.temp_dir = tempfile.NamedTemporaryFile().name
        with zipfile.ZipFile(self.csar, "r") as zf:
            zf.extractall(self.temp_dir)

    def _validate_external_references(self):
        """Extracts files referenced in the main template

        These references are currently supported:
        * imports
        * interface implementations
        * artifacts
        """
        try:
            self.decompress()
            main_tpl_file = self.get_main_template()
            if not main_tpl_file:
                return
            main_tpl = self.get_main_template_yaml()

            if 'imports' in main_tpl:
                ImportsLoader(main_tpl['imports'],
                              os.path.join(self.temp_dir, main_tpl_file))

            if 'topology_template' in main_tpl:
                topology_template = main_tpl['topology_template']

                if 'node_templates' in topology_template:
                    node_templates = topology_template['node_templates']

                    for node_template_key in node_templates:
                        node_template = node_templates[node_template_key]
                        if 'artifacts' in node_template:
                            artifacts = node_template['artifacts']
                            for artifact_key in artifacts:
                                artifact = artifacts[artifact_key]
                                if isinstance(artifact, six.string_types):
                                    self._validate_external_reference(
                                        main_tpl_file,
                                        artifact)
                                elif isinstance(artifact, dict):
                                    if 'file' in artifact:
                                        self._validate_external_reference(
                                            main_tpl_file,
                                            artifact['file'])
                                else:
                                    ValidationIssueCollector.appendException(
                                        ValueError(_('Unexpected artifact '
                                                     'definition for "%s".')
                                                   % artifact_key))
                                    self.error_caught = True
                        if 'interfaces' in node_template:
                            interfaces = node_template['interfaces']
                            for interface_key in interfaces:
                                interface = interfaces[interface_key]
                                for opertation_key in interface:
                                    operation = interface[opertation_key]
                                    if isinstance(operation, six.string_types):
                                        self._validate_external_reference(
                                            main_tpl_file,
                                            operation,
                                            False)
                                    elif isinstance(operation, dict):
                                        if 'implementation' in operation:
                                            self._validate_external_reference(
                                                main_tpl_file,
                                                operation['implementation'])
        finally:
            if self.temp_dir:
                shutil.rmtree(self.temp_dir)

    def _validate_external_reference(self, tpl_file, resource_file,
                                     raise_exc=True):
        """Verify that the external resource exists

        If resource_file is a URL verify that the URL is valid.
        If resource_file is a relative path verify that the path is valid
        considering base folder (self.temp_dir) and tpl_file.
        Note that in a CSAR resource_file cannot be an absolute path.
        """
        if UrlUtils.validate_url(resource_file):
            msg = (_('The resource at "%s" cannot be accessed.') %
                   resource_file)
            try:
                if UrlUtils.url_accessible(resource_file):
                    return
                else:
                    ValidationIssueCollector.appendException(
                        URLException(what=msg))
                    self.error_caught = True
            except Exception:
                ValidationIssueCollector.appendException(
                    URLException(what=msg))
                self.error_caught = True

        if os.path.isfile(os.path.join(self.temp_dir,
                                       os.path.dirname(tpl_file),
                                       resource_file)):
            return

        if raise_exc:
            ValidationIssueCollector.appendException(
                ValueError(_('The resource "%s" does not exist.')
                           % resource_file))
            self.error_caught = True
*/


