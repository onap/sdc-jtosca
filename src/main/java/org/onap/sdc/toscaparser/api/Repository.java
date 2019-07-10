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

import org.onap.sdc.toscaparser.api.common.JToscaValidationIssue;
import org.onap.sdc.toscaparser.api.utils.ThreadLocalsHolder;
import org.onap.sdc.toscaparser.api.utils.UrlUtils;

import java.util.LinkedHashMap;

public class Repository {

    private static final String DESCRIPTION = "description";
    private static final String URL = "url";
    private static final String CREDENTIAL = "credential";
    private static final String SECTIONS[] = {DESCRIPTION, URL, CREDENTIAL};

    private String name;
    private Object reposit;
    private String url;

    @SuppressWarnings("unchecked")
    public Repository(String repName, Object repValue) {
        name = repName;
        reposit = repValue;
        if (reposit instanceof LinkedHashMap) {
            url = (String) ((LinkedHashMap<String, Object>) reposit).get("url");
            if (url == null) {
                ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE229", String.format(
                        "MissingRequiredFieldError: Repository \"%s\" is missing required field \"url\"",
                        name)));
            }
        }
        loadAndValidate(name, reposit);
    }

    @SuppressWarnings("unchecked")
    private void loadAndValidate(String val, Object repositDef) {
        String keyname = val;
        if (repositDef instanceof LinkedHashMap) {
            for (String key : ((LinkedHashMap<String, Object>) reposit).keySet()) {
                boolean bFound = false;
                for (String sect : SECTIONS) {
                    if (key.equals(sect)) {
                        bFound = true;
                        break;
                    }
                }
                if (!bFound) {
                    ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE230", String.format(
                            "UnknownFieldError: repositories \"%s\" contains unknown field \"%s\"",
                            keyname, key)));
                }
            }

            String repositUrl = (String) ((LinkedHashMap<String, Object>) repositDef).get("url");
            if (repositUrl != null) {
                boolean urlVal = UrlUtils.validateUrl(repositUrl);
                if (!urlVal) {
                    ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE231", String.format(
                            "URLException: repsositories \"%s\" Invalid Url", keyname)));
                }
            }
        }
    }

    @Override
    public String toString() {
        return "Repository{" +
                "name='" + name + '\'' +
                ", reposit=" + reposit +
                ", url='" + url + '\'' +
                '}';
    }
}

/*python

from toscaparser.common.exception import ValidationIssueCollector
from toscaparser.common.exception import MissingRequiredFieldError
from toscaparser.common.exception import UnknownFieldError
from toscaparser.common.exception import URLException
from toscaparser.utils.gettextutils import _
import org.openecomp.sdc.toscaparser.api.utils.urlutils

SECTIONS = (DESCRIPTION, URL, CREDENTIAL) = \
           ('description', 'url', 'credential')


class Repository(object):
    def __init__(self, repositories, values):
        self.name = repositories
        self.reposit = values
        if isinstance(self.reposit, dict):
            if 'url' not in self.reposit.keys():
                ValidationIssueCollector.appendException(
                    MissingRequiredFieldError(what=_('Repository "%s"')
                                              % self.name, required='url'))
            self.url = self.reposit['url']
        self.load_and_validate(self.name, self.reposit)

    def load_and_validate(self, val, reposit_def):
        self.keyname = val
        if isinstance(reposit_def, dict):
            for key in reposit_def.keys():
                if key not in SECTIONS:
                    ValidationIssueCollector.appendException(
                        UnknownFieldError(what=_('repositories "%s"')
                                          % self.keyname, field=key))

            if URL in reposit_def.keys():
                reposit_url = reposit_def.get(URL)
                url_val = toscaparser.utils.urlutils.UrlUtils.\
                    validate_url(reposit_url)
                if url_val is not True:
                    ValidationIssueCollector.appendException(
                        URLException(what=_('repsositories "%s" Invalid Url')
                                     % self.keyname))
*/
