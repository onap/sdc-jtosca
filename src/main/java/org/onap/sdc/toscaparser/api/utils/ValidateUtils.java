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

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;

public class ValidateUtils {

    private static final String RANGE_UNBOUNDED = "UNBOUNDED";

    private ValidateUtils() {
    }

    public static Object strToNum(Object value) {
        // Convert a string representation of a number into a numeric type
        // TODO(TBD) we should not allow numeric values in, input should be str
        if (value instanceof Number) {
            return value;
        }
        try {
            return Integer.parseInt((String) value);
        } catch (NumberFormatException e) {
        }
        try {
            return Float.parseFloat((String) value);
        } catch (Exception e) {
        }
        return null;
    }

    public static Object validateNumeric(Object value) {
        if (value != null) {
            if (!(value instanceof Number)) {
                ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE257", String.format(
                        "ValueError: \"%s\" is not a numeric", value.toString())));
            }
        }
        return value;
    }

    public static Object validateInteger(Object value) {
        if (value != null) {
            if (!(value instanceof Integer)) {
                // allow "true" and "false"
                if (value instanceof Boolean) {
                    return (Boolean) value ? 1 : 0;
                }
                ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE258", String.format(
                        "ValueError: \"%s\" is not an integer", value.toString())));
            }
        }
        return value;
    }

    public static Object validateFloat(Object value) {
        if (value != null) {
            if (!(value instanceof Float || value instanceof Double)) {
                ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE259", String.format(
                        "ValueError: \"%s\" is not a float", value.toString())));
            }
        }
        return value;
    }

    public static Object validateString(Object value) {
        if (value != null) {
            if (!(value instanceof String)) {
                ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE260", String.format(
                        "ValueError: \'%s\' is not a string", value.toString())));
            }
        }
        return value;
    }

    public static Object validateList(Object value) {
        if (value != null) {
            if (!(value instanceof ArrayList)) {
                ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE261", String.format(
                        "ValueError: \"%s\" is not a list", value.toString())));
            }
        }
        return value;
    }


    @SuppressWarnings("unchecked")
    public static Object validateRange(Object range) {
        // list class check
        validateList(range);
        // validate range list has a min and max
        if (range instanceof ArrayList && ((ArrayList<Object>) range).size() != 2) {
            ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE262", String.format(
                    "ValueError: \"%s\" is not a valid range", range.toString())));
            // too dangerous to continue...
            return range;
        }
        // validate min and max are numerics or the keyword UNBOUNDED
        boolean minTest = false;
        boolean maxTest = false;
        Object r0 = ((ArrayList<Object>) range).get(0);
        Object r1 = ((ArrayList<Object>) range).get(1);

        if (!(r0 instanceof Integer) && !(r0 instanceof Float)
                || !(r1 instanceof Integer) && !(r1 instanceof Float)) {
            ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE263", String.format(
                    "ValueError: \"%s\" is not a valid range", range.toString())));
            // too dangerous to continue...
            return range;
        }

        Float min = 0.0F;
        Float max = 0.0F;
        if (r0 instanceof String && ((String) r0).equals(RANGE_UNBOUNDED)) {
            minTest = true;
        } else {
            min = r0 instanceof Integer ? ((Integer) r0).floatValue() : (Float) r0;
        }
        if (r1 instanceof String && ((String) r1).equals(RANGE_UNBOUNDED)) {
            maxTest = true;
        } else {
            max = r1 instanceof Integer ? ((Integer) r1).floatValue() : (Float) r1;
        }

        // validate the max > min (account for UNBOUNDED)
        if (!minTest && !maxTest) {
            // Note: min == max is allowed
            if (min > max) {
                ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE264", String.format(
                        "ValueError:\"%s\" is not a valid range", range.toString())));
            }
        }
        return range;
    }

    @SuppressWarnings("unchecked")
    public static Object validateValueInRange(Object value, Object range, String propName) {
        // verify all 3 are numeric and convert to Floats
        if (!(value instanceof Integer || value instanceof Float)) {
            ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE265", String.format(
                    "ValueError: validateInRange: \"%s\" is not a number", range.toString())));
            return value;
        }
        Float fval = value instanceof Integer ? ((Integer) value).floatValue() : (Float) value;

        //////////////////////////
        //"validateRange(range);"
        //////////////////////////
        // better safe than sorry...
        // validate that range list has a min and max
        if (range instanceof ArrayList && ((ArrayList<Object>) range).size() != 2) {
            ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE266", String.format(
                    "ValueError: \"%s\" is not a valid range", range.toString())));
            // too dangerous to continue...
            return value;
        }
        // validate min and max are numerics or the keyword UNBOUNDED
        boolean minTest = false;
        boolean maxTest = false;
        Object r0 = ((ArrayList<Object>) range).get(0);
        Object r1 = ((ArrayList<Object>) range).get(1);

        if (!(r0 instanceof Integer) && !(r0 instanceof Float)
                || !(r1 instanceof Integer) && !(r1 instanceof Float)) {
            ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE267", String.format(
                    "ValueError: \"%s\" is not a valid range", range.toString())));
            // too dangerous to continue...
            return value;
        }

        Float min = 0.0F;
        Float max = 0.0F;
        if (r0 instanceof String && ((String) r0).equals(RANGE_UNBOUNDED)) {
            minTest = true;
        } else {
            min = r0 instanceof Integer ? ((Integer) r0).floatValue() : (Float) r0;
        }
        if (r1 instanceof String && ((String) r1).equals(RANGE_UNBOUNDED)) {
            maxTest = true;
        } else {
            max = r1 instanceof Integer ? ((Integer) r1).floatValue() : (Float) r1;
        }

        // validate the max > min (account for UNBOUNDED)
        if (!minTest && !maxTest) {
            // Note: min == max is allowed
            if (min > max) {
                ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE268", String.format(
                        "ValueError:\"%s\" is not a valid range", range.toString())));
            }
        }
        // finally...
        boolean bError = false;
        //Note: value is valid if equal to min
        if (!minTest) {
            if (fval < min) {
                bError = true;
            }
        }
        // Note: value is valid if equal to max
        if (!maxTest) {
            if (fval > max) {
                bError = true;
            }
        }
        if (bError) {
            ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE269", String.format(
                    "RangeValueError: Property \"%s\", \"%s\" not in range [\"%s\" - \"%s\"",
                    propName, value.toString(), r0.toString(), r1.toString())));
        }
        return value;
    }

    public static Object validateMap(Object ob) {
        if (ob != null) {
            if (!(ob instanceof LinkedHashMap)) {
                ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE270", String.format(
                        "ValueError\"%s\" is not a map.", ob.toString())));
            }
        }
        return ob;
    }

    public static Object validateBoolean(Object value) {
        if (value != null) {
            if (value instanceof Boolean) {
                return value;
            }
            if (value instanceof String) {
                String normalized = ((String) value).toLowerCase();
                if (normalized.equals("true") || normalized.equals("false")) {
                    return normalized.equals("true");
                }
            }
            ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE271", String.format(
                    "ValueError: \"%s\" is not a boolean", value.toString())));
        }
        return value;
    }

    public static Object validateTimestamp(Object value) {

        /*
	    try:
	        # Note: we must return our own exception message
	        # as dateutil's parser returns different types / values on
	        # different systems. OSX, for example, returns a tuple
	        # containing a different error message than Linux
	        dateutil.parser.parse(value)
	    except Exception as e:
	        original_err_msg = str(e)
	        log.error(original_err_msg)
	        ValidationIssueCollector.appendException(
	            ValueError(_('"%(val)s" is not a valid timestamp. "%(msg)s"') %
	                       {'val': value, 'msg': original_err_msg}))
		*/
        // timestamps are loaded as Date objects by the YAML parser
        if (value != null) {
            if (!(value instanceof Date)) {
                ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE272", String.format(
                        "ValueError: \"%s\" is not a valid timestamp",
                        value.toString())));

            }
        }
        return value;
    }

}

