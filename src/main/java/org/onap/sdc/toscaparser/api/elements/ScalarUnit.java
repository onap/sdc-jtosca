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

import org.onap.sdc.toscaparser.api.common.JToscaValidationIssue;
import org.onap.sdc.toscaparser.api.utils.ThreadLocalsHolder;
import org.onap.sdc.toscaparser.api.utils.ValidateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class ScalarUnit {

    private static Logger log = LoggerFactory.getLogger(ScalarUnit.class.getName());

    private static final String SCALAR_UNIT_SIZE = "scalar-unit.size";
    private static final String SCALAR_UNIT_FREQUENCY = "scalar-unit.frequency";
    private static final String SCALAR_UNIT_TIME = "scalar-unit.time";

    public static final String[] SCALAR_UNIT_TYPES = {
            SCALAR_UNIT_SIZE, SCALAR_UNIT_FREQUENCY, SCALAR_UNIT_TIME
    };

    private Object value;
    private HashMap<String, Object> scalarUnitDict;
    private String scalarUnitDefault;

    public ScalarUnit(Object value) {
        this.value = value;
        scalarUnitDict = new HashMap<>();
        scalarUnitDefault = "";
    }

    void putToScalarUnitDict(String key, Object value) {
        scalarUnitDict.put(key, value);
    }

    void setScalarUnitDefault(String scalarUnitDefault) {
        this.scalarUnitDefault = scalarUnitDefault;
    }

    private String checkUnitInScalarStandardUnits(String inputUnit) {
        // Check whether the input unit is following specified standard

        // If unit is not following specified standard, convert it to standard
        // unit after displaying a warning message.

        if (scalarUnitDict.get(inputUnit) != null) {
            return inputUnit;
        } else {
            for (String key : scalarUnitDict.keySet()) {
                if (key.toUpperCase().equals(inputUnit.toUpperCase())) {
                    log.debug("ScalarUnit - checkUnitInScalarStandardUnits - \n"
                                    + "The unit {} does not follow scalar unit standards\n"
                                    + "using {} instead",
                            inputUnit, key);
                    return key;
                }
            }
            ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE007", String.format(
                    "'The unit \"%s\" is not valid. Valid units are \n%s",
                    inputUnit, scalarUnitDict.keySet().toString())));
            return inputUnit;
        }
    }

    public Object validateScalarUnit() {
        Pattern pattern = Pattern.compile("([0-9.]+)\\s*(\\w+)");
        Matcher matcher = pattern.matcher(value.toString());
        if (matcher.find()) {
            ValidateUtils.strToNum(matcher.group(1));
            String scalarUnit = checkUnitInScalarStandardUnits(matcher.group(2));
            value = matcher.group(1) + " " + scalarUnit;
        } else {
            ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE134", String.format(
                    "ValueError: \"%s\" is not a valid scalar-unit", value.toString())));
        }
        return value;
    }

    public double getNumFromScalarUnit(String unit) {
        if (unit != null) {
            unit = checkUnitInScalarStandardUnits(unit);
        } else {
            unit = scalarUnitDefault;
        }
        Pattern pattern = Pattern.compile("([0-9.]+)\\s*(\\w+)");
        Matcher matcher = pattern.matcher(value.toString());
        if (matcher.find()) {
            final double minimalNum = 0.0000000000001;

            ValidateUtils.strToNum(matcher.group(1));
            String scalarUnit = checkUnitInScalarStandardUnits(matcher.group(2));
            value = matcher.group(1) + " " + scalarUnit;
            Object on1 = ValidateUtils.strToNum(matcher.group(1)) != null ? ValidateUtils.strToNum(matcher.group(1)) : 0;
            Object on2 = scalarUnitDict.get(matcher.group(2)) != null ? scalarUnitDict.get(matcher.group(2)) : 0;
            Object on3 = scalarUnitDict.get(unit) != null ? scalarUnitDict.get(unit) : 0;

            Double n1 = new Double(on1.toString());
            Double n2 = new Double(on2.toString());
            Double n3 = new Double(on3.toString());
            double converted = n1 * n2 / n3;

            if (Math.abs(converted - Math.round(converted)) < minimalNum) {
                converted = Math.round(converted);
            }
            return converted;
        }
        return 0.0;
    }

    private static HashMap<String, String> scalarUnitMapping = getScalarUnitMappings();

    private static HashMap<String, String> getScalarUnitMappings() {
        HashMap<String, String> map = new HashMap<>();
        map.put(SCALAR_UNIT_FREQUENCY, "ScalarUnitFrequency");
        map.put(SCALAR_UNIT_SIZE, "ScalarUnitSize");
        map.put(SCALAR_UNIT_TIME, "ScalarUnit_Time");
        return map;
    }

    public static ScalarUnit getScalarunitClass(String type, Object val) {
        if (type.equals(SCALAR_UNIT_SIZE)) {
            return new ScalarUnitSize(val);
        } else if (type.equals(SCALAR_UNIT_TIME)) {
            return new ScalarUnitTime(val);
        } else if (type.equals(SCALAR_UNIT_FREQUENCY)) {
            return new ScalarUnitFrequency(val);
        }
        return null;
    }

    public static double getScalarunitValue(String type, Object value, String unit) {
        if (type.equals(SCALAR_UNIT_SIZE)) {
            return (new ScalarUnitSize(value)).getNumFromScalarUnit(unit);
        }
        if (type.equals(SCALAR_UNIT_TIME)) {
            return (new ScalarUnitTime(value)).getNumFromScalarUnit(unit);
        }
        if (type.equals(SCALAR_UNIT_FREQUENCY)) {
            return (new ScalarUnitFrequency(value)).getNumFromScalarUnit(unit);
        }
        ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE135", String.format(
                "TypeError: \"%s\" is not a valid scalar-unit type", type)));
        return 0.0;
    }

}

