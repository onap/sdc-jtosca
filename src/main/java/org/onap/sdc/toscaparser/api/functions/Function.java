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

package org.onap.sdc.toscaparser.api.functions;


import org.onap.sdc.toscaparser.api.TopologyTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class Function {

    protected static final String GET_PROPERTY = "get_property";
    protected static final String GET_ATTRIBUTE = "get_attribute";
    protected static final String GET_INPUT = "get_input";
    protected static final String GET_OPERATION_OUTPUT = "get_operation_output";
    protected static final String CONCAT = "concat";
    protected static final String TOKEN = "token";

    protected static final String SELF = "SELF";
    protected static final String HOST = "HOST";
    protected static final String TARGET = "TARGET";
    protected static final String SOURCE = "SOURCE";

    protected static final String HOSTED_ON = "tosca.relationships.HostedOn";

    protected static HashMap<String, String> functionMappings = _getFunctionMappings();

    private static HashMap<String, String> _getFunctionMappings() {
        HashMap<String, String> map = new HashMap<>();
        map.put(GET_PROPERTY, "GetProperty");
        map.put(GET_INPUT, "GetInput");
        map.put(GET_ATTRIBUTE, "GetAttribute");
        map.put(GET_OPERATION_OUTPUT, "GetOperationOutput");
        map.put(CONCAT, "Concat");
        map.put(TOKEN, "Token");
        return map;
    }

    protected TopologyTemplate toscaTpl;
    protected Object context;
    protected String name;
    protected ArrayList<Object> args;


    public Function(TopologyTemplate _toscaTpl, Object _context, String _name, ArrayList<Object> _args) {
        toscaTpl = _toscaTpl;
        context = _context;
        name = _name;
        args = _args;
        validate();

    }

    abstract Object result();

    abstract void validate();

    @SuppressWarnings("unchecked")
    public static boolean isFunction(Object funcObj) {
        // Returns True if the provided function is a Tosca intrinsic function.
        //
        //Examples:
        //
        //* "{ get_property: { SELF, port } }"
        //* "{ get_input: db_name }"
        //* Function instance

        //:param function: Function as string or a Function instance.
        //:return: True if function is a Tosca intrinsic function, otherwise False.
        //

        if (funcObj instanceof LinkedHashMap) {
            LinkedHashMap<String, Object> function = (LinkedHashMap<String, Object>) funcObj;
            if (function.size() == 1) {
                String funcName = (new ArrayList<String>(function.keySet())).get(0);
                return functionMappings.keySet().contains(funcName);
            }
        }
        return (funcObj instanceof Function);
    }

    @SuppressWarnings("unchecked")
    public static Object getFunction(TopologyTemplate ttpl, Object context, Object rawFunctionObj, boolean resolveGetInput) {
        // Gets a Function instance representing the provided template function.

        // If the format provided raw_function format is not relevant for template
        // functions or if the function name doesn't exist in function mapping the
        // method returns the provided raw_function.
        //
        // :param tosca_tpl: The tosca template.
        // :param node_template: The node template the function is specified for.
        // :param raw_function: The raw function as dict.
        // :return: Template function as Function instance or the raw_function if
        //  parsing was unsuccessful.


        // iterate over leaves of the properties's tree and convert function leaves to function object,
        // support List and Map nested,
        // assuming that leaf value of function is always map type contains 1 item (e.g. my_leaf: {get_input: xxx}).

        if (rawFunctionObj instanceof LinkedHashMap) { // In map type case
            LinkedHashMap rawFunction = ((LinkedHashMap) rawFunctionObj);
            if (rawFunction.size() == 1 &&
                    !(rawFunction.values().iterator().next() instanceof LinkedHashMap)) { // End point
                return getFunctionForObjectItem(ttpl, context, rawFunction, resolveGetInput);
            } else {
                return getFunctionForMap(ttpl, context, rawFunction, resolveGetInput);
            }
        } else if (rawFunctionObj instanceof ArrayList) { // In list type case
            return getFunctionForList(ttpl, context, (ArrayList) rawFunctionObj, resolveGetInput);
        }

        return rawFunctionObj;
    }

    private static Object getFunctionForList(TopologyTemplate ttpl, Object context, ArrayList rawFunctionObj, boolean resolveGetInput) {
        // iterate over list properties in recursion, convert leaves to function,
        // and collect them in the same hierarchy as the original list.
        ArrayList<Object> rawFunctionObjList = new ArrayList<>();
        for (Object rawFunctionObjItem : rawFunctionObj) {
            rawFunctionObjList.add(getFunction(ttpl, context, rawFunctionObjItem, resolveGetInput));
        }
        return rawFunctionObjList;
    }

    private static Object getFunctionForMap(TopologyTemplate ttpl, Object context, LinkedHashMap rawFunction, boolean resolveGetInput) {
        // iterate over map nested properties in recursion, convert leaves to function,
        // and collect them in the same hierarchy as the original map.
        LinkedHashMap rawFunctionObjMap = new LinkedHashMap();
        for (Object rawFunctionObjItem : rawFunction.entrySet()) {
            Object itemValue = getFunction(ttpl, context, ((Map.Entry) rawFunctionObjItem).getValue(), resolveGetInput);
            rawFunctionObjMap.put(((Map.Entry) rawFunctionObjItem).getKey(), itemValue);
        }
        return rawFunctionObjMap;
    }

    private static Object getFunctionForObjectItem(TopologyTemplate ttpl, Object context, Object rawFunctionObjItem, boolean resolveGetInput) {
        if (isFunction(rawFunctionObjItem)) {
            LinkedHashMap<String, Object> rawFunction = (LinkedHashMap<String, Object>) rawFunctionObjItem;
            String funcName = (new ArrayList<String>(rawFunction.keySet())).get(0);
            if (functionMappings.keySet().contains(funcName)) {
                String funcType = functionMappings.get(funcName);
                Object oargs = (new ArrayList<Object>(rawFunction.values())).get(0);
                ArrayList<Object> funcArgs;
                if (oargs instanceof ArrayList) {
                    funcArgs = (ArrayList<Object>) oargs;
                } else {
                    funcArgs = new ArrayList<>();
                    funcArgs.add(oargs);
                }

                switch (funcType) {
                    case "GetInput":
                        if (resolveGetInput) {
                            GetInput input = new GetInput(ttpl, context, funcName, funcArgs);
                            return input.result();
                        }
                        return new GetInput(ttpl, context, funcName, funcArgs);
                    case "GetAttribute":
                        return new GetAttribute(ttpl, context, funcName, funcArgs);
                    case "GetProperty":
                        return new GetProperty(ttpl, context, funcName, funcArgs);
                    case "GetOperationOutput":
                        return new GetOperationOutput(ttpl, context, funcName, funcArgs);
                    case "Concat":
                        return new Concat(ttpl, context, funcName, funcArgs);
                    case "Token":
                        return new Token(ttpl, context, funcName, funcArgs);
                }
            }
        }

        return rawFunctionObjItem;
    }

    @Override
    public String toString() {
        String argsStr = args.size() > 1 ? args.toString() : args.get(0).toString();
        return name + ":" + argsStr;
    }
}

