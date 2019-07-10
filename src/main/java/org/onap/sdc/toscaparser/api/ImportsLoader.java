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

package org.onap.sdc.toscaparser.api;

import com.google.common.base.Charsets;
import org.onap.sdc.toscaparser.api.common.JToscaValidationIssue;
import org.onap.sdc.toscaparser.api.utils.ThreadLocalsHolder;
import org.onap.sdc.toscaparser.api.utils.UrlUtils;

import org.onap.sdc.toscaparser.api.elements.TypeValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;

public class ImportsLoader {

    private static Logger log = LoggerFactory.getLogger(ImportsLoader.class.getName());
    private static final String FILE = "file";
    private static final String REPOSITORY = "repository";
    private static final String NAMESPACE_URI = "namespace_uri";
    private static final String NAMESPACE_PREFIX = "namespace_prefix";
    private String IMPORTS_SECTION[] = {FILE, REPOSITORY, NAMESPACE_URI, NAMESPACE_PREFIX};

    private ArrayList<Object> importslist;
    private String path;
    private ArrayList<String> typeDefinitionList;

    private LinkedHashMap<String, Object> customDefs;
    private LinkedHashMap<String, Object> allCustomDefs;
    private ArrayList<LinkedHashMap<String, Object>> nestedToscaTpls;
    private LinkedHashMap<String, Object> repositories;

    @SuppressWarnings("unchecked")
    public ImportsLoader(ArrayList<Object> _importslist,
                         String _path,
                         Object _typeDefinitionList,
                         LinkedHashMap<String, Object> tpl) {

        this.importslist = _importslist;
        customDefs = new LinkedHashMap<String, Object>();
        allCustomDefs = new LinkedHashMap<String, Object>();
        nestedToscaTpls = new ArrayList<LinkedHashMap<String, Object>>();
        if ((_path == null || _path.isEmpty()) && tpl == null) {
            //msg = _('Input tosca template is not provided.')
            //log.warning(msg)
            ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE184", "ValidationError: Input tosca template is not provided"));
        }

        this.path = _path;
        this.repositories = new LinkedHashMap<String, Object>();

        if (tpl != null && tpl.get("repositories") != null) {
            this.repositories = (LinkedHashMap<String, Object>) tpl.get("repositories");
        }
        this.typeDefinitionList = new ArrayList<String>();
        if (_typeDefinitionList != null) {
            if (_typeDefinitionList instanceof ArrayList) {
                this.typeDefinitionList = (ArrayList<String>) _typeDefinitionList;
            } else {
                this.typeDefinitionList.add((String) _typeDefinitionList);
            }
        }
        _validateAndLoadImports();
    }

    public LinkedHashMap<String, Object> getCustomDefs() {
        return allCustomDefs;
    }

    public ArrayList<LinkedHashMap<String, Object>> getNestedToscaTpls() {
        return nestedToscaTpls;
    }

