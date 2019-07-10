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

package org.onap.sdc.toscaparser.api.utils;

import org.onap.sdc.toscaparser.api.common.JToscaValidationIssue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

// test with functions/test_concat.yaml
public class TOSCAVersionProperty {

    private String version;

    private static final String VERSION_RE =
            "^(?<gMajorVersion>([0-9][0-9]*))"
                    + "(\\.(?<gMinorVersion>([0-9][0-9]*)))?"
                    + "(\\.(?<gFixVersion>([0-9][0-9]*)))?"
                    + "(\\.(?<gQualifier>([0-9A-Za-z]+)))?"
                    + "(\\-(?<gBuildVersion>[0-9])*)?$";

    private String minorVersion = null;
    private String majorVersion = null;
    private String fixVersion = null;
    private String qualifier = null;
    private String buildVersion = null;


    public TOSCAVersionProperty(String version) {

        if (version.equals("0") || version.equals("0.0") || version.equals("0.0.0")) {
            return;
        }

        Pattern pattern = Pattern.compile(VERSION_RE);
        Matcher matcher = pattern.matcher(version);
        if (!matcher.find()) {
            ThreadLocalsHolder.getCollector().appendValidationIssue(
                    new JToscaValidationIssue(
                            "JE252",
                            "InvalidTOSCAVersionPropertyException: "
                                    + "Value of TOSCA version property \"" + version + "\" is invalid"
                    ));
            return;
        }
        minorVersion = matcher.group("gMinorVersion");
        majorVersion = matcher.group("gMajorVersion");
        fixVersion = matcher.group("gFixVersion");
        qualifier = validateQualifier(matcher.group("gQualifier"));
        buildVersion = validateBuild(matcher.group("gBuildVersion"));
        validateMajorVersion(majorVersion);

        this.version = version;

    }

    private String validateMajorVersion(String value) {
        // Validate major version

        // Checks if only major version is provided and assumes
        // minor version as 0.
        // Eg: If version = 18, then it returns version = '18.0'

        if (minorVersion == null && buildVersion == null && !value.equals("0")) {
            //log.warning(_('Minor version assumed "0".'))
            version = version + "0";
        }
        return value;
    }

    private String validateQualifier(String value) {
        // Validate qualifier

        // TOSCA version is invalid if a qualifier is present without the
        // fix version or with all of major, minor and fix version 0s.

        // For example, the following versions are invalid
        //    18.0.abc
        //    0.0.0.abc

        if ((fixVersion == null && value != null) || (minorVersion.equals("0") && majorVersion.equals("0")
                && fixVersion.equals("0") && value != null)) {
            ThreadLocalsHolder.getCollector().appendValidationIssue(
                    new JToscaValidationIssue(
                            "JE253",
                            "InvalidTOSCAVersionPropertyException: Value of TOSCA version property \""
                                    + version
                                    + "\" is invalid"
                    ));
        }
        return value;
    }

    private String validateBuild(String value) {
        // Validate build version

        // TOSCA version is invalid if build version is present without the qualifier.
        // Eg: version = 18.0.0-1 is invalid.

        if (qualifier == null && value != null) {
            ThreadLocalsHolder.getCollector().appendValidationIssue(
                    new JToscaValidationIssue(
                            "JE254",
                            "InvalidTOSCAVersionPropertyException: "
                                    + "Value of TOSCA version property \"" + version + "\" is invalid"
                    )
            );
        }
        return value;
    }

    public Object getVersion() {
        return version;
    }

}

/*python

class TOSCAVersionProperty(object):

    VERSION_RE = re.compile('^(?P<major_version>([0-9][0-9]*))'
                            '(\.(?P<minor_version>([0-9][0-9]*)))?'
                            '(\.(?P<fix_version>([0-9][0-9]*)))?'
                            '(\.(?P<qualifier>([0-9A-Za-z]+)))?'
                            '(\-(?P<build_version>[0-9])*)?$')

    def __init__(self, version):
        self.version = str(version)
        match = self.VERSION_RE.match(self.version)
        if not match:
            ValidationIssueCollector.appendException(
                InvalidTOSCAVersionPropertyException(what=(self.version)))
            return
        ver = match.groupdict()
        if self.version in ['0', '0.0', '0.0.0']:
            log.warning(_('Version assumed as not provided'))
            self.version = None
        self.minor_version = ver['minor_version']
        self.major_version = ver['major_version']
        self.fix_version = ver['fix_version']
        self.qualifier = self._validate_qualifier(ver['qualifier'])
        self.build_version = self._validate_build(ver['build_version'])
        self._validate_major_version(self.major_version)

    def _validate_major_version(self, value):
        """Validate major version

        Checks if only major version is provided and assumes
        minor version as 0.
        Eg: If version = 18, then it returns version = '18.0'
        """

        if self.minor_version is None and self.build_version is None and \
                value != '0':
            log.warning(_('Minor version assumed "0".'))
            self.version = '.'.join([value, '0'])
        return value

    def _validate_qualifier(self, value):
        """Validate qualifier

           TOSCA version is invalid if a qualifier is present without the
           fix version or with all of major, minor and fix version 0s.

           For example, the following versions are invalid
              18.0.abc
              0.0.0.abc
        """
        if (self.fix_version is None and value) or \
            (self.minor_version == self.major_version ==
             self.fix_version == '0' and value):
            ValidationIssueCollector.appendException(
                InvalidTOSCAVersionPropertyException(what=(self.version)))
        return value

    def _validate_build(self, value):
        """Validate build version

           TOSCA version is invalid if build version is present without the
           qualifier.
           Eg: version = 18.0.0-1 is invalid.
        """
        if not self.qualifier and value:
            ValidationIssueCollector.appendException(
                InvalidTOSCAVersionPropertyException(what=(self.version)))
        return value

    def get_version(self):
        return self.version
*/
