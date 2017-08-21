package org.openecomp.sdc.toscaparser.api.extensions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExtTools {

	private static Logger log = LoggerFactory.getLogger(ExtTools.class.getName());

	private static LinkedHashMap<String,Object> EXTENSION_INFO = new LinkedHashMap<>();
	
	public ExtTools() {
		
        EXTENSION_INFO = _loadExtensions();
	}
	
	private LinkedHashMap<String,Object> _loadExtensions() {
		
		LinkedHashMap<String,Object> extensions = new LinkedHashMap<>();
		
		String path = ExtTools.class.getProtectionDomain().getCodeSource().getLocation().getPath();
	    //String extdir = path + File.separator + "resources/extensions";
	    
	    String extdir = ExtTools.class.getClassLoader().getResource("extensions").getFile();
	    
	    // for all folders in extdir
	    File extDir = new File(extdir);
	    File extDirList[] = extDir.listFiles();
	    if (extDirList != null) {
		    for(File f: extDirList) {
		    	if(f.isDirectory()) {
		  	        // for all .py files in folder
		    		File extFileList[] = f.listFiles();
		    		for(File pyf: extFileList) {
		    			String pyfName = pyf.getName();
		    			String pyfPath = pyf.getAbsolutePath();
		    			if(pyfName.endsWith(".py")) {
		    				// get VERSION,SECTIONS,DEF_FILE
		    				try {
			    				String version = null;
			    				ArrayList<String> sections = null;
			    				String defsFile = null;
		    				    String line;
		    					InputStream fis = new FileInputStream(pyfPath);
		    				    InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
		    				    BufferedReader br = new BufferedReader(isr);
		    					Pattern pattern = Pattern.compile("^([^#]\\S+)\\s*=\\s*(\\S.*)$");
	    					    while((line = br.readLine()) != null) {
		    					    line = line.replace("'","\"");
	    	    					Matcher matcher = pattern.matcher(line.toString());
	    	    					if(matcher.find()) {
	    	    						if(matcher.group(1).equals("VERSION")) {
	    	    							version = matcher.group(2);
	    	    							if(version.startsWith("'") || version.startsWith("\"")) {
	    	    								version = version.substring(1,version.length()-1);
	    	    							}
	    	    						}
	    	    						else if(matcher.group(1).equals("DEFS_FILE")) {
	    	    							String fn = matcher.group(2);
	    	    							if(fn.startsWith("'") || fn.startsWith("\"")) {
	    	    								fn = fn.substring(1,fn.length()-1);
	    	    							}
	    	    							defsFile = pyf.getParent() + File.separator + fn;//matcher.group(2);
	    	    						}
	    	    						else if(matcher.group(1).equals("SECTIONS")) {
	    	    							sections = new ArrayList<>();
	    	    							Pattern secpat = Pattern.compile("\"([^\"]+)\"");
	    	    	    					Matcher secmat = secpat.matcher(matcher.group(2));
	    	    	    					while(secmat.find()) {
	    	    	    						sections.add(secmat.group(1));
	    	    	    					}
	    	    						}
	    	    					}
		    					}
	    					    br.close();
	    					    
	    					    if(version != null && defsFile != null) {
	    					    	LinkedHashMap<String,Object> ext = new LinkedHashMap<>();
	    	    					ext.put("defs_file", defsFile);
	    	    					if(sections != null) {
	        	    					ext.put("sections", sections);
	    	    					}
	    	    					extensions.put(version, ext);
		    					}
	    					    else {
	    					    	// error
	    					    }
		    				}
		    				catch(Exception e) {
		    					log.error("ExtTools - _loadExtensions - {}", e.getMessage());
		    					// ...
		    				}
		    			}
		    		}
		    	}
		    }
	    }
		return extensions;
	}
	
	public ArrayList<String> getVersions() {
		return new ArrayList<String>(EXTENSION_INFO.keySet());
	}
	
	public LinkedHashMap<String,ArrayList<String>> getSections() {
		LinkedHashMap<String,ArrayList<String>> sections = new LinkedHashMap<>();
        for(String version: EXTENSION_INFO.keySet()) {
        	LinkedHashMap<String,Object> eiv = (LinkedHashMap<String,Object>)EXTENSION_INFO.get(version);
        	sections.put(version,(ArrayList<String>)eiv.get("sections"));
        }
        return sections;
	}

	public String getDefsFile(String version) { 
    	LinkedHashMap<String,Object> eiv = (LinkedHashMap<String,Object>)EXTENSION_INFO.get(version);
    	return (String)eiv.get("defs_file");
	}
	
}

/*python

from toscaparser.common.exception import ToscaExtAttributeError
from toscaparser.common.exception import ToscaExtImportError

log = logging.getLogger("tosca.model")

REQUIRED_ATTRIBUTES = ['VERSION', 'DEFS_FILE']


class ExtTools(object):
    def __init__(self):
        self.EXTENSION_INFO = self._load_extensions()

    def _load_extensions(self):
        '''Dynamically load all the extensions .'''
        extensions = {}

        # Use the absolute path of the class path
        abs_path = os.path.dirname(os.path.abspath(__file__))

        extdirs = [e for e in os.listdir(abs_path) if
                   not e.startswith('tests') and
                   os.path.isdir(os.path.join(abs_path, e))]

        for e in extdirs:
            log.info(e)
            extpath = abs_path + '/' + e
            # Grab all the extension files in the given path
            ext_files = [f for f in os.listdir(extpath) if f.endswith('.py')
                         and not f.startswith('__init__')]

            # For each module, pick out the target translation class
            for f in ext_files:
                log.info(f)
                ext_name = 'toscaparser/extensions/' + e + '/' + f.strip('.py')
                ext_name = ext_name.replace('/', '.')
                try:
                    extinfo = importlib.import_module(ext_name)
                    version = getattr(extinfo, 'VERSION')
                    defs_file = extpath + '/' + getattr(extinfo, 'DEFS_FILE')

                    # Sections is an optional attribute
                    sections = getattr(extinfo, 'SECTIONS', ())

                    extensions[version] = {'sections': sections,
                                           'defs_file': defs_file}
                except ImportError:
                    raise ToscaExtImportError(ext_name=ext_name)
                except AttributeError:
                    attrs = ', '.join(REQUIRED_ATTRIBUTES)
                    raise ToscaExtAttributeError(ext_name=ext_name,
                                                 attrs=attrs)

        print 'Extensions ',extensions#GGG
        return extensions

    def get_versions(self):
        return self.EXTENSION_INFO.keys()

    def get_sections(self):
        sections = {}
        for version in self.EXTENSION_INFO.keys():
            sections[version] = self.EXTENSION_INFO[version]['sections']

        return sections

    def get_defs_file(self, version):
        versiondata = self.EXTENSION_INFO.get(version)

        if versiondata:
            return versiondata.get('defs_file')
        else:
            return None
*/