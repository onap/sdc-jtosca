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

package org.onap.sdc.toscaparser.api.elements.constraints;

import com.google.common.collect.ImmutableMap;
import org.onap.sdc.toscaparser.api.common.JToscaValidationIssue;
import org.onap.sdc.toscaparser.api.elements.enums.FileSize;
import org.onap.sdc.toscaparser.api.utils.ThreadLocalsHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;


public class Schema {

    private static final String TYPE = "type";
    private static final String REQUIRED = "required";
    private static final String DESCRIPTION = "description";
    private static final String DEFAULT = "default";
    private static final String CONSTRAINTS = "constraints";
    private static final String STATUS = "status";
    private static final String ENTRYSCHEMA = "entry_schema";
    private static final String[] KEYS = {
            TYPE, REQUIRED, DESCRIPTION, DEFAULT, CONSTRAINTS, ENTRYSCHEMA, STATUS};

    public static final String INTEGER = "integer";
    public static final String STRING = "string";
    public static final String BOOLEAN = "boolean";
    public static final String FLOAT = "float";
    public static final String RANGE = "range";
    public static final String NUMBER = "number";
    public static final String TIMESTAMP = "timestamp";
    public static final String LIST = "list";
    public static final String MAP = "map";
    public static final String SCALAR_UNIT_SIZE = "scalar-unit.size";
    public static final String SCALAR_UNIT_FREQUENCY = "scalar-unit.frequency";
    public static final String SCALAR_UNIT_TIME = "scalar-unit.time";
    public static final String VERSION = "version";
    public static final String PORTDEF = "PortDef";
    public static final String PORTSPEC = "PortSpec"; //??? PortSpec.SHORTNAME
    public static final String JSON = "json";

    public static final String[] PROPERTY_TYPES = {
            INTEGER, STRING, BOOLEAN, FLOAT, RANGE, NUMBER, TIMESTAMP, LIST, MAP,
            SCALAR_UNIT_SIZE, SCALAR_UNIT_FREQUENCY, SCALAR_UNIT_TIME,
            VERSION, PORTDEF, PORTSPEC, JSON};

    public static final String[] SIMPLE_PROPERTY_TYPES = {
            INTEGER, STRING, BOOLEAN, FLOAT, RANGE, NUMBER, TIMESTAMP,
            SCALAR_UNIT_SIZE, SCALAR_UNIT_FREQUENCY, SCALAR_UNIT_TIME,
            VERSION};

    @SuppressWarnings("unused")
    private static final String SCALAR_UNIT_SIZE_DEFAULT = "B";

    private static Map<String, Long> scalarUnitSizeDict = ImmutableMap.<String, Long>builder()
            .put("B", FileSize.B)
            .put("KB", FileSize.KB)
            .put("MB", FileSize.MB)
            .put("GB", FileSize.GB)
            .put("TB", FileSize.TB)
            .put("KIB", FileSize.KIB)
            .put("MIB", FileSize.MIB)
            .put("GIB", FileSize.GIB)
            .put("TIB", FileSize.TIB)
            .build();


    private String name;
    private LinkedHashMap<String, Object> schema;
    private int len;
    private ArrayList<Constraint> constraintsList;


