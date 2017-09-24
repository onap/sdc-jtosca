package org.openecomp.sdc.toscaparser.api.utils;

import org.openecomp.sdc.toscaparser.api.common.JToscaValidationIssue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TOSCAVersionProperty {// test with functions/test_concat.yaml
	
	private String version;
	
	private static final String versionRe =
		    "^(?<gMajorVersion>([0-9][0-9]*))" +
            "(\\.(?<gMinorVersion>([0-9][0-9]*)))?" +
            "(\\.(?<gFixVersion>([0-9][0-9]*)))?" +
            "(\\.(?<gQualifier>([0-9A-Za-z]+)))?" +
            "(\\-(?<gBuildVersion>[0-9])*)?$";

	private String minorVersion = null;
	private String majorVersion = null;
	private String fixVersion = null;
	private String qualifier = null;
	private String buildVersion = null;

	
	public TOSCAVersionProperty(Object _version) {
		version = _version.toString();

        if(version.equals("0") || version.equals("0.0") || version.equals("0.0.0")) {
            //log.warning(_('Version assumed as not provided'))
            version = "";
            return;
        }

		Pattern pattern = Pattern.compile(versionRe);
		Matcher matcher = pattern.matcher(version);
		if(!matcher.find()) {
			ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE252", String.format(
                "InvalidTOSCAVersionPropertyException: " +
                "Value of TOSCA version property \"%s\" is invalid",
                version))); 
            return;
		}
        minorVersion = matcher.group("gMinorVersion");
        majorVersion = matcher.group("gMajorVersion");
        fixVersion = matcher.group("gFixVersion");
        qualifier = _validateQualifier(matcher.group("gQualifier"));
        buildVersion = _validateBuild(matcher.group("gBuildVersion"));
        _validateMajorVersion(majorVersion);
	
	}
	
	private String _validateMajorVersion(String value) {
        // Validate major version

        // Checks if only major version is provided and assumes
        // minor version as 0.
        // Eg: If version = 18, then it returns version = '18.0'

        if(minorVersion == null && buildVersion == null && !value.equals("0")) {
            //log.warning(_('Minor version assumed "0".'))
            version = version + "0";
        }
        return value;
	}

	private String _validateQualifier(String value) {
	    // Validate qualifier
	
	    // TOSCA version is invalid if a qualifier is present without the
	    // fix version or with all of major, minor and fix version 0s.
	
	    // For example, the following versions are invalid
	    //    18.0.abc
	    //    0.0.0.abc
		
		if((fixVersion == null && value != null) ||
		   (minorVersion.equals("0") && majorVersion.equals("0") && 
		      fixVersion.equals("0") &&  value != null)) {
			ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE253", String.format(
	                "InvalidTOSCAVersionPropertyException: " +
	                "Value of TOSCA version property \"%s\" is invalid",
	                version))); 
		}
		return value;
	}

    private String _validateBuild(String value) {
        // Validate build version

        // TOSCA version is invalid if build version is present without the qualifier.
        // Eg: version = 18.0.0-1 is invalid.

    	if(qualifier == null && value != null) {
			ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE254", String.format(
      	                "InvalidTOSCAVersionPropertyException: " +
       	                "Value of TOSCA version property \"%s\" is invalid",
       	                version))); 
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