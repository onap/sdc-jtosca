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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class DumpUtils {

    @SuppressWarnings("unchecked")
    private static void dumpYaml(Object yo, int level) {
        final String indent = "                                                                         ";
        try {
            if (yo == null) {
                System.out.println("<null>");
                return;
            }
            String cname = yo.getClass().getSimpleName();
            System.out.print(cname);
            if (cname.equals("LinkedHashMap")) {
                LinkedHashMap<String, Object> lhm = (LinkedHashMap<String, Object>) yo;
                System.out.println();
                for (Map.Entry<String, Object> me : lhm.entrySet()) {
                    System.out.print(indent.substring(0, level) + me.getKey() + ": ");
                    dumpYaml(me.getValue(), level + 2);
                }
            } else if (cname.equals("ArrayList")) {
                ArrayList<Object> al = (ArrayList<Object>) yo;
                System.out.println();
                for (int i = 0; i < al.size(); i++) {
                    System.out.format("%s[%d] ", indent.substring(0, level), i);
                    dumpYaml(al.get(i), level + 2);
                }
            } else if (cname.equals("String")) {
                System.out.println(" ==> \"" + (String) yo + "\"");
            } else if (cname.equals("Integer")) {
                System.out.println(" ==> " + (int) yo);
            } else if (cname.equals("Boolean")) {
                System.out.println(" ==> " + (boolean) yo);
            } else if (cname.equals("Double")) {
                System.out.println(" ==> " + (double) yo);
            } else {
                System.out.println(" !! unexpected type");
            }
        } catch (Exception e) {
            System.out.println("Exception!! " + e.getMessage());
        }
    }
}
