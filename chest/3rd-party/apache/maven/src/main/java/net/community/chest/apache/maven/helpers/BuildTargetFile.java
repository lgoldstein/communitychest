/**
 * 
 */
package net.community.chest.apache.maven.helpers;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.community.chest.CoVariantReturn;
import net.community.chest.dom.DOMUtils;
import net.community.chest.lang.StringUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Used to translate between a file that represents the artifact and
 * its target details</P>
 * @author Lyor G.
 * @since Aug 13, 2008 2:16:13 PM
 */
public class BuildTargetFile extends BaseTargetDetails {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8224310542152989812L;
	public BuildTargetFile ()
	{
		super();
	}
	// Some known types
	public static final String	JAR_FILE_TYPE="jar",
								ZIP_FILE_TYPE="zip",
								POM_FILE_TYPE="pom",
								SRC_FILE_TYPE="sources",
								DOC_FILE_TYPE="javadoc";

	/**
	 * Type of file represented in the instance 
	 */
	private String	_fileType	/* =null */;
	public String getFileType ()
	{
		return _fileType;
	}

	public void setFileType (String fileType)
	{
		_fileType = fileType;
	}

	/**
	 * File packaging type - default={@link #JAR_FILE_TYPE}
	 */
	private String	_filePackaging=JAR_FILE_TYPE;
	public String getFilePackaging ()
	{
		return _filePackaging;
	}

	public void setFilePackaging (String filePackaging)
	{
		_filePackaging = filePackaging;
	}

	public <A extends Appendable> A appendArtifactFileName (final A sb) throws IOException
	{
		if (null == sb)
			throw new IOException("No " + Appendable.class.getSimpleName() + " instance provided");

		final String	artifactId=getArtifactId(),
						version=getVersion(),
						fileType=getFileType(),
						pkgType=getFilePackaging();
		final int		aLen=(null == artifactId) ? 0 : artifactId.length(),
						vLen=(null == version) ? 0 : version.length(),
						pLen=(null == pkgType) ? 0 : pkgType.length();
		if ((aLen <= 0) || (vLen <= 0) || (pLen <= 0))
			throw new IOException("Incomplete target file specification");

		sb.append(artifactId)
			.append('-')
			.append(version)
			;
		if (SRC_FILE_TYPE.equalsIgnoreCase(fileType)
		 || DOC_FILE_TYPE.equalsIgnoreCase(fileType))
			sb.append('-')
			  .append(fileType)
			  .append('.')
			  .append(pkgType)
			  ;
		else
			sb.append('.')
			  .append(pkgType)
			  ;

		return sb;
	}

	public String toArtifactFileName () throws IllegalStateException
	{
		try
		{
			final Appendable	sb=appendArtifactFileName(new StringBuilder(128));
			return sb.toString();
		}
		catch(IOException e)
		{
			throw new IllegalStateException(e.getMessage());
		}
	}
	/**
	 * Converts the instance into a relative path of the artifact
	 * @param sepChar Separator character to use when building the path
	 * @return Relative path of the artifact from repository root using
	 * the supplied separator character
	 * @throws IllegalStateException if incomplete data in instance (e.g.,
	 * no group or no version, etc.)
	 */
	public String toRelativeArtifactPath (final char sepChar) throws IllegalStateException
	{
		final String	groupId=getGroupId(),
						groupPath=((null == groupId) || (groupId.length() <= 0)) ? null : groupId.replace('.', sepChar),
						artifactId=getArtifactId(),
						version=getVersion(),
						fileType=getFileType();
		final int		gLen=(null == groupPath) ? 0 : groupPath.length(),
						aLen=(null == artifactId) ? 0 : artifactId.length(),
						vLen=(null == version) ? 0 : version.length(),
						tLen=(null == fileType)? 0 : fileType.length();
		if ((gLen <= 0) || (aLen <= 0) || (vLen <= 0))
			throw new IllegalStateException("Incomplete target file specification");

		final StringBuilder	sb=new StringBuilder(gLen + 2 * (aLen + vLen) + 2 * Math.max(0, tLen) + 8)
							.append(groupPath)
							.append(sepChar)
							.append(artifactId)
							.append(sepChar)
							.append(version)
							.append(sepChar)
							;
		try
		{
			appendArtifactFileName(sb);
		}
		catch(IOException e)
		{
			throw new IllegalStateException(e.getMessage());
		}

		return sb.toString();
	}

