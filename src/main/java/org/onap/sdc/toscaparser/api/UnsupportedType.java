package org.onap.sdc.toscaparser.api;

import org.onap.sdc.toscaparser.api.common.JToscaValidationIssue;
import org.onap.sdc.toscaparser.api.utils.ThreadLocalsHolder;

public class UnsupportedType {

    // Note: TOSCA spec version related

	/*
    The tosca.nodes.Storage.ObjectStorage and tosca.nodes.Storage.BlockStorage
    used here as un_supported_types are part of the name changes in TOSCA spec
    version 1.1. The original name as specified in version 1.0 are,
    tosca.nodes.BlockStorage and tosca.nodes.ObjectStorage which are supported
    by the tosca-parser. Since there are little overlapping in version support
    currently in the tosca-parser, the names tosca.nodes.Storage.ObjectStorage
    and tosca.nodes.Storage.BlockStorage are used here to demonstrate the usage
    of un_supported_types. As tosca-parser move to provide support for version
    1.1 and higher, they will be removed.
    */
	
    private static final String unsupportedTypes[] = {
    									"tosca.test.invalidtype",
    									"tosca.nodes.Storage.ObjectStorage",
    									"tosca.nodes.Storage.BlockStorage"};

    public static boolean validateType(String entityType) {
    	for(String ust: unsupportedTypes) {
    		if(ust.equals(entityType)) {
                ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE251", String.format(
                		"UnsupportedTypeError: Entity type \"%s\" is not supported",entityType))); 
    			return true;
    		}
    	}
        return false;
    }
}

/*python

from toscaparser.common.exception import ValidationIssueCollector
from toscaparser.common.exception import UnsupportedTypeError
from toscaparser.utils.gettextutils import _

log = logging.getLogger('tosca')


class UnsupportedType(object):

    """Note: TOSCA spec version related

    The tosca.nodes.Storage.ObjectStorage and tosca.nodes.Storage.BlockStorage
    used here as un_supported_types are part of the name changes in TOSCA spec
    version 1.1. The original name as specified in version 1.0 are,
    tosca.nodes.BlockStorage and tosca.nodes.ObjectStorage which are supported
    by the tosca-parser. Since there are little overlapping in version support
    currently in the tosca-parser, the names tosca.nodes.Storage.ObjectStorage
    and tosca.nodes.Storage.BlockStorage are used here to demonstrate the usage
    of un_supported_types. As tosca-parser move to provide support for version
    1.1 and higher, they will be removed.
    """
    un_supported_types = ['tosca.test.invalidtype',
                          'tosca.nodes.Storage.ObjectStorage',
                          'tosca.nodes.Storage.BlockStorage']

    def __init__(self):
        pass

    @staticmethod
    def validate_type(entitytype):
        if entitytype in UnsupportedType.un_supported_types:
            ValidationIssueCollector.appendException(UnsupportedTypeError(
                                               what=_('%s')
                                               % entitytype))
            return True
        else:
            return False
*/