    @SuppressWarnings({"unchecked", "unused"})
    public void _validateAndLoadImports() {
        Set<String> importNames = new HashSet<String>();

        if (importslist == null) {
            //msg = _('"imports" keyname is defined without including templates.')
            //log.error(msg)
            ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE185",
                    "ValidationError: \"imports\" keyname is defined without including templates"));
            return;
        }

        for (Object importDef : importslist) {
            String fullFileName = null;
            LinkedHashMap<String, Object> customType = null;
            if (importDef instanceof LinkedHashMap) {
                for (Map.Entry<String, Object> me : ((LinkedHashMap<String, Object>) importDef).entrySet()) {
                    String importName = me.getKey();
                    Object importUri = me.getValue();
                    if (importNames.contains(importName)) {
                        //msg = (_('Duplicate import name "%s" was found.') % import_name)
                        //log.error(msg)
                        ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE186", String.format(
                                "ValidationError: Duplicate import name \"%s\" was found", importName)));
                    }
                    importNames.add(importName); //???

                    // _loadImportTemplate returns 2 objects
                    Object ffnct[] = _loadImportTemplate(importName, importUri);
                    fullFileName = (String) ffnct[0];
                    customType = (LinkedHashMap<String, Object>) ffnct[1];
                    String namespacePrefix = "";
                    if (importUri instanceof LinkedHashMap) {
                        namespacePrefix = (String)
                                ((LinkedHashMap<String, Object>) importUri).get(NAMESPACE_PREFIX);
                    }

                    if (customType != null) {
                        TypeValidation tv = new TypeValidation(customType, importDef);
                        _updateCustomDefs(customType, namespacePrefix);
                    }
                }
            } else { // old style of imports
                // _loadImportTemplate returns 2 objects
                Object ffnct[] = _loadImportTemplate(null, importDef);
                fullFileName = (String) ffnct[0];
                customType = (LinkedHashMap<String, Object>) ffnct[1];
                if (customType != null) {
                    TypeValidation tv = new TypeValidation(customType, importDef);
                    _updateCustomDefs(customType, null);
                }
            }
            _updateNestedToscaTpls(fullFileName, customType);


        }
    }

    /**
     * This method is used to get consolidated custom definitions by passing custom Types from
     * each import. The resultant collection is then passed back which contains all import
     * definitions
     *
     * @param customType      the custom type
     * @param namespacePrefix the namespace prefix
     */
    @SuppressWarnings("unchecked")
    private void _updateCustomDefs(LinkedHashMap<String, Object> customType, String namespacePrefix) {
        LinkedHashMap<String, Object> outerCustomTypes;
        for (String typeDef : typeDefinitionList) {
            if (typeDef.equals("imports")) {
                customDefs.put("imports", customType.get(typeDef));
                if (allCustomDefs.isEmpty() || allCustomDefs.get("imports") == null) {
                    allCustomDefs.put("imports", customType.get(typeDef));
                } else if (customType.get(typeDef) != null) {
                    Set<Object> allCustomImports = new HashSet<>((ArrayList<Object>) allCustomDefs.get("imports"));
                    allCustomImports.addAll((ArrayList<Object>) customType.get(typeDef));
                    allCustomDefs.put("imports", new ArrayList<>(allCustomImports));
                }
            } else {
                outerCustomTypes = (LinkedHashMap<String, Object>) customType.get(typeDef);
                if (outerCustomTypes != null) {
                    if (namespacePrefix != null && !namespacePrefix.isEmpty()) {
                        LinkedHashMap<String, Object> prefixCustomTypes = new LinkedHashMap<String, Object>();
                        for (Map.Entry<String, Object> me : outerCustomTypes.entrySet()) {
                            String typeDefKey = me.getKey();
                            String nameSpacePrefixToKey = namespacePrefix + "." + typeDefKey;
                            prefixCustomTypes.put(nameSpacePrefixToKey, outerCustomTypes.get(typeDefKey));
                        }
                        customDefs.putAll(prefixCustomTypes);
                        allCustomDefs.putAll(prefixCustomTypes);
                    } else {
                        customDefs.putAll(outerCustomTypes);
                        allCustomDefs.putAll(outerCustomTypes);
                    }
                }
            }
        }
    }

    private void _updateNestedToscaTpls(String fullFileName, LinkedHashMap<String, Object> customTpl) {
        if (fullFileName != null && customTpl != null) {
            LinkedHashMap<String, Object> tt = new LinkedHashMap<String, Object>();
            tt.put(fullFileName, customTpl);
            nestedToscaTpls.add(tt);
        }
    }

    private void _validateImportKeys(String importName, LinkedHashMap<String, Object> importUri) {
        if (importUri.get(FILE) == null) {
            //log.warning(_('Missing keyname "file" in import "%(name)s".') % {'name': import_name})
            ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE187", String.format(
                    "MissingRequiredFieldError: Import of template \"%s\" is missing field %s", importName, FILE)));
        }
        for (String key : importUri.keySet()) {
            boolean bFound = false;
            for (String is : IMPORTS_SECTION) {
                if (is.equals(key)) {
                    bFound = true;
                    break;
                }
            }
            if (!bFound) {
                //log.warning(_('Unknown keyname "%(key)s" error in '
                //        'imported definition "%(def)s".')
                //      % {'key': key, 'def': import_name})
                ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE188", String.format(
                        "UnknownFieldError: Import of template \"%s\" has unknown fiels %s", importName, key)));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Object[] _loadImportTemplate(String importName, Object importUriDef) {
    	/*
        This method loads the custom type definitions referenced in "imports"
        section of the TOSCA YAML template by determining whether each import
        is specified via a file reference (by relative or absolute path) or a
        URL reference.

        Possibilities:
        +----------+--------+------------------------------+
        | template | import | comment                      |
        +----------+--------+------------------------------+
        | file     | file   | OK                           |
        | file     | URL    | OK                           |
        | preparsed| file   | file must be a full path     |
        | preparsed| URL    | OK                           |
        | URL      | file   | file must be a relative path |
        | URL      | URL    | OK                           |
        +----------+--------+------------------------------+
    	*/
        Object al[] = new Object[2];

        boolean shortImportNotation = false;
        String fileName;
        String repository;
        if (importUriDef instanceof LinkedHashMap) {
            _validateImportKeys(importName, (LinkedHashMap<String, Object>) importUriDef);
            fileName = (String) ((LinkedHashMap<String, Object>) importUriDef).get(FILE);
            repository = (String) ((LinkedHashMap<String, Object>) importUriDef).get(REPOSITORY);
            if (repository != null) {
                if (!repositories.keySet().contains(repository)) {
                    ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE189", String.format(
                            "InvalidPropertyValueError: Repository \"%s\" not found in \"%s\"",
                            repository, repositories.keySet().toString())));
                }
            }
        } else {
            fileName = (String) importUriDef;
            repository = null;
            shortImportNotation = true;
        }

        if (fileName == null || fileName.isEmpty()) {
            //msg = (_('A template file name is not provided with import '
            //         'definition "%(import_name)s".')
            //       % {'import_name': import_name})
            //log.error(msg)
            ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE190", String.format(
                    "ValidationError: A template file name is not provided with import definition \"%s\"", importName)));
            al[0] = al[1] = null;
            return al;
        }

        if (UrlUtils.validateUrl(fileName)) {
            try (InputStream input = new URL(fileName).openStream();) {
                al[0] = fileName;
                Yaml yaml = new Yaml();
                al[1] = yaml.load(input);
                return al;
            } catch (IOException e) {
                ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE191", String.format(
                        "ImportError: \"%s\" loading YAML import from \"%s\"", e.getClass().getSimpleName(), fileName)));
                al[0] = al[1] = null;
                return al;
            }
        } else if (repository == null || repository.isEmpty()) {
            boolean aFile = false;
            String importTemplate = null;
            if (path != null && !path.isEmpty()) {
                if (UrlUtils.validateUrl(path)) {
                    File fp = new File(path);
                    if (fp.isAbsolute()) {
                        String msg = String.format(
                                "ImportError: Absolute file name \"%s\" cannot be used in the URL-based input template \"%s\"",
                                fileName, path);
                        ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE192", msg));
                        al[0] = al[1] = null;
                        return al;
                    }
                    importTemplate = UrlUtils.joinUrl(path, fileName);
                    aFile = false;
                } else {

                    aFile = true;
                    File fp = new File(path);
                    if (fp.isFile()) {
                        File fn = new File(fileName);
                        if (fn.isFile()) {
                            importTemplate = fileName;
                        } else {
                            String fullPath = Paths.get(path).toAbsolutePath().getParent().toString() + File.separator + fileName;
                            File ffp = new File(fullPath);
                            if (ffp.isFile()) {
                                importTemplate = fullPath;
                            } else {
                                String dirPath = Paths.get(path).toAbsolutePath().getParent().toString();
                                String filePath;
                                if (Paths.get(fileName).getParent() != null) {
                                    filePath = Paths.get(fileName).getParent().toString();
                                } else {
                                    filePath = "";
                                }
                                if (!filePath.isEmpty() && dirPath.endsWith(filePath)) {
                                    String sFileName = Paths.get(fileName).getFileName().toString();
                                    importTemplate = dirPath + File.separator + sFileName;
                                    File fit = new File(importTemplate);
                                    if (!fit.isFile()) {
                                        //msg = (_('"%(import_template)s" is'
                                        //        'not a valid file')
                                        //      % {'import_template':
                                        //         import_template})
                                        //log.error(msg)
                                        String msg = String.format(
                                                "ValueError: \"%s\" is not a valid file", importTemplate);
                                        ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE193", msg));
                                        log.debug("ImportsLoader - _loadImportTemplate - {}", msg);
                                    }
                                }
                            }
                        }
                    }
                }
            } else {  // template is pre-parsed
                File fn = new File(fileName);
                if (fn.isAbsolute() && fn.isFile()) {
                    aFile = true;
                    importTemplate = fileName;
                } else {
                    String msg = String.format(
                            "Relative file name \"%s\" cannot be used in a pre-parsed input template", fileName);
                    ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE194", "ImportError: " + msg));
                    al[0] = al[1] = null;
                    return al;
                }
            }

            if (importTemplate == null || importTemplate.isEmpty()) {
                //log.error(_('Import "%(name)s" is not valid.') %
                //          {'name': import_uri_def})
                ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE195", String.format(
                        "ImportError: Import \"%s\" is not valid", importUriDef)));
                al[0] = al[1] = null;
                return al;
            }

            // for now, this must be a file
            if (!aFile) {
                log.error("ImportsLoader - _loadImportTemplate - Error!! Expected a file. importUriDef = {}, importTemplate = {}", importUriDef, importTemplate);
                ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE196", String.format(
                        "ImportError: Import \"%s\" is not a file", importName)));
                al[0] = al[1] = null;
                return al;
            }
            try (BufferedReader br = new BufferedReader(new FileReader(importTemplate));) {
                al[0] = importTemplate;

                Yaml yaml = new Yaml();
                al[1] = yaml.load(br);
                return al;
            } catch (FileNotFoundException e) {
                ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE197", String.format(
                        "ImportError: Failed to load YAML from \"%s\"" + e, importName)));
                al[0] = al[1] = null;
                return al;
            } catch (Exception e) {
                ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE198", String.format(
                        "ImportError: Exception from SnakeYAML file = \"%s\"" + e, importName)));
                al[0] = al[1] = null;
                return al;
            }
        }

        if (shortImportNotation) {
            //log.error(_('Import "%(name)s" is not valid.') % import_uri_def)
            ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE199", String.format(
                    "ImportError: Import \"%s\" is not valid", importName)));
            al[0] = al[1] = null;
            return al;
        }

        String fullUrl = "";
        String repoUrl = "";
        if (repository != null && !repository.isEmpty()) {
            if (repositories != null) {
                for (String repoName : repositories.keySet()) {
                    if (repoName.equals(repository)) {
                        Object repoDef = repositories.get(repoName);
                        if (repoDef instanceof String) {
                            repoUrl = (String) repoDef;
                        } else if (repoDef instanceof LinkedHashMap) {
                            repoUrl = (String) ((LinkedHashMap<String, Object>) repoDef).get("url");
                        }
                        // Remove leading, ending spaces and strip
                        // the last character if "/"
                        repoUrl = repoUrl.trim();
                        if (repoUrl.endsWith("/")) {
                            repoUrl = repoUrl.substring(0, repoUrl.length() - 1);
                        }
                        fullUrl = repoUrl + "/" + fileName;
                        break;
                    }
                }
            }
            if (fullUrl.isEmpty()) {
                String msg = String.format(
                        "referenced repository \"%s\" in import definition \"%s\" not found",
                        repository, importName);
                ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE200", "ImportError: " + msg));
                al[0] = al[1] = null;
                return al;
            }
        }
        if (UrlUtils.validateUrl(fullUrl)) {
            try (InputStream input = new URL(fullUrl).openStream();) {
                al[0] = fullUrl;
                Yaml yaml = new Yaml();
                al[1] = yaml.load(input);
                return al;
            } catch (IOException e) {
                ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE201", String.format(
                        "ImportError: Exception loading YAML import from \"%s\"", fullUrl)));
                al[0] = al[1] = null;
                return al;
            }
        } else {
            String msg = String.format(
                    "repository URL \"%s\" in import definition \"%s\" is not valid",
                    repoUrl, importName);
            ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE202", "ImportError: " + msg));
        }

        // if we got here something is wrong with the flow...
        log.error("ImportsLoader - _loadImportTemplate - got to dead end (importName {})", importName);
        ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE203", String.format(
                "ImportError: _loadImportTemplate got to dead end (importName %s)\n", importName)));
        al[0] = al[1] = null;
        return al;
    }

    @Override
    public String toString() {
        return "ImportsLoader{" +
                "IMPORTS_SECTION=" + Arrays.toString(IMPORTS_SECTION) +
                ", importslist=" + importslist +
                ", path='" + path + '\'' +
                ", typeDefinitionList=" + typeDefinitionList +
                ", customDefs=" + customDefs +
                ", nestedToscaTpls=" + nestedToscaTpls +
                ", repositories=" + repositories +
                '}';
    }
}

