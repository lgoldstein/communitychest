/**
 * Copyright 2009-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.springsource.insight.samples.rest.restful;

import java.net.URI;
import java.util.Collection;
import java.util.Date;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.jboss.resteasy.plugins.providers.atom.Content;
import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.jboss.resteasy.plugins.providers.atom.Feed;
import org.jboss.resteasy.plugins.providers.atom.Link;
import org.jboss.resteasy.plugins.providers.atom.Person;
import org.springframework.stereotype.Component;

import com.springsource.insight.samples.rest.AbstractRequestHandler;
import com.springsource.insight.samples.rest.model.RestfulData;
import com.springsource.insight.samples.rest.model.RestfulDataList;
import com.springsource.insight.samples.rest.model.RestfulService;

/**
 * @author lgoldstein
 */
@Path("/restful")
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML })
@Component
public class RestfulAccessor extends AbstractRequestHandler {
	private final RestfulService	_service;

	@Inject
	public RestfulAccessor (final RestfulService service) {
		_service = service;
	}

	@GET	// no need for @Path since the root GET is interpreted as calling this method 
	public Object listAll (@Context 											final  UriInfo uriInfo,
						   @QueryParam("feed")  @DefaultValue("false") 			final boolean asFeed,
						   @QueryParam("delay") @DefaultValue(DEFAULT_DELAY) 	final int maxDelay) {
		final RestfulDataList	list=_service.findAll();
		delay(maxDelay);
		if (asFeed) {
			return wrapInFeed("Instances", list, uriInfo, BY_ID_TEMPLATE);
		}

		return Response.ok(list, MediaType.APPLICATION_XML_TYPE).build();
	}

	public static final String	ID_PARAM_NAME="id", BY_ID_TEMPLATE="{" + ID_PARAM_NAME + "}";
	@GET
	@Path(BY_ID_TEMPLATE)
	public Response getData (@PathParam(ID_PARAM_NAME) final long id)	{
		final RestfulData	value=_service.getData(id);
		if (value== null) {
			return Response.status(Response.Status.GONE).build();
		}

		return Response.ok(value, MediaType.APPLICATION_XML_TYPE).build();
	}

	public static final String BALANCE_PARAM_NAME="balance", BALANCE_TEMPLATE="{" + BALANCE_PARAM_NAME + "}";
	@POST
	@Path(BALANCE_TEMPLATE)
	public Response create (@PathParam(BALANCE_PARAM_NAME) final int balance) {
		final RestfulData	value=_service.create(balance);
		return Response.ok(value, MediaType.APPLICATION_XML_TYPE).build();
	}

	@PUT
	@Path(BY_ID_TEMPLATE + "/" + BALANCE_TEMPLATE)
	public Response update (@PathParam(ID_PARAM_NAME) final long id, @PathParam(BALANCE_PARAM_NAME) final int balance) {
		final RestfulData	value=_service.setBalance(id, balance);
		if (value== null) {
			return Response.status(Response.Status.GONE).build();
		}

		return Response.ok(value, MediaType.APPLICATION_XML_TYPE).build();
	}

	@DELETE
	@Path(BY_ID_TEMPLATE)
	public Response delete (@PathParam(ID_PARAM_NAME) final long id) {
		final RestfulData	value=_service.removeData(id);
		if (value== null) {
			return Response.status(Response.Status.GONE).build();
		}

		return Response.ok(value, MediaType.APPLICATION_XML_TYPE).build();
	}

	protected Content wrapInContent (final Object dto)
	{
		if (null == dto)
			return null;

		final Content content=new Content();
		content.setType(MediaType.APPLICATION_XML_TYPE);
		content.setJAXBObject(dto);
		return content;
	}

	protected Entry wrapInFeedEntry (final Object dto, final URI dtoUri, final Date updated)
	{
		final Content	c=wrapInContent(dto);
		if (null == c)
			return null;

		final Entry	entry=new Entry();
		entry.setId(dtoUri);
		entry.setUpdated(updated);
		entry.setContent(c);
		return entry;
	}

	protected Feed wrapInFeed (final String								title,
							   final Collection<? extends RestfulData> 	dtoList,
							   final UriInfo							uriInfo,
							   final String								urlTemplate)
	{
		final Feed	feed=new Feed();
		final URI	absPath=uriInfo.getAbsolutePath();
		final Date	now=new Date(System.currentTimeMillis());
		feed.setId(absPath);
		feed.setTitle(title);
		feed.setUpdated(now);
		feed.getAuthors().add(new Person("Lyor Goldstein"));
	    feed.getLinks().add(new Link("edit", absPath, MediaType.TEXT_XML_TYPE));

		if ((null == dtoList) || (dtoList.size() <= 0))
			return feed;
		
		for (final RestfulData dto : dtoList)
		{
			final Long			dtoId=dto.getId();
			final UriBuilder	ub=(null == dtoId) ? null : uriInfo.getBaseUriBuilder();
			final URI 			dtoUri=(null == ub) ? null : ub.path(getClass()).path(urlTemplate).build(dtoId);
			final Entry 		entry=wrapInFeedEntry(dto, dtoUri, now);
			if (null == entry)
				continue;
			entry.setBase(absPath);
			feed.getEntries().add(entry);
		}

		return feed;
	}

	protected Feed wrapInFeed (Entry entry, URI uri, Date updated)
	{
		final Feed	feed=new Feed();
		feed.setId(uri);
		feed.setUpdated(updated);
		feed.getAuthors().add(new Person("Lyor Goldstein"));
	    feed.getLinks().add(new Link("edit", uri, MediaType.TEXT_XML_TYPE));
	    feed.getEntries().add(entry);
		return feed;
	}

	private static final Object[] createTemplateArguments (
			final Long dtoId, final Object ... extraArgs)
	{
		final int		numExtra=(null == extraArgs) ? 0 : extraArgs.length;
		final Object[]	ret=new Object[Math.max(0, numExtra) + 1];

		ret[0] = dtoId;
		if (numExtra > 0)
			System.arraycopy(extraArgs, 0, ret, 1, numExtra);
		return ret;
	}

	private Response createResponse (final UriInfo 			uriInfo,
									 final Object			dto,
									 final Long				dtoId,
									 final Response.Status 	stValue,
									 final String 			template,
									 final Object ...		extraArgs)
	{
		final UriBuilder 		ub=uriInfo.getBaseUriBuilder();
		final Object[] 			buildArgs=
			createTemplateArguments(dtoId, extraArgs);
		final URI 				dtoUri=
			ub.path(this.getClass()).path(template).build(buildArgs);
		final ResponseBuilder 	rb=Response.status(stValue).location(dtoUri);
		if (dto != null)
		{
			final Date	now=new Date(System.currentTimeMillis());
			final Entry entry=wrapInFeedEntry(dto, dtoUri, now);
			rb.entity(wrapInFeed(entry, null, now));
		}

		return rb.build();
	}

	public static final Object[] EMPTY_OBJECT_ARGS=new Object[0];
	protected Response createRetrievedResponse (final UriInfo 	uriInfo,
			  									final Long		id,
			  									final Object 	dto,
			  									final String 	template)
	{
		// see http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.11
		final Response.Status	stValue=
			(null == dto) ? Response.Status.GONE : Response.Status.OK;
		return createResponse(uriInfo, dto, id, stValue, template, EMPTY_OBJECT_ARGS);
	}
}