/*python

from toscaparser.elements import constraints
from toscaparser.common.exception import ValidationIssueCollector
from toscaparser.common.exception import InvalidTOSCAVersionPropertyException
from toscaparser.common.exception import RangeValueError
from toscaparser.utils.gettextutils import _

log = logging.getLogger('tosca')

RANGE_UNBOUNDED = 'UNBOUNDED'


def str_to_num(value):
    '''Convert a string representation of a number into a numeric type.'''
    # tODO(TBD) we should not allow numeric values in, input should be str
    if isinstance(value, numbers.Number):
        return value
    try:
        return int(value)
    except ValueError:
        return float(value)


def validate_numeric(value):
    if not isinstance(value, numbers.Number):
        ValidationIssueCollector.appendException(
            ValueError(_('"%s" is not a numeric.') % value))
    return value


def validate_integer(value):
    if not isinstance(value, int):
        try:
            value = int(value)
        except Exception:
            ValidationIssueCollector.appendException(
                ValueError(_('"%s" is not an integer.') % value))
    return value


def validate_float(value):
    if not isinstance(value, float):
        ValidationIssueCollector.appendException(
            ValueError(_('"%s" is not a float.') % value))
    return value


def validate_string(value):
    if not isinstance(value, six.string_types):
        ValidationIssueCollector.appendException(
            ValueError(_('"%s" is not a string.') % value))
    return value


def validate_list(value):
    if not isinstance(value, list):
        ValidationIssueCollector.appendException(
            ValueError(_('"%s" is not a list.') % value))
    return value


def validate_range(range):
    # list class check
    validate_list(range)
    # validate range list has a min and max
    if len(range) != 2:
        ValidationIssueCollector.appendException(
            ValueError(_('"%s" is not a valid range.') % range))
    # validate min and max are numerics or the keyword UNBOUNDED
    min_test = max_test = False
    if not range[0] == RANGE_UNBOUNDED:
        min = validate_numeric(range[0])
    else:
        min_test = True
    if not range[1] == RANGE_UNBOUNDED:
        max = validate_numeric(range[1])
    else:
        max_test = True
    # validate the max > min (account for UNBOUNDED)
    if not min_test and not max_test:
        # Note: min == max is allowed
        if min > max:
            ValidationIssueCollector.appendException(
                ValueError(_('"%s" is not a valid range.') % range))

    return range


def validate_value_in_range(value, range, prop_name):
    validate_numeric(value)
    validate_range(range)

    # Note: value is valid if equal to min
    if range[0] != RANGE_UNBOUNDED:
        if value < range[0]:
            ValidationIssueCollector.appendException(
                RangeValueError(pname=prop_name,
                                pvalue=value,
                                vmin=range[0],
                                vmax=range[1]))
    # Note: value is valid if equal to max
    if range[1] != RANGE_UNBOUNDED:
        if value > range[1]:
            ValidationIssueCollector.appendException(
                RangeValueError(pname=prop_name,
                                pvalue=value,
                                vmin=range[0],
                                vmax=range[1]))
    return value


def validate_map(value):
    if not isinstance(value, collections.Mapping):
        ValidationIssueCollector.appendException(
            ValueError(_('"%s" is not a map.') % value))
    return value


def validate_boolean(value):
    if isinstance(value, bool):
        return value

    if isinstance(value, str):
        normalised = value.lower()
        if normalised in ['true', 'false']:
            return normalised == 'true'

    ValidationIssueCollector.appendException(
        ValueError(_('"%s" is not a boolean.') % value))


def validate_timestamp(value):
    try:
        # Note: we must return our own exception message
        # as dateutil's parser returns different types / values on
        # different systems. OSX, for example, returns a tuple
        # containing a different error message than Linux
        dateutil.parser.parse(value)
    except Exception as e:
        original_err_msg = str(e)
        log.error(original_err_msg)
        ValidationIssueCollector.appendException(
            ValueError(_('"%(val)s" is not a valid timestamp. "%(msg)s"') %
                       {'val': value, 'msg': original_err_msg}))
    return

*/