	public URL toURL (final URL baseURL) throws MalformedURLException
	{
		final String	urlPrefix=(null == baseURL) ? null : baseURL.toString();
		final int		pLen=(null == urlPrefix) ? 0 : urlPrefix.length();
		if (pLen <= 0)
			throw new MalformedURLException("No base URL specified");

		final String	relPath=toRelativeArtifactPath('/');
		final int		rLen=(null == relPath) ? 0 : relPath.length();
		if (rLen <= 0)
			throw new MalformedURLException("No relative location available");

		final StringBuilder	sb=new StringBuilder(pLen + rLen + 4).append(urlPrefix);
		if ((urlPrefix.charAt(pLen-1) != '/') && (relPath.charAt(0) != '/'))
			sb.append('/');
		sb.append(relPath);

		return new URL(sb.toString());
	}
	/**
	 * Converts the instance into a relative path of the artifact
	 * @return Relative path of the artifact from repository root using the
	 * current O/S default {@link File#separatorChar}
	 * @throws IllegalStateException if incomplete data in instance (e.g.,
	 * no group or no version, etc.)
	 * @see #toRelativeArtifactPath(char) for control of the separator
	 */
	public String toRelativeArtifactPath () throws IllegalStateException
	{
		return toRelativeArtifactPath(File.separatorChar);
	}
	/*
	 * @see net.community.chest.apache.maven.helpers.BaseTargetDetails#clear()
	 */
	@Override
	public void clear ()
	{
		super.clear();

		setFileType((String) null);
		setFilePackaging((String) null);
	}
	/*
	 * @see net.community.chest.apache.maven.helpers.BaseTargetDetails#equals(java.lang.Object)
	 */
	@Override
	public boolean equals (final Object obj)
	{
		if (!super.equals(obj))
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof BuildTargetFile))
			return false;

		final BuildTargetFile	btf=(BuildTargetFile) obj;
		return (0 == StringUtil.compareDataStrings(getFileType(), btf.getFileType(), false))
			&& (0 == StringUtil.compareDataStrings(getFilePackaging(), btf.getFilePackaging(), false))
			;
	}
	/*
	 * @see net.community.chest.apache.maven.helpers.BaseTargetDetails#hashCode()
	 */
	@Override
	public int hashCode ()
	{
		return super.hashCode()
			+ StringUtil.getDataStringHashCode(getFileType(), false)
			+ StringUtil.getDataStringHashCode(getFilePackaging(), false)
			;
	}
	/*
	 * @see net.community.chest.apache.maven.helpers.BaseTargetDetails#toString()
	 */
	@Override
	public String toString ()
	{
		return super.toString() + "[" + getFileType() + "]";
	}
	/*
	 * @see net.community.chest.apache.maven.helpers.BaseTargetDetails#clone()
	 */
	@Override
	@CoVariantReturn
	public BuildTargetFile clone () throws CloneNotSupportedException
	{
		return getClass().cast(super.clone());
	}

	public static final String	FILETYPE_ATTR="fileType";
	public String setFileType (Element elem)
	{
		final String	t=elem.getAttribute(FILETYPE_ATTR);
		if ((t != null) && (t.length() > 0))
			setFileType(t);

		return t;
	}

	public static final String	PKGFILE_ATTR="pkgType";
	public String setFilePackaging (Element elem)
	{
		final String	t=elem.getAttribute(PKGFILE_ATTR);
		if ((t != null) && (t.length() > 0))
			setFilePackaging(t);

		return t;
	}
	/*
	 * @see net.community.chest.apache.maven.helpers.BaseTargetDetails#toXml(org.w3c.dom.Document)
	 */
	@Override
	public Element toXml (final Document doc) throws Exception
	{
		final Element	rootElem=super.toXml(doc);
		DOMUtils.addNonEmptyAttribute(rootElem, FILETYPE_ATTR, getFileType());
		DOMUtils.addNonEmptyAttribute(rootElem, PKGFILE_ATTR, getFilePackaging());
		return rootElem;
	}
	/*
	 * @see net.community.chest.apache.maven.helpers.BaseTargetDetails#fromXml(org.w3c.dom.Element)
	 */
	@Override
	@CoVariantReturn
	public BuildTargetFile fromXml (final Element elem) throws Exception
	{
		final BaseTargetDetails	baseInst=super.fromXml(elem);
		if (baseInst != this)
			throw new IllegalStateException("fromXml() mismatched re-constructed instances");

		setFileType(elem);
		setFilePackaging(elem);
		return this;
	}

	public BuildTargetFile (Element elem) throws Exception
	{
		super(elem);
	}
	/**
	 * Translates a relative file path into a {@link BuildTargetFile} instance
	 * @param <T> The {@link BuildTargetFile} derived instance to update
	 * @param tgt The {@link BuildTargetFile} instance to update
	 * @param relPath The relative artifact file path from repository root
	 * @param sepChar Separator character of the path components
	 * @return The matching {@link BuildTargetFile} instance - null if
	 * null/empty path
	 * @throws IllegalArgumentException If bad relative path format (e.g., not
	 * enough components)
	 */
	public static final <T extends BuildTargetFile> T updateRelativeArtifactPath (
			final T tgt, final String relPath, final char sepChar) throws IllegalArgumentException
	{
		final List<String>	cl=StringUtil.splitString(relPath, sepChar);
		final int			numComps=(null == cl) ? 0 : cl.size();
		if (numComps <= 0)	// OK if no path
			return tgt;

		// min.=group/artifact/version/file
		if (numComps < 4)
			throw new IllegalArgumentException("updateRelativeArtifactPath(" + relPath + ") not enough components");

		final String	fileName=cl.get(numComps - 1),
						version=cl.get(numComps - 2),
						artifactId=cl.get(numComps - 3);
		final int		nLen=(null == fileName) ? 0 : fileName.length(),
						aLen=(null == artifactId) ? 0 : artifactId.length(),
						vLen=(null == version) ? 0 : version.length(),
						dotPos=(nLen <= 1) ? (-1) : fileName.lastIndexOf('.');
		final String	filePrefix=
			((dotPos <= 0) || (dotPos >= (nLen-1))) ? null : fileName.substring(0, dotPos),
						fileSuffix=
			((dotPos <= 0) || (dotPos >= (nLen-1))) ? null : fileName.substring(dotPos + 1);
		final int		prfxLen=(null == filePrefix) ? 0 : filePrefix.length(),
						sfxLen=(null == fileSuffix) ? 0 : fileSuffix.length();
		if ((nLen <= 0) || (prfxLen <= 0)
		 || (sfxLen <= 0)
		 || (vLen <= 0)
		 || (aLen <= 0)
		 // file name must contain the version and the artifact + suffix
		 || (nLen <= (aLen + vLen)))
			throw new IllegalArgumentException("updateRelativeArtifactPath(" + relPath + ") bad components");

		final String	fileType, pureName;
		// if prefix is exactly of same length as artifact-version then assume no special handling
		if (prfxLen == (aLen + vLen + 1))
		{
			pureName = filePrefix;
			fileType = fileSuffix;
		}
		else	// must be like "-sources" or "-javadoc"
		{
			final int	tPos=filePrefix.lastIndexOf('-');
			if ((tPos <= 0) || (tPos >= (prfxLen-1)))
				throw new IllegalArgumentException("updateRelativeArtifactPath(" + relPath + ") bad artifact file type");

			pureName = filePrefix.substring(0, tPos);
			fileType = filePrefix.substring(tPos + 1);

			// make sure that suffix is JAR or ZIP if "-sources" or "-javadoc" file type
			if (DOC_FILE_TYPE.equalsIgnoreCase(fileType)
			 || SRC_FILE_TYPE.equalsIgnoreCase(fileType))
			{
				if ((!JAR_FILE_TYPE.equalsIgnoreCase(fileSuffix))
				 && (!ZIP_FILE_TYPE.equalsIgnoreCase(fileSuffix)))
					throw new IllegalArgumentException("updateRelativeArtifactPath(" + relPath + ") bad artifact file suffix");
			}
		}

		final int	pLen=(null == pureName) ? 0 : pureName.length();
		// pure name must be exactly same as artifact-version
		if ((pLen <= 0) || (pLen != (aLen + vLen + 1))
		 || (null == fileType) || (fileType.length() <= 0))
			throw new IllegalArgumentException("updateRelativeArtifactPath(" + relPath + ") bad artifact file name");

		final String	aName=pureName.substring(0, aLen),
						aVersion=pureName.substring(aLen + 1);
		if ((StringUtil.compareDataStrings(aName, artifactId, false) != 0)
		 || (StringUtil.compareDataStrings(aVersion, version, false) != 0))
			throw new IllegalArgumentException("updateRelativeArtifactPath(" + relPath + ") bad artifact file name components");

		final StringBuilder	sbGroup=new StringBuilder((numComps - 3) * 32);
		for (int	cIndex=0; cIndex < (numComps - 3); cIndex++)
		{
			final String	c=cl.get(cIndex);
			if ((null == c) || (c.length() <= 0))
				throw new IllegalArgumentException("updateRelativeArtifactPath(" + relPath + ") bad group path component at index=" + cIndex);

			if (cIndex > 0)
				sbGroup.append('.');
			sbGroup.append(c);
		}

		tgt.setGroupId(sbGroup.toString());
		tgt.setArtifactId(artifactId);
		tgt.setVersion(version);
		tgt.setFileType(fileType);

		if (POM_FILE_TYPE.equalsIgnoreCase(fileType))
			tgt.setFilePackaging(fileType);
		else
			tgt.setFilePackaging(fileSuffix);

		return tgt;
	}
	/**
	 * Translates a relative file path into a {@link BuildTargetFile} instance
	 * @param relPath The relative artifact file path from repository root
	 * @param sepChar Separator character of the path components
	 * @return The matching {@link BuildTargetFile} instance - null if
	 * null/empty path
	 * @throws IllegalArgumentException If bad relative path format (e.g., not
	 * enough components)
	 */
	public static final BuildTargetFile fromRelativeArtifactPath (final String relPath, final char sepChar) throws IllegalArgumentException
	{
		if ((null == relPath) || (relPath.length() <= 0))
			return null;

		return updateRelativeArtifactPath(new BuildTargetFile(), relPath, sepChar);
	}
	/**
	 * Translates a relative file path into a {@link BuildTargetFile} instance
	 * @param relPath The relative artifact file path from repository root
	 * (<B>Note:</B> assumed to use {@link File#separatorChar})
	 * @return The matching {@link BuildTargetFile} instance - null if
	 * null/empty path
	 * @throws IllegalArgumentException If bad relative path format (e.g., not
	 * enough components)
	 * @see #fromRelativeArtifactPath(String, char) for separator control
	 */
	public static final BuildTargetFile fromRelativeArtifactPath (final String relPath) throws IllegalArgumentException
	{
		return fromRelativeArtifactPath(relPath, File.separatorChar);
	}

	public static final List<BuildTargetFile> fromNodesList (final NodeList nl) throws Exception
	{
		final int					numNodes=(null == nl) ? 0 : nl.getLength();
		final List<BuildTargetFile>	fl=(numNodes <= 0) ? null : new ArrayList<BuildTargetFile>(numNodes);
		for (int	nIndex=0; nIndex < numNodes; nIndex++)
		{
			final Node	n=nl.item(nIndex);
			if ((null == n) || (n.getNodeType() != Node.ELEMENT_NODE))
				continue;

			fl.add(new BuildTargetFile((Element) n));
		}

		return fl;
	}

	public static final List<BuildTargetFile> fromRootElement (Element root) throws Exception
	{
		return (null == root) ? null : fromNodesList(root.getChildNodes());
	}

	public static final List<BuildTargetFile> fromRootElement (Document doc) throws Exception
	{
		return (null == doc) ? null : fromRootElement(doc.getDocumentElement());
	}
}
