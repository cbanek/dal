/*
************************************************************************
*******************  CANADIAN ASTRONOMY DATA CENTRE  *******************
**************  CENTRE CANADIEN DE DONNÉES ASTRONOMIQUES  **************
*
*  (c) 2022.                            (c) 2022.
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
*  $Revision: 5 $
*
************************************************************************
*/

package org.opencadc.pkg.server;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.archivers.tar.TarConstants;
import org.apache.log4j.Logger;

public class TarWriter extends PackageWriter {
    private static final Logger log = Logger.getLogger(TarWriter.class);

    public static final String MIME_TYPE = "application/x-tar";
    public static final String EXTENSION = ".tar";

    public TarWriter(OutputStream ostream) {
        super(new TarArchiveOutputStream(ostream));
        ((TarArchiveOutputStream)super.archiveOutputStream).setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
    }

    @Override
    ArchiveEntry createFileEntry(String relativePath, long size, Date lastModifiedDate) {
        log.debug(String.format("file ArchiveEntry: %s %s %s", relativePath, size, lastModifiedDate));

        TarArchiveEntry entry = new TarArchiveEntry(relativePath, TarConstants.LF_NORMAL);
        entry.setSize(size);
        if (lastModifiedDate != null) {
            entry.setModTime(lastModifiedDate);
        }
        return entry;
    }

    @Override
    ArchiveEntry createDirectoryEntry(String relativePath) {
        log.debug("directory ArchiveEntry: " + relativePath);

        if (relativePath.startsWith("/")) {
            relativePath = relativePath.substring(1);
        }
        if (!relativePath.endsWith("/")) {
            relativePath = relativePath + "/";
        }

        return new TarArchiveEntry(relativePath, TarConstants.LF_DIR);
    }

    @Override
    ArchiveEntry createSymbolicLinkEntry(String relativePath, String linkTarget) {
        log.debug(String.format("symbolic link ArchiveEntry: %s -> %s", relativePath, linkTarget));

        TarArchiveEntry entry = new TarArchiveEntry(relativePath, TarConstants.LF_SYMLINK);
        entry.setLinkName(linkTarget);
        return entry;
    }

}
