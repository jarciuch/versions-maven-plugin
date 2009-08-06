package org.codehaus.mojo.versions.api;

/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements.  See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership.  The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License.  You may obtain a copy of the License at
*
*  http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.artifact.metadata.ArtifactMetadataRetrievalException;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.artifact.versioning.VersionRange;
import org.codehaus.mojo.versions.ordering.VersionComparator;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by IntelliJ IDEA.
 *
 * @author connollys
 * @since Aug 4, 2009 11:04:36 AM
 */
public abstract class AbstractVersionDetails
    implements VersionDetails
{

    public final ArtifactVersion[] getVersions()
    {
        return getVersions( true );
    }

    public abstract ArtifactVersion[] getVersions( boolean includeSnapshots );

    public final ArtifactVersion[] getVersions( VersionRange versionRange, boolean includeSnapshots )
    {
        Set/*<ArtifactVersion>*/ result;
        result = new TreeSet( getVersionComparator() );
        Iterator i = Arrays.asList( getVersions( includeSnapshots ) ).iterator();
        while ( i.hasNext() )
        {
            ArtifactVersion candidate = (ArtifactVersion) i.next();
            if ( !versionRange.containsVersion( candidate ) )
            {
                continue;
            }
            if ( !includeSnapshots && ArtifactUtils.isSnapshot( candidate.toString() ) )
            {
                continue;
            }
            result.add( candidate );
        }
        return (ArtifactVersion[]) result.toArray( new ArtifactVersion[result.size()] );

    }

    public final ArtifactVersion[] getVersions( ArtifactVersion currentVersion, ArtifactVersion upperBound )
    {
        return getVersions( currentVersion, upperBound, false );
    }

    public final ArtifactVersion[] getVersions( ArtifactVersion currentVersion, ArtifactVersion upperBound,
                                                boolean includeSnapshots )
    {
        return getVersions( currentVersion, upperBound, includeSnapshots, false, false );
    }

    private final ArtifactVersion[] getNewerVersions( ArtifactVersion version, boolean includeSnapshots )
    {
        return getVersions( version, null, includeSnapshots, false, true );
    }

    public final ArtifactVersion getNewestVersion( ArtifactVersion lowerBound, ArtifactVersion upperBound )
    {
        return getNewestVersion( lowerBound, upperBound, true );
    }

    public final ArtifactVersion getNewestVersion( ArtifactVersion lowerBound, ArtifactVersion upperBound,
                                                   boolean includeSnapshots )
    {
        return getNewestVersion( lowerBound, upperBound, includeSnapshots, false, false );
    }

    public final ArtifactVersion getNewestVersion( ArtifactVersion lowerBound, ArtifactVersion upperBound,
                                                   boolean includeSnapshots, boolean includeLower,
                                                   boolean includeUpper )
    {
        ArtifactVersion latest = null;
        final VersionComparator versionComparator = getVersionComparator();
        Iterator i = Arrays.asList( getVersions( includeSnapshots ) ).iterator();
        while ( i.hasNext() )
        {
            ArtifactVersion candidate = (ArtifactVersion) i.next();
            int lower = lowerBound == null ? -1 : versionComparator.compare( lowerBound, candidate );
            int upper = upperBound == null ? +1 : versionComparator.compare( upperBound, candidate );
            if ( lower > 0 || upper < 0 )
            {
                continue;
            }
            if ( ( !includeLower && lower == 0 ) || ( !includeUpper && upper == 0 ) )
            {
                continue;
            }
            if ( !includeSnapshots && ArtifactUtils.isSnapshot( candidate.toString() ) )
            {
                continue;
            }
            if ( latest == null )
            {
                latest = candidate;
            }
            else if ( versionComparator.compare( latest, candidate ) < 0 )
            {
                latest = candidate;
            }
        }
        return latest;
    }

    public final ArtifactVersion getNewestVersion( VersionRange versionRange, boolean includeSnapshots )
    {
        ArtifactVersion latest = null;
        final VersionComparator versionComparator = getVersionComparator();
        Iterator i = Arrays.asList( getVersions( includeSnapshots ) ).iterator();
        while ( i.hasNext() )
        {
            ArtifactVersion candidate = (ArtifactVersion) i.next();
            if ( !versionRange.containsVersion( candidate ) )
            {
                continue;
            }
            if ( !includeSnapshots && ArtifactUtils.isSnapshot( candidate.toString() ) )
            {
                continue;
            }
            if ( latest == null )
            {
                latest = candidate;
            }
            else if ( versionComparator.compare( latest, candidate ) < 0 )
            {
                latest = candidate;
            }
        }
        return latest;
    }

    public final boolean containsVersion( String version )
    {
        Iterator i = Arrays.asList( getVersions( true ) ).iterator();
        while ( i.hasNext() )
        {
            ArtifactVersion candidate = (ArtifactVersion) i.next();
            if ( version.equals( candidate.toString() ) )
            {
                return true;
            }
        }
        return false;
    }

    public final ArtifactVersion[] getNewerVersions( String version, boolean includeSnapshots )
    {
        return getNewerVersions( new DefaultArtifactVersion( version ), includeSnapshots );
    }

    public final ArtifactVersion getOldestVersion( ArtifactVersion lowerBound, ArtifactVersion upperBound )
    {
        return getOldestVersion( lowerBound, upperBound, true );
    }

    public final ArtifactVersion getOldestVersion( VersionRange versionRange, boolean includeSnapshots )
    {
        ArtifactVersion oldest = null;
        final VersionComparator versionComparator = getVersionComparator();
        Iterator i = Arrays.asList( getVersions( includeSnapshots ) ).iterator();
        while ( i.hasNext() )
        {
            ArtifactVersion candidate = (ArtifactVersion) i.next();
            if ( !versionRange.containsVersion( candidate ) )
            {
                continue;
            }
            if ( !includeSnapshots && ArtifactUtils.isSnapshot( candidate.toString() ) )
            {
                continue;
            }
            if ( oldest == null )
            {
                oldest = candidate;
            }
            else if ( versionComparator.compare( oldest, candidate ) > 0 )
            {
                oldest = candidate;
            }
        }
        return oldest;
    }

    public final ArtifactVersion getOldestVersion( ArtifactVersion lowerBound, ArtifactVersion upperBound,
                                                   boolean includeSnapshots )
    {
        return getOldestVersion( lowerBound, upperBound, includeSnapshots, false, false );
    }

    public final ArtifactVersion getOldestVersion( ArtifactVersion lowerBound, ArtifactVersion upperBound,
                                                   boolean includeSnapshots, boolean includeLower,
                                                   boolean includeUpper )
    {
        ArtifactVersion oldest = null;
        final VersionComparator versionComparator = getVersionComparator();
        Iterator i = Arrays.asList( getVersions( includeSnapshots ) ).iterator();
        while ( i.hasNext() )
        {
            ArtifactVersion candidate = (ArtifactVersion) i.next();
            int lower = lowerBound == null ? -1 : versionComparator.compare( lowerBound, candidate );
            int upper = upperBound == null ? +1 : versionComparator.compare( upperBound, candidate );
            if ( lower > 0 || upper < 0 )
            {
                continue;
            }
            if ( ( !includeLower && lower == 0 ) || ( !includeUpper && upper == 0 ) )
            {
                continue;
            }
            if ( !includeSnapshots && ArtifactUtils.isSnapshot( candidate.toString() ) )
            {
                continue;
            }
            if ( oldest == null )
            {
                oldest = candidate;
            }
            else if ( versionComparator.compare( oldest, candidate ) > 0 )
            {
                oldest = candidate;
            }
        }
        return oldest;
    }

    public final ArtifactVersion[] getVersions( ArtifactVersion lowerBound, ArtifactVersion upperBound,
                                                boolean includeSnapshots, boolean includeLower, boolean includeUpper )
    {
        Set/*<ArtifactVersion>*/ result;
        final VersionComparator versionComparator = getVersionComparator();
        result = new TreeSet( versionComparator );
        Iterator i = Arrays.asList( getVersions( includeSnapshots ) ).iterator();
        while ( i.hasNext() )
        {
            ArtifactVersion candidate = (ArtifactVersion) i.next();
            int lower = lowerBound == null ? -1 : versionComparator.compare( lowerBound, candidate );
            int upper = upperBound == null ? +1 : versionComparator.compare( upperBound, candidate );
            if ( lower > 0 || upper < 0 )
            {
                continue;
            }
            if ( ( !includeLower && lower == 0 ) || ( !includeUpper && upper == 0 ) )
            {
                continue;
            }
            if ( !includeSnapshots && ArtifactUtils.isSnapshot( candidate.toString() ) )
            {
                continue;
            }
            result.add( candidate );
        }
        return (ArtifactVersion[]) result.toArray( new ArtifactVersion[result.size()] );
    }
}