/*python

from toscaparser.common.exception import ValidationIssueCollector
from toscaparser.utils.gettextutils import _
from toscaparser.utils import validateutils

log = logging.getLogger('tosca')


class ScalarUnit(object):
    '''Parent class for scalar-unit type.'''

    SCALAR_UNIT_TYPES = (
        SCALAR_UNIT_SIZE, SCALAR_UNIT_FREQUENCY, SCALAR_UNIT_TIME
    ) = (
        'scalar-unit.size', 'scalar-unit.frequency', 'scalar-unit.time'
    )

    def __init__(self, value):
        self.value = value

    def _check_unit_in_scalar_standard_units(self, input_unit):
        """Check whether the input unit is following specified standard

        If unit is not following specified standard, convert it to standard
        unit after displaying a warning message.
        """
        if input_unit in self.scalarUnitDict.keys():
            return input_unit
        else:
            for key in self.scalarUnitDict.keys():
                if key.upper() == input_unit.upper():
                    log.warning(_('The unit "%(unit)s" does not follow '
                                  'scalar unit standards; using "%(key)s" '
                                  'instead.') % {'unit': input_unit,
                                                 'key': key})
                    return key
            msg = (_('The unit "%(unit)s" is not valid. Valid units are '
                     '"%(valid_units)s".') %
                   {'unit': input_unit,
                    'valid_units': sorted(self.scalarUnitDict.keys())})
            ValidationIssueCollector.appendException(ValueError(msg))

    def validate_scalar_unit(self):
        regex = re.compile('([0-9.]+)\s*(\w+)')
        try:
            result = regex.match(str(self.value)).groups()
            validateutils.str_to_num(result[0])
            scalar_unit = self._check_unit_in_scalar_standard_units(result[1])
            self.value = ' '.join([result[0], scalar_unit])
            return self.value

        except Exception:
            ValidationIssueCollector.appendException(
                ValueError(_('"%s" is not a valid scalar-unit.')
                           % self.value))

    def get_num_from_scalar_unit(self, unit=None):
        if unit:
            unit = self._check_unit_in_scalar_standard_units(unit)
        else:
            unit = self.scalarUnitDefault
        self.validate_scalar_unit()

        regex = re.compile('([0-9.]+)\s*(\w+)')
        result = regex.match(str(self.value)).groups()
        converted = (float(validateutils.str_to_num(result[0]))
                     * self.scalarUnitDict[result[1]]
                     / self.scalarUnitDict[unit])
        if converted - int(converted) < 0.0000000000001:
            converted = int(converted)
        return converted


class ScalarUnit_Size(ScalarUnit):

    scalarUnitDefault = 'B'
    scalarUnitDict = {'B': 1, 'kB': 1000, 'KiB': 1024, 'MB': 1000000,
                        'MiB': 1048576, 'GB': 1000000000,
                        'GiB': 1073741824, 'TB': 1000000000000,
                        'TiB': 1099511627776}


class ScalarUnit_Time(ScalarUnit):

    scalarUnitDefault = 'ms'
    scalarUnitDict = {'d': 86400, 'h': 3600, 'm': 60, 's': 1,
                        'ms': 0.001, 'us': 0.000001, 'ns': 0.000000001}


class ScalarUnit_Frequency(ScalarUnit):

    scalarUnitDefault = 'GHz'
    scalarUnitDict = {'Hz': 1, 'kHz': 1000,
                        'MHz': 1000000, 'GHz': 1000000000}


scalarunit_mapping = {
    ScalarUnit.SCALAR_UNIT_FREQUENCY: ScalarUnit_Frequency,
    ScalarUnit.SCALAR_UNIT_SIZE: ScalarUnit_Size,
    ScalarUnit.SCALAR_UNIT_TIME: ScalarUnit_Time,
    }


def get_scalarunit_class(type):
    return scalarunit_mapping.get(type)


def get_scalarunit_value(type, value, unit=None):
    if type in ScalarUnit.SCALAR_UNIT_TYPES:
        ScalarUnit_Class = get_scalarunit_class(type)
        return (ScalarUnit_Class(value).
                get_num_from_scalar_unit(unit))
    else:
        ValidationIssueCollector.appendException(
            TypeError(_('"%s" is not a valid scalar-unit type.') % type))
*/