    public Schema(String name, LinkedHashMap<String, Object> schemaDict) {
        this.name = name;

        if (!(schemaDict instanceof LinkedHashMap)) {
            //msg = (_('Schema definition of "%(pname)s" must be a dict.')
            //       % dict(pname=name))
            ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE117", String.format(
                    "InvalidSchemaError: Schema definition of \"%s\" must be a dict", this.name)));
        }

        if (schemaDict.get("type") == null) {
            //msg = (_('Schema definition of "%(pname)s" must have a "type" '
            //         'attribute.') % dict(pname=name))
            ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE118", String.format(
                    "InvalidSchemaError: Schema definition of \"%s\" must have a \"type\" attribute", this.name)));
        }

        schema = schemaDict;
        len = 0; //??? None
        constraintsList = new ArrayList<>();
    }

    public String getType() {
        return (String) schema.get(TYPE);
    }

    public boolean isRequired() {
        return (boolean) schema.getOrDefault(REQUIRED, true);
    }

    public String getDescription() {
        return (String) schema.getOrDefault(DESCRIPTION, "");
    }

    public Object getDefault() {
        return schema.get(DEFAULT);
    }

    public String getStatus() {
        return (String) schema.getOrDefault(STATUS, "");
    }

    public static boolean isRequestedTypeSimple(String type) {
        return Arrays.asList(SIMPLE_PROPERTY_TYPES).contains(type);
    }

    @SuppressWarnings("unchecked")
    public ArrayList<Constraint> getConstraints() {
        if (constraintsList.size() == 0) {
            Object cob = schema.get(CONSTRAINTS);
            if (cob instanceof ArrayList) {
                ArrayList<Object> constraintSchemata = (ArrayList<Object>) cob;
                for (Object ob : constraintSchemata) {
                    if (ob instanceof LinkedHashMap) {
                        for (String cClass : ((LinkedHashMap<String, Object>) ob).keySet()) {
                            Constraint c = Constraint.factory(cClass, name, getType(), ob);
                            if (c != null) {
                                constraintsList.add(c);
                            } else {
                                // error
                                ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE119", String.format(
                                        "UnknownFieldError: Constraint type \"%s\" for property \"%s\" is not supported",
                                        cClass, name)));
                            }
                            break;
                        }
                    }
                }
            }
        }
        return constraintsList;
    }

    @SuppressWarnings("unchecked")
    public LinkedHashMap<String, Object> getEntrySchema() {
        return (LinkedHashMap<String, Object>) schema.get(ENTRYSCHEMA);
    }

    // Python intrinsic methods...

    // substitute for __getitem__ (aka self[key])
    public Object getItem(String key) {
        return schema.get(key);
    }

    /*
    def __iter__(self):
        for k in self.KEYS:
            try:
                self.schema[k]
            except KeyError:
                pass
            else:
                yield k
    */

    // substitute for __len__ (aka self.len())
    public int getLen() {
        int len = 0;
        for (String k : KEYS) {
            if (schema.get(k) != null) {
                len++;
            }
            this.len = len;
        }
        return this.len;
    }

    // getter
    public LinkedHashMap<String, Object> getSchema() {
        return schema;
    }

}

/*python

class Schema(collections.Mapping):

KEYS = (
    TYPE, REQUIRED, DESCRIPTION,
    DEFAULT, CONSTRAINTS, ENTRYSCHEMA, STATUS
) = (
    'type', 'required', 'description',
    'default', 'constraints', 'entry_schema', 'status'
)

PROPERTY_TYPES = (
    INTEGER, STRING, BOOLEAN, FLOAT, RANGE,
    NUMBER, TIMESTAMP, LIST, MAP,
    SCALAR_UNIT_SIZE, SCALAR_UNIT_FREQUENCY, SCALAR_UNIT_TIME,
    VERSION, PORTDEF, PORTSPEC
) = (
    'integer', 'string', 'boolean', 'float', 'range',
    'number', 'timestamp', 'list', 'map',
    'scalar-unit.size', 'scalar-unit.frequency', 'scalar-unit.time',
    'version', 'PortDef', PortSpec.SHORTNAME
)

SCALAR_UNIT_SIZE_DEFAULT = 'B'
scalarUnitSizeDict = {'B': 1, 'KB': 1000, 'KIB': 1024, 'MB': 1000000,
                         'MIB': 1048576, 'GB': 1000000000,
                         'GIB': 1073741824, 'TB': 1000000000000,
                         'TIB': 1099511627776}

def __init__(self, name, schema_dict):
    self.name = name
    if not isinstance(schema_dict, collections.Mapping):
        msg = (_('Schema definition of "%(pname)s" must be a dict.')
               % dict(pname=name))
        ValidationIssueCollector.appendException(InvalidSchemaError(message=msg))

    try:
        schema_dict['type']
    except KeyError:
        msg = (_('Schema definition of "%(pname)s" must have a "type" '
                 'attribute.') % dict(pname=name))
        ValidationIssueCollector.appendException(InvalidSchemaError(message=msg))

    self.schema = schema_dict
    self.len = None
    self.constraints_list = []

@property
def type(self):
    return self.schema[self.TYPE]

@property
def required(self):
    return self.schema.get(self.REQUIRED, True)

@property
def description(self):
    return self.schema.get(self.DESCRIPTION, '')

@property
def default(self):
    return self.schema.get(self.DEFAULT)

@property
def status(self):
    return self.schema.get(self.STATUS, '')

@property
def constraints(self):
    if not self.constraints_list:
        constraint_schemata = self.schema.get(self.CONSTRAINTS)
        if constraint_schemata:
            self.constraints_list = [Constraint(self.name,
                                                self.type,
                                                cschema)
                                     for cschema in constraint_schemata]
    return self.constraints_list

@property
def entry_schema(self):
    return self.schema.get(self.ENTRYSCHEMA)

def __getitem__(self, key):
    return self.schema[key]

def __iter__(self):
    for k in self.KEYS:
        try:
            self.schema[k]
        except KeyError:
            pass
        else:
            yield k

def __len__(self):
    if self.len is None:
        self.len = len(list(iter(self)))
    return self.len
*/
