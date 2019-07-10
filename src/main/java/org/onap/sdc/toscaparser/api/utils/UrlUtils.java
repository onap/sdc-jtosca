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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class UrlUtils {

    private static final int HTTP_STATUS_OK = 200;

    private UrlUtils() {
    }

    public static boolean validateUrl(String sUrl) {
        // Validates whether the given path is a URL or not

        // If the given path includes a scheme (http, https, ftp, ...) and a net
        // location (a domain name such as www.github.com) it is validated as a URL
        try {
            URL url = new URL(sUrl);
            if (url.getProtocol().equals("file")) {
                return true;
            }
            return url.getAuthority() != null;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    public static String joinUrl(String sUrl, String relativePath) {
        // Builds a new URL from the given URL and the relative path

        // Example:
        //   url: http://www.githib.com/openstack/heat
        //   relative_path: heat-translator
        //   - joined: http://www.githib.com/openstack/heat-translator
        if (!validateUrl(sUrl)) {
            ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE255", String.format(
                    "ValueError: The URL \"%s\" is malformed", sUrl)));
        }
        try {
            URL base = new URL(sUrl);
            return (new URL(base, relativePath)).toString();
        } catch (MalformedURLException e) {
            ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE256", String.format(
                    "ValueError: Joining URL \"%s\" and relative path \"%s\" caused an exception", sUrl, relativePath)));
            return sUrl;
        }
    }

    public static boolean isUrlAccessible(String sUrl) {
        // Validates whether the given URL is accessible

        // Returns true if the get call returns a 200 response code.
        // Otherwise, returns false.
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(sUrl).openConnection();
            connection.setRequestMethod("HEAD");
            int responseCode = connection.getResponseCode();
            return responseCode == HTTP_STATUS_OK;
        } catch (IOException e) {
            return false;
        }
    }

}

/*python

from six.moves.urllib.parse import urljoin
from six.moves.urllib.parse import urlparse
from toscaparser.common.exception import ValidationIssueCollector
from toscaparser.utils.gettextutils import _

try:
    # Python 3.x
    import urllib.request as urllib2
except ImportError:
    # Python 2.x
    import urllib2


class UrlUtils(object):

    @staticmethod
    def validate_url(path):
        """Validates whether the given path is a URL or not.

        If the given path includes a scheme (http, https, ftp, ...) and a net
        location (a domain name such as www.github.com) it is validated as a
        URL.
        """
        parsed = urlparse(path)
        if parsed.scheme == 'file':
            # If the url uses the file scheme netloc will be ""
            return True
        else:
            return bool(parsed.scheme) and bool(parsed.netloc)

    @staticmethod
    def join_url(url, relative_path):
        """Builds a new URL from the given URL and the relative path.

        Example:
          url: http://www.githib.com/openstack/heat
          relative_path: heat-translator
          - joined: http://www.githib.com/openstack/heat-translator
        """
        if not UrlUtils.validate_url(url):
            ValidationIssueCollector.appendException(
                ValueError(_('"%s" is not a valid URL.') % url))
        return urljoin(url, relative_path)

    @staticmethod
    def url_accessible(url):
        """Validates whether the given URL is accessible.

        Returns true if the get call returns a 200 response code.
        Otherwise, returns false.
        """
        return urllib2.urlopen(url).getcode() == 200
*/
