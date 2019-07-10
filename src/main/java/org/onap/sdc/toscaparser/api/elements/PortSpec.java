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

package org.onap.sdc.toscaparser.api.elements;

import org.onap.sdc.toscaparser.api.DataEntity;
import org.onap.sdc.toscaparser.api.common.JToscaValidationIssue;
import org.onap.sdc.toscaparser.api.utils.ThreadLocalsHolder;
import org.onap.sdc.toscaparser.api.utils.ValidateUtils;

import java.util.LinkedHashMap;

public class PortSpec {
    // Parent class for tosca.datatypes.network.PortSpec type

    private static final String SHORTNAME = "PortSpec";
    private static final String TYPE_URI = "tosca.datatypes.network." + SHORTNAME;

    private static final String PROTOCOL = "protocol";
    private static final String SOURCE = "source";
    private static final String SOURCE_RANGE = "source_range";
    private static final String TARGET = "target";
    private static final String TARGET_RANGE = "target_range";

    private static final String PROPERTY_NAMES[] = {
            PROTOCOL, SOURCE, SOURCE_RANGE,
            TARGET, TARGET_RANGE
    };

    // todo(TBD) May want to make this a subclass of DataType
    // and change init method to set PortSpec's properties
    public PortSpec() {

    }

    // The following additional requirements MUST be tested:
    // 1) A valid PortSpec MUST have at least one of the following properties:
    //   target, target_range, source or source_range.
    // 2) A valid PortSpec MUST have a value for the source property that
    //    is within the numeric range specified by the property source_range
    //    when source_range is specified.
    // 3) A valid PortSpec MUST have a value for the target property that is
    //    within the numeric range specified by the property target_range
    //    when target_range is specified.
    public static void validateAdditionalReq(Object _properties,
                                             String propName,
                                             LinkedHashMap<String, Object> custom_def) {

        try {
            LinkedHashMap<String, Object> properties = (LinkedHashMap<String, Object>) _properties;
            Object source = properties.get(PortSpec.SOURCE);
            Object sourceRange = properties.get(PortSpec.SOURCE_RANGE);
            Object target = properties.get(PortSpec.TARGET);
            Object targetRange = properties.get(PortSpec.TARGET_RANGE);

            // verify one of the specified values is set
            if (source == null && sourceRange == null &&
                    target == null && targetRange == null) {
                ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE129", String.format(
                        "InvalidTypeAdditionalRequirementsError: Additional requirements for type \"%s\" not met",
                        TYPE_URI)));
            }
            // Validate source value is in specified range
            if (source != null && sourceRange != null) {
                ValidateUtils.validateValueInRange(source, sourceRange, SOURCE);
            } else {
                DataEntity portdef = new DataEntity("PortDef", source, null, SOURCE);
                portdef.validate();
            }
            // Validate target value is in specified range
            if (target != null && targetRange != null) {
                ValidateUtils.validateValueInRange(target, targetRange, SOURCE);
            } else {
                DataEntity portdef = new DataEntity("PortDef", source, null, TARGET);
                portdef.validate();
            }
        } catch (Exception e) {
            ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE130", String.format(
                    "ValueError: \"%s\" do not meet requirements for type \"%s\"",
                    _properties.toString(), SHORTNAME)));
        }
    }

}

/*python

from toscaparser.common.exception import ValidationIssueCollector
from toscaparser.common.exception import InvalidTypeAdditionalRequirementsError
from toscaparser.utils.gettextutils import _
import org.openecomp.sdc.toscaparser.api.utils.validateutils as validateutils

log = logging.getLogger('tosca')


class PortSpec(object):
    '''Parent class for tosca.datatypes.network.PortSpec type.'''

    SHORTNAME = 'PortSpec'
    TYPE_URI = 'tosca.datatypes.network.' + SHORTNAME

    PROPERTY_NAMES = (
        PROTOCOL, SOURCE, SOURCE_RANGE,
        TARGET, TARGET_RANGE
    ) = (
        'protocol', 'source', 'source_range',
        'target', 'target_range'
    )

    # TODO(TBD) May want to make this a subclass of DataType
    # and change init method to set PortSpec's properties
    def __init__(self):
        pass

    # The following additional requirements MUST be tested:
    # 1) A valid PortSpec MUST have at least one of the following properties:
    #   target, target_range, source or source_range.
    # 2) A valid PortSpec MUST have a value for the source property that
    #    is within the numeric range specified by the property source_range
    #    when source_range is specified.
    # 3) A valid PortSpec MUST have a value for the target property that is
    #    within the numeric range specified by the property target_range
    #    when target_range is specified.
    @staticmethod
    def validate_additional_req(properties, prop_name, custom_def=None, ):
        try:
            source = properties.get(PortSpec.SOURCE)
            source_range = properties.get(PortSpec.SOURCE_RANGE)
            target = properties.get(PortSpec.TARGET)
            target_range = properties.get(PortSpec.TARGET_RANGE)

            # verify one of the specified values is set
            if source is None and source_range is None and \
                    target is None and target_range is None:
                ValidationIssueCollector.appendException(
                    InvalidTypeAdditionalRequirementsError(
                        type=PortSpec.TYPE_URI))
            # Validate source value is in specified range
            if source and source_range:
                validateutils.validate_value_in_range(source, source_range,
                                                      PortSpec.SOURCE)
            else:
                from toscaparser.dataentity import DataEntity
                portdef = DataEntity('PortDef', source, None, PortSpec.SOURCE)
                portdef.validate()
            # Validate target value is in specified range
            if target and target_range:
                validateutils.validate_value_in_range(target, target_range,
                                                      PortSpec.TARGET)
            else:
                from toscaparser.dataentity import DataEntity
                portdef = DataEntity('PortDef', source, None, PortSpec.TARGET)
                portdef.validate()
        except Exception:
            msg = _('"%(value)s" do not meet requirements '
                    'for type "%(type)s".') \
                % {'value': properties, 'type': PortSpec.SHORTNAME}
            ValidationIssueCollector.appendException(
                ValueError(msg))
*/
