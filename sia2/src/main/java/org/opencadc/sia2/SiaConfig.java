/*
************************************************************************
*******************  CANADIAN ASTRONOMY DATA CENTRE  *******************
**************  CENTRE CANADIEN DE DONNÉES ASTRONOMIQUES  **************
*
*  (c) 2023.                            (c) 2023.
*  Government of Canada                 Gouvernement du Canada
*  National Research Council            Conseil national de recherches
*  Ottawa, Canada, K1A 0R6              Ottawa, Canada, K1A 0R6
*  All rights reserved                  Tous droits réservés
*
*  NRC disclaims any warranties,        Le CNRC dénie toute garantie
*  expressed, implied, or               énoncée, implicite ou légale,
*  statutory, of any kind with          de quelque nature que ce
*  respect to the software,             soit, concernant le logiciel,
*  including without limitation         y compris sans restriction
*  any warranty of merchantability      toute garantie de valeur
*  or fitness for a particular          marchande ou de pertinence
*  purpose. NRC shall not be            pour un usage particulier.
*  liable in any event for any          Le CNRC ne pourra en aucun cas
*  damages, whether direct or           être tenu responsable de tout
*  indirect, special or general,        dommage, direct ou indirect,
*  consequential or incidental,         particulier ou général,
*  arising from the use of the          accessoire ou fortuit, résultant
*  software.  Neither the name          de l'utilisation du logiciel. Ni
*  of the National Research             le nom du Conseil National de
*  Council of Canada nor the            Recherches du Canada ni les noms
*  names of its contributors may        de ses  participants ne peuvent
*  be used to endorse or promote        être utilisés pour approuver ou
*  products derived from this           promouvoir les produits dérivés
*  software without specific prior      de ce logiciel sans autorisation
*  written permission.                  préalable et particulière
*                                       par écrit.
*
*  This file is part of the             Ce fichier fait partie du projet
*  OpenCADC project.                    OpenCADC.
*
*  OpenCADC is free software:           OpenCADC est un logiciel libre ;
*  you can redistribute it and/or       vous pouvez le redistribuer ou le
*  modify it under the terms of         modifier suivant les termes de
*  the GNU Affero General Public        la “GNU Affero General Public
*  License as published by the          License” telle que publiée
*  Free Software Foundation,            par la Free Software Foundation
*  either version 3 of the              : soit la version 3 de cette
*  License, or (at your option)         licence, soit (à votre gré)
*  any later version.                   toute version ultérieure.
*
*  OpenCADC is distributed in the       OpenCADC est distribué
*  hope that it will be useful,         dans l’espoir qu’il vous
*  but WITHOUT ANY WARRANTY;            sera utile, mais SANS AUCUNE
*  without even the implied             GARANTIE : sans même la garantie
*  warranty of MERCHANTABILITY          implicite de COMMERCIALISABILITÉ
*  or FITNESS FOR A PARTICULAR          ni d’ADÉQUATION À UN OBJECTIF
*  PURPOSE.  See the GNU Affero         PARTICULIER. Consultez la Licence
*  General Public License for           Générale Publique GNU Affero
*  more details.                        pour plus de détails.
*
*  You should have received             Vous devriez avoir reçu une
*  a copy of the GNU Affero             copie de la Licence Générale
*  General Public License along         Publique GNU Affero avec
*  with OpenCADC.  If not, see          OpenCADC ; si ce n’est
*  <http://www.gnu.org/licenses/>.      pas le cas, consultez :
*                                       <http://www.gnu.org/licenses/>.
*
************************************************************************
*/

package org.opencadc.sia2;

import ca.nrc.cadc.auth.AuthMethod;
import ca.nrc.cadc.net.ResourceNotFoundException;
import ca.nrc.cadc.reg.Standards;
import ca.nrc.cadc.reg.client.RegistryClient;
import ca.nrc.cadc.util.InvalidConfigException;
import ca.nrc.cadc.util.MultiValuedProperties;
import ca.nrc.cadc.util.PropertiesReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import org.apache.log4j.Logger;
import org.opencadc.tap.TapClient;

/**
 *
 * @author pdowler
 */
public class SiaConfig {
    private static final Logger log = Logger.getLogger(SiaConfig.class);

    private static final String CONFIG = "sia2.properties";
    
    private static final String BASE_KEY = "org.opencadc.sia2";
    private static final String QUERY_KEY = BASE_KEY + ".queryService";
    private static final String TABLE_KEY = BASE_KEY + ".table";
    
    private final URI queryService;
    private final String tableName;
    
    public SiaConfig() {
        StringBuilder sb = new StringBuilder();
        try {
            PropertiesReader r = new PropertiesReader(CONFIG);
            MultiValuedProperties props = r.getAllProperties();
            
            String qs = props.getFirstPropertyValue(QUERY_KEY);
            URI qsURI = null;
            sb.append("\n\t").append(QUERY_KEY).append(" - ");
            if (qs == null) {
                sb.append("MISSING");
            } else {
                try {
                    qsURI = new URI(qs);
                    sb.append("OK");
                } catch (URISyntaxException ex) {
                    sb.append("ERROR invalid URI: " + qs);
                }
            }
            
            if (qsURI == null) {
                throw new InvalidConfigException("invalid config: " + sb.toString());
            }
            this.queryService = qsURI;

	    String tn = props.getFirstPropertyValue(TABLE_KEY);
	    if (tn == null) {
		this.tableName = "ivoa.ObsCore";
	    } else {
                this.tableName = tn;
	    }
        } catch (InvalidConfigException ex) {
            throw ex;
        }
    }
    
    public String getTableName() {
	return tableName;
    }

    public URI getQueryService() {
        return queryService;
    }
    
    public URL getTapSyncURL() throws MalformedURLException, ResourceNotFoundException {
        if (queryService.getScheme().equals("ivo")) {
            // registry lookup
            RegistryClient reg = new RegistryClient();
            URL base = reg.getServiceURL(queryService, Standards.TAP_10, AuthMethod.ANON);
            if (base == null) {
                throw new ResourceNotFoundException("not found in registry: " + queryService);
            }
            return new URL(base.toExternalForm() + "/sync");
        }

        // assume direct URL
        return new URL(queryService.toASCIIString() + "/sync");
    }

    public URL getAvailailityURL() throws MalformedURLException {
        if (queryService.getScheme().equals("ivo")) {
            // registry lookup
            RegistryClient reg = new RegistryClient();
            return reg.getServiceURL(queryService, Standards.VOSI_AVAILABILITY, AuthMethod.ANON);
        }

        // assume direct URL
        return new URL(queryService.toASCIIString() + "/availability");
    }
}