/*python

from toscaparser.common.exception import ValidationIsshueCollector
from toscaparser.common.exception import UnknownInputError
from toscaparser.dataentity import DataEntity
from toscaparser.elements.constraints import Schema
from toscaparser.elements.datatype import DataType
from toscaparser.elements.entity_type import EntityType
from toscaparser.elements.relationshiptype import RelationshipType
from toscaparser.elements.statefulentitytype import StatefulEntityType
from toscaparser.utils.gettextutils import _


GET_PROPERTY = 'get_property'
GET_ATTRIBUTE = 'get_attribute'
GET_INPUT = 'get_input'
GET_OPERATION_OUTPUT = 'get_operation_output'
CONCAT = 'concat'
TOKEN = 'token'

SELF = 'SELF'
HOST = 'HOST'
TARGET = 'TARGET'
SOURCE = 'SOURCE'

HOSTED_ON = 'tosca.relationships.HostedOn'


@six.add_metaclass(abc.ABCMeta)
class Function(object):
    """An abstract type for representing a Tosca template function."""

    def __init__(self, tosca_tpl, context, name, args):
        self.tosca_tpl = tosca_tpl
        self.context = context
        self.name = name
        self.args = args
        self.validate()

    @abc.abstractmethod
    def result(self):
        """Invokes the function and returns its result

        Some methods invocation may only be relevant on runtime (for example,
        getting runtime properties) and therefore its the responsibility of
        the orchestrator/translator to take care of such functions invocation.

        :return: Function invocation result.
        """
        return {self.name: self.args}

    @abc.abstractmethod
    def validate(self):
        """Validates function arguments."""
        pass
*/