/*python

import logging
import os

from toscaparser.common.exception import ValidationIssueCollector
from toscaparser.common.exception import InvalidPropertyValueError
from toscaparser.common.exception import MissingRequiredFieldError
from toscaparser.common.exception import UnknownFieldError
from toscaparser.common.exception import ValidationError
from toscaparser.elements.tosca_type_validation import TypeValidation
from toscaparser.utils.gettextutils import _
import org.openecomp.sdc.toscaparser.api.utils.urlutils
import org.openecomp.sdc.toscaparser.api.utils.yamlparser

YAML_LOADER = toscaparser.utils.yamlparser.load_yaml
log = logging.getLogger("tosca")


class ImportsLoader(object):

    IMPORTS_SECTION = (FILE, REPOSITORY, NAMESPACE_URI, NAMESPACE_PREFIX) = \
                      ('file', 'repository', 'namespace_uri',
                       'namespace_prefix')

    def __init__(self, importslist, path, type_definition_list=None,
                 tpl=None):
        self.importslist = importslist
        self.custom_defs = {}
        if not path and not tpl:
            msg = _('Input tosca template is not provided.')
            log.warning(msg)
            ValidationIssueCollector.appendException(ValidationError(message=msg))
        self.path = path
        self.repositories = {}
        if tpl and tpl.get('repositories'):
            self.repositories = tpl.get('repositories')
        self.type_definition_list = []
        if type_definition_list:
            if isinstance(type_definition_list, list):
                self.type_definition_list = type_definition_list
            else:
                self.type_definition_list.append(type_definition_list)
        self._validate_and_load_imports()

    def get_custom_defs(self):
        return self.custom_defs

    def _validate_and_load_imports(self):
        imports_names = set()

        if not self.importslist:
            msg = _('"imports" keyname is defined without including '
                    'templates.')
            log.error(msg)
            ValidationIssueCollector.appendException(ValidationError(message=msg))
            return

        for import_def in self.importslist:
            if isinstance(import_def, dict):
                for import_name, import_uri in import_def.items():
                    if import_name in imports_names:
                        msg = (_('Duplicate import name "%s" was found.') %
                               import_name)
                        log.error(msg)
                        ValidationIssueCollector.appendException(
                            ValidationError(message=msg))
                    imports_names.add(import_name)

                    custom_type = self._load_import_template(import_name,
                                                             import_uri)
                    namespace_prefix = None
                    if isinstance(import_uri, dict):
                        namespace_prefix = import_uri.get(
                            self.NAMESPACE_PREFIX)
                    if custom_type:
                        TypeValidation(custom_type, import_def)
                        self._update_custom_def(custom_type, namespace_prefix)
            else:  # old style of imports
                custom_type = self._load_import_template(None,
                                                         import_def)
                if custom_type:
                    TypeValidation(
                        custom_type, import_def)
                    self._update_custom_def(custom_type, None)

    def _update_custom_def(self, custom_type, namespace_prefix):
        outer_custom_types = {}
        for type_def in self.type_definition_list:
            outer_custom_types = custom_type.get(type_def)
            if outer_custom_types:
                if type_def == "imports":
                    self.custom_defs.update({'imports': outer_custom_types})
                else:
                    if namespace_prefix:
                        prefix_custom_types = {}
                        for type_def_key in outer_custom_types.keys():
                            namespace_prefix_to_key = (namespace_prefix +
                                                       "." + type_def_key)
                            prefix_custom_types[namespace_prefix_to_key] = \
                                outer_custom_types[type_def_key]
                        self.custom_defs.update(prefix_custom_types)
                    else:
                        self.custom_defs.update(outer_custom_types)

    def _validate_import_keys(self, import_name, import_uri_def):
        if self.FILE not in import_uri_def.keys():
            log.warning(_('Missing keyname "file" in import "%(name)s".')
                        % {'name': import_name})
            ValidationIssueCollector.appendException(
                MissingRequiredFieldError(
                    what='Import of template "%s"' % import_name,
                    required=self.FILE))
        for key in import_uri_def.keys():
            if key not in self.IMPORTS_SECTION:
                log.warning(_('Unknown keyname "%(key)s" error in '
                              'imported definition "%(def)s".')
                            % {'key': key, 'def': import_name})
                ValidationIssueCollector.appendException(
                    UnknownFieldError(
                        what='Import of template "%s"' % import_name,
                        field=key))

    def _load_import_template(self, import_name, import_uri_def):
        """Handle custom types defined in imported template files

        This method loads the custom type definitions referenced in "imports"
        section of the TOSCA YAML template by determining whether each import
        is specified via a file reference (by relative or absolute path) or a
        URL reference.

        Possibilities:
        +----------+--------+------------------------------+
        | template | import | comment                      |
        +----------+--------+------------------------------+
        | file     | file   | OK                           |
        | file     | URL    | OK                           |
        | preparsed| file   | file must be a full path     |
        | preparsed| URL    | OK                           |
        | URL      | file   | file must be a relative path |
        | URL      | URL    | OK                           |
        +----------+--------+------------------------------+
        """
        short_import_notation = False
        if isinstance(import_uri_def, dict):
            self._validate_import_keys(import_name, import_uri_def)
            file_name = import_uri_def.get(self.FILE)
            repository = import_uri_def.get(self.REPOSITORY)
            repos = self.repositories.keys()
            if repository is not None:
                if repository not in repos:
                    ValidationIssueCollector.appendException(
                        InvalidPropertyValueError(
                            what=_('Repository is not found in "%s"') % repos))
        else:
            file_name = import_uri_def
            repository = None
            short_import_notation = True

        if not file_name:
            msg = (_('A template file name is not provided with import '
                     'definition "%(import_name)s".')
                   % {'import_name': import_name})
            log.error(msg)
            ValidationIssueCollector.appendException(ValidationError(message=msg))
            return

        if toscaparser.utils.urlutils.UrlUtils.validate_url(file_name):
            return YAML_LOADER(file_name, False)
        elif not repository:
            import_template = None
            if self.path:
                if toscaparser.utils.urlutils.UrlUtils.validate_url(self.path):
                    if os.path.isabs(file_name):
                        msg = (_('Absolute file name "%(name)s" cannot be '
                                 'used in a URL-based input template '
                                 '"%(template)s".')
                               % {'name': file_name, 'template': self.path})
                        log.error(msg)
                        ValidationIssueCollector.appendException(ImportError(msg))
                        return
                    import_template = toscaparser.utils.urlutils.UrlUtils.\
                        join_url(self.path, file_name)
                    a_file = False
                else:
                    a_file = True
                    main_a_file = os.path.isfile(self.path)

                    if main_a_file:
                        if os.path.isfile(file_name):
                            import_template = file_name
                        else:
                            full_path = os.path.join(
                                os.path.dirname(os.path.abspath(self.path)),
                                file_name)
                            if os.path.isfile(full_path):
                                import_template = full_path
                            else:
                                file_path = file_name.rpartition("/")
                                dir_path = os.path.dirname(os.path.abspath(
                                    self.path))
                                if file_path[0] != '' and dir_path.endswith(
                                    file_path[0]):
                                        import_template = dir_path + "/" +\
                                            file_path[2]
                                        if not os.path.isfile(import_template):
                                            msg = (_('"%(import_template)s" is'
                                                     'not a valid file')
                                                   % {'import_template':
                                                      import_template})
                                            log.error(msg)
                                            ValidationIssueCollector.appendException
                                            (ValueError(msg))
            else:  # template is pre-parsed
                if os.path.isabs(file_name) and os.path.isfile(file_name):
                    a_file = True
                    import_template = file_name
                else:
                    msg = (_('Relative file name "%(name)s" cannot be used '
                             'in a pre-parsed input template.')
                           % {'name': file_name})
                    log.error(msg)
                    ValidationIssueCollector.appendException(ImportError(msg))
                    return

            if not import_template:
                log.error(_('Import "%(name)s" is not valid.') %
                          {'name': import_uri_def})
                ValidationIssueCollector.appendException(
                    ImportError(_('Import "%s" is not valid.') %
                                import_uri_def))
                return
            return YAML_LOADER(import_template, a_file)

        if short_import_notation:
            log.error(_('Import "%(name)s" is not valid.') % import_uri_def)
            ValidationIssueCollector.appendException(
                ImportError(_('Import "%s" is not valid.') % import_uri_def))
            return

        full_url = ""
        if repository:
            if self.repositories:
                for repo_name, repo_def in self.repositories.items():
                    if repo_name == repository:
                        # Remove leading, ending spaces and strip
                        # the last character if "/"
                        repo_url = ((repo_def['url']).strip()).rstrip("//")
                        full_url = repo_url + "/" + file_name

            if not full_url:
                msg = (_('referenced repository "%(n_uri)s" in import '
                         'definition "%(tpl)s" not found.')
                       % {'n_uri': repository, 'tpl': import_name})
                log.error(msg)
                ValidationIssueCollector.appendException(ImportError(msg))
                return

        if toscaparser.utils.urlutils.UrlUtils.validate_url(full_url):
            return YAML_LOADER(full_url, False)
        else:
            msg = (_('repository url "%(n_uri)s" is not valid in import '
                     'definition "%(tpl)s".')
                   % {'n_uri': repo_url, 'tpl': import_name})
            log.error(msg)
            ValidationIssueCollector.appendException(ImportError(msg))
*/